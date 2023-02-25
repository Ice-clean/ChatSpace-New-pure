package top.iceclean.chatspace.cache;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.iceclean.chatspace.infrastructure.constant.GroupType;
import top.iceclean.chatspace.infrastructure.constant.RedisKey;
import top.iceclean.chatspace.infrastructure.constant.ZoneType;
import top.iceclean.chatspace.infrastructure.po.Group;
import top.iceclean.chatspace.infrastructure.po.Zone;
import top.iceclean.chatspace.infrastructure.utils.RedissonUtils;
import top.iceclean.chatspace.infrastructure.vo.SiteVO;
import top.iceclean.chatspace.infrastructure.vo.ZoneVO;
import top.iceclean.feign.GroupClient;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 区域相关缓存
 * @author : Ice'Clean
 * @date : 2022-11-28
 */
@Slf4j
@Component
public class ZoneCache {

    private final RedissonUtils redissonUtils;
    private final GroupClient groupClient;

    private final GroupCache groupCache;
    private final GeoCache geoCache;

    /** 缓存用户未 ACK 的区域进入 */
    private final UserCache.State tempZoneInState;

    /** 区域的关键缓存数据 */
    @Getter
    @AllArgsConstructor
    public static class Info {
        private final Integer zoneId;
        private final Integer groupId;
        private final Integer zoneType;

        public static String serialize(Zone zone) {
            return zone.getZoneType() + "-" + zone.getGroupId() + "-" + zone.getId();
        }

        public static Info parse(String data) {
            int[] info = Arrays.stream(data.split("-")).mapToInt(Integer::parseInt).toArray();
            return new Info(info[2], info[1], info[0]);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Info info = (Info) o;
            return Objects.equals(zoneId, info.zoneId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(zoneId);
        }
    }

    public ZoneCache(RedissonUtils redissonUtils, GroupClient groupClient, GroupCache groupCache, GeoCache geoCache) {
        this.redissonUtils = redissonUtils;
        this.groupClient = groupClient;
        this.groupCache = groupCache;
        this.geoCache = geoCache;
        this.tempZoneInState = UserCache.getIntState(RedisKey.TEMP_ZONE_IN, redissonUtils, UserCache.State.Type.INT);
    }

    /**
     * 根据区域 ID 获取区域 VO
     * 通过使用区域缓存和群组缓存来构造
     * @param zoneId 区域 ID
     */
    public ZoneVO getZoneVO(int zoneId) {
        // 从缓存中获取区域实体和群组实体
        Zone zone = this.getZone(zoneId);
        Group group = groupCache.getGroup(zone.getGroupId());
        // 默认没有会话 ID并构造区域响应体
        return new ZoneVO(zone, group);
    }

    /**
     * 更具区域 ID 获取区域实体
     * 缓存中找不到时，则去数据库查找并回种
     * @param zoneId 区域 ID
     */
    public Zone getZone(int zoneId) {
        // 先在缓存中获取
        Object zoneCache = redissonUtils.hashGet(RedisKey.ZONE_HASH, zoneId);
        if (zoneCache == null) {
            // 缓存中不存在，则访问服务获取
            Zone zone = groupClient.getZoneById(zoneId);
            // 然后回种缓存并返回
            redissonUtils.hashSet(RedisKey.ZONE_HASH, zoneId, JSON.toJSONString(zone));
            return zone;
        }

        // 缓存中存在，直接反序列化后返回
        return JSON.parseObject(zoneCache.toString(), Zone.class);
    }

    /**
     * 显式将区域实体缓存起来，并返回区域响应对象（发生在区域新建出来的时候）
     * @param zone 区域实体
     * @return 区域响应对象
     */
    public ZoneVO cachedZone(Zone zone) {
        // 将区域缓存起来，并缓存其位置
        redissonUtils.hashSet(RedisKey.ZONE_HASH, zone.getId(), JSON.toJSONString(zone));
        cachedZoneSite(zone);
        Group group = groupCache.getGroup(zone.getGroupId());
        return new ZoneVO(zone, group);
    }

    /**
     * 将指定区域的所有坐标缓存起来（默认实现，调用各个实现类的缓存操作方法）
     * @param zone 指定的区域
     */
    public void cachedZoneSite(Zone zone) {
        // 先将区域的四个坐标加入，用于判断区域重叠
        geoCache.putZone(zone);

        // 扫描全部坐标，缓存坐标和区域 ID 的映射关系
        String key = RedisKey.ZONE_SITE_HASH + zone.getSpaceId();
        String zoneInfo = Info.serialize(zone);
        int startX = zone.getStartX();
        int startY = zone.getStartY();
        int endX = zone.getEndX();
        int endY = zone.getEndY();
        int pId = zone.getZonePid();
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y<= endY; y++) {
                // 形式为（pid-x-y : type-groupId-zoneId）
                redissonUtils.hashSet(key, pId + "-" + x + "-" + y, zoneInfo);
            }
        }
    }

    /**
     * 获取指定空间指定坐标属于哪个区域
     * @param pid 当前所进入的区域 ID
     * @param site 指定位置，提供 spaceId 和 x y 坐标
     * @return 有区域则返回区域类型和 ID的数组，否则返回 null
     */
    public Info getZoneInfo(int pid, SiteVO site) {
        Object result = redissonUtils.hashGet(RedisKey.ZONE_SITE_HASH + site.getSpaceId(),
                pid + "-" + site.getX() + "-" + site.getY());
        if (result != null) {
            return Info.parse(result.toString());
        }
        return null;
    }

    /**
     * 获取用户当前所进入的区域（真正执行 ZONE_ACK 之后）
     * 如果没有，返回 0
     * @param userId 用户 ID
     * @return 所进入的区域
     */
    public int getUserZoneId(int userId) {
        Integer zoneId = redissonUtils.listGetLast(RedisKey.USER_ZONE_LIST + userId);
        return zoneId == null ? 0 : zoneId;
    }

    /** 用户临时进入区域（未确认） */
    public void zoneInTemp(int zoneId, int userId) {
        tempZoneInState.set(userId, zoneId);
    }

    /** 获取用户临时进入的区域 */
    public Integer getTempZoneIn(int userId) {
        return tempZoneInState.get(userId);
    }

    /** 清除并返回临时区域 */
    public Integer cleanTempZoneIn(int userId) {
        Integer tempZoneIn = tempZoneInState.get(userId);
        tempZoneInState.clean(userId);
        return tempZoneIn;
    }

    /** 清除用户的区域信息 */
    public void cleanUserZone(int userId) {
        redissonUtils.delete(RedisKey.USER_ZONE_LIST + userId);
    }

    /** 用户进入区域 */
    public void zoneIn(int zoneId, int userId) {
        // 将新区域入栈
        redissonUtils.listPushLast(RedisKey.USER_ZONE_LIST + userId, zoneId);
        // 将用户 ID 缓存到指定区域
        redissonUtils.setAdd(RedisKey.ZONE_USER_SET + zoneId, userId);
    }

    /** 用户离开当前区域，并返回其父级区域 */
    public void zoneOut(int zoneId, int userId) {
        // 将当前区域出栈（也就是最后一个区域）
        Integer exitZoneId = redissonUtils.listPopLast(RedisKey.USER_ZONE_LIST + userId);
        if (zoneId != exitZoneId) {
            // 理论上应该相等，不相等就是出问题了，给出警告！
            log.error("严重：退出的区域与当前所处的区域不相等：{} != {}", zoneId, exitZoneId);
        }
        // 将用户从区域中移除
        redissonUtils.setRemove(RedisKey.ZONE_USER_SET + zoneId, userId);
    }

    /** 判断用户是否在指定区域中 */
    public boolean containsUser(int zoneId, int userId) {
        return zoneId == 0 || redissonUtils.setContains(RedisKey.ZONE_USER_SET + zoneId, userId);
    }

    /** 判断用户有无进入区域的权限 */
    public boolean permitEnter(int zoneId, int userId) {
        // 获取区域，如果是系统群组、公开区域或者加入了群组，则可以进入
        Zone zone = getZone(zoneId);
        Group group = groupCache.getGroup(zone.getGroupId());
        return group.getGroupType() == GroupType.SYSTEM.value() ||
                zone.getZoneType().equals(ZoneType.PUBLIC.value()) ||
                groupCache.containsUser(zone.getGroupId(), userId);
    }

    /** 获取区域中的所有用户 */
    public Set<Integer> getZoneUsers(int zoneId) {
        return redissonUtils.setGet(RedisKey.ZONE_USER_SET + zoneId)
                .stream().map(id -> Integer.parseInt(id.toString())).collect(Collectors.toSet());
    }

    /**
     * 判断指定区域是否与已有的区域发生重叠（同一个嵌套级别）
     * @param zone 区域实体
     * @return 重叠了返回 true，否则 false
     */
    public boolean isZoneCover(Zone zone) {
        String key = RedisKey.ZONE_SITE_HASH + zone.getSpaceId();
        int startX = zone.getStartX();
        int startY = zone.getStartY();
        int endX = zone.getEndX();
        int endY = zone.getEndY();
        int pId = zone.getZonePid();
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y<= endY; y++) {
                // 形式为（pid-x-y : type-groupId-zoneId）
                if (redissonUtils.hashGet(key, pId + "-" + x + "-" + y) != null) {
                    return true;
                }
            }
        }
        // 返回没重叠
        return false;
    }
}
