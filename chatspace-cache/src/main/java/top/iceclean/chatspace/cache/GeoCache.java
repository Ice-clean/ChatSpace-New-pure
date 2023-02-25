package top.iceclean.chatspace.cache;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.GeoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.iceclean.chatspace.infrastructure.constant.RedisKey;
import top.iceclean.chatspace.infrastructure.dto.SiteDTO;
import top.iceclean.chatspace.infrastructure.po.Site;
import top.iceclean.chatspace.infrastructure.po.Zone;
import top.iceclean.chatspace.infrastructure.utils.GeoUtils;
import top.iceclean.chatspace.infrastructure.utils.RedissonUtils;
import top.iceclean.chatspace.infrastructure.vo.SiteVO;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * GEO 相关缓存
 * @author : Ice'Clean
 * @date : 2022-11-29
 */
@Slf4j
@Component
public class GeoCache {

    private final RedissonUtils redissonUtils;

    /** 地图比例 */
    private static final int DEFAULT_RATE = 1;
    /** 误差允许 */
    private static final double ERROR_RATE = 1e-2;
    /** 默认单位 */
    private static final GeoUnit DEFAULT_UNIT = GeoUnit.KILOMETERS;

    @Autowired
    public GeoCache(RedissonUtils redissonUtils) {
        this.redissonUtils = redissonUtils;
    }

    /**
     * 将当前用户放入 GEO 缓存
     * @param siteVO 用户的坐标信息
     */
    public void putUser(SiteVO siteVO) {
        // 先进行坐标的转换
        double[] geoSite = GeoUtils.compute(siteVO.getX(), siteVO.getY(), DEFAULT_RATE);
        redissonUtils.geoAdd(RedisKey.USER_SITE_HASH + siteVO.getSpaceId(), geoSite[0], geoSite[1], siteVO.getUser().getUserId());
    }
    public void putUser(SiteDTO siteDTO, int userId) {
        // 先进行坐标的转换
        double[] geoSite = GeoUtils.compute(siteDTO.getX(), siteDTO.getY(), DEFAULT_RATE);
        redissonUtils.geoAdd(RedisKey.USER_SITE_HASH + siteDTO.getSpaceId(), geoSite[0], geoSite[1], userId);
    }

    /**
     * 将用户从 GEO 缓存中移除
     * @param siteVO 用户坐标信息
     */
    public void removeUser(SiteVO siteVO) {
        log.info("查出用户 GEO 信息：{}", siteVO.getUser().getUserId());
        redissonUtils.geoRemove(RedisKey.USER_SITE_HASH + siteVO.getSpaceId(), siteVO.getUser().getUserId());
    }

    /**
     * 将当前区域的四个坐标放入 GEO 缓存
     * @param zone 区域实体
     */
    public void putZone(Zone zone) {
        String key = RedisKey.ZONE_POINT_HASH + zone.getSpaceId();
        double[] site1 = GeoUtils.compute(zone.getStartX(), zone.getStartY(), DEFAULT_RATE);
        double[] site2 = GeoUtils.compute(zone.getStartX(), zone.getEndY(), DEFAULT_RATE);
        double[] site3 = GeoUtils.compute(zone.getEndX(), zone.getStartY(), DEFAULT_RATE);
        double[] site4 = GeoUtils.compute(zone.getEndX(), zone.getEndY(), DEFAULT_RATE);
        redissonUtils.geoAdd(key, site1[0], site1[1], zone.getId() + "-1");
        redissonUtils.geoAdd(key, site2[0], site2[1], zone.getId() + "-2");
        redissonUtils.geoAdd(key, site3[0], site3[1], zone.getId() + "-3");
        redissonUtils.geoAdd(key, site4[0], site4[1], zone.getId() + "-4");
    }

    /**
     * 将区域从 GEO 缓存中移除
     * @param zone 区域试图以
     */
    public void removeZone(Zone zone) {
        String key = RedisKey.ZONE_POINT_HASH + zone.getSpaceId();
        redissonUtils.geoRemove(key, zone.getId() + "-1");
        redissonUtils.geoRemove(key, zone.getId() + "-2");
        redissonUtils.geoRemove(key, zone.getId() + "-3");
        redissonUtils.geoRemove(key, zone.getId() + "-4");
    }

    /**
     * 获取用户的虚拟坐标
     * @param spaceId 所处空间 ID
     * @param userId 用户 ID
     * @return 虚拟位置
     */
    public Site getUserSite(int spaceId, int userId) {
        // 从缓存中获取实时的物理坐标
        double[] pos = redissonUtils.geoGet(RedisKey.USER_SITE_HASH + spaceId, userId);
        if (pos == null) {
            return null;
        }
        // 将物理坐标转化为虚拟坐标
        int[] vPos = GeoUtils.recompute(pos[0], pos[1], DEFAULT_RATE);
        Site site = new Site();
        site.setId(userId);
        site.setSpaceId(spaceId);
        site.setX(vPos[0]);
        site.setY(vPos[1]);
        log.info("实时坐标转化：{}", site);
        return site;
    }

    /**
     * 获取指定用户视野内的其他用户
     * @return 该用户视野内所有的用户 ID
     */
    public Map<Object, Double> getVisionCover(int spaceId, int userId, double vision) {
        // 查询指定空间，指定用户，指定范围内的所有用户 ID
        return redissonUtils.geoRadius(RedisKey.USER_SITE_HASH + spaceId, userId, DEFAULT_RATE * vision + ERROR_RATE, DEFAULT_UNIT);
    }

    /**
     * 获取指定用户存在感内的其他用户
     * @return 该用户存在感内所有的用户 ID
     */
    public Map<Object, Double> getExistCover(int spaceId, int userId, double exist) {
        // 查询指定空间，指定用户，指定范围内的所有用户 ID
        return redissonUtils.geoRadius(RedisKey.USER_SITE_HASH + spaceId, userId, DEFAULT_RATE*exist + ERROR_RATE, DEFAULT_UNIT);
    }

    /**
     * 判断 user1 在指定范围内是否覆盖到 user2
     * @param spaceId 两个用户所处的空间 ID
     * @param user1 用户 1
     * @param range 以用户 1 为中心的范围
     * @param user2 用户 2
     * @return 能覆盖到则返回 true
     */
    public boolean canCover(int spaceId, int user1, double range, int user2) {
        // 获取两个用户之间的距离
        Double dist = redissonUtils.geoDist(RedisKey.USER_SITE_HASH + spaceId, user1, user2, DEFAULT_UNIT);
        if (dist == null) {
            return false;
        }
        // 获取方位大小
        range *= DEFAULT_RATE;
        return range - dist > -ERROR_RATE;
    }

    public boolean canCover(double dist, double range) {
        range *= DEFAULT_RATE;
        return range - dist > -ERROR_RATE;
    }
}
