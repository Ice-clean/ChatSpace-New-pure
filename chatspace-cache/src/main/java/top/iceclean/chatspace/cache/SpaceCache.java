package top.iceclean.chatspace.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.iceclean.chatspace.infrastructure.constant.RedisKey;
import top.iceclean.chatspace.infrastructure.po.Site;
import top.iceclean.chatspace.infrastructure.po.Space;
import top.iceclean.chatspace.infrastructure.utils.RedissonUtils;
import top.iceclean.chatspace.infrastructure.vo.SpaceVO;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author : Ice'Clean
 * @date : 2022-12-03
 */
@Component
public class SpaceCache {

    private final RedissonUtils redissonUtils;
    /** 用户所在空间的状态缓存 */
    private final UserCache.State spaceState;
    /** 位置缓存 */
    private final GeoCache geoCache;

    @Autowired
    public SpaceCache(RedissonUtils redissonUtils, GeoCache geoCache) {
        this.redissonUtils = redissonUtils;
        this.spaceState = UserCache.getIntState(RedisKey.SPACE_SESSION, redissonUtils, UserCache.State.Type.INT);
        this.geoCache = geoCache;
    }

    /** 用户进入空间 */
    public void spaceIn(int spaceId, int userId) {
        // 将用户 ID 缓存到指定区域，并标记缓存
        redissonUtils.setAdd(RedisKey.SPACE_USER_SET + spaceId, userId);
        spaceState.set(userId, spaceId);
    }

    /** 用户离开空间 */
    public void spaceOut(int spaceId, int userId) {
        // 将用户从区域中移除，并清除标记
        redissonUtils.setRemove(RedisKey.SPACE_USER_SET + spaceId, userId);
        spaceState.clean(userId);
    }

    /** 判断用户是否在指定空间中 */
    public boolean containsUser(int spaceId, int userId) {
        return redissonUtils.setContains(RedisKey.SPACE_USER_SET + spaceId, userId);
    }

    /** 获取用户所在的空间 */
    public int getUserSpaceId(int userId) {
        System.out.println(userId + " -> " + spaceState.get(userId));
        return spaceState.get(userId);
    }

    /** 获取用户当前的位置（哪个空间，哪个坐标） */
    public Site getUserSite(int userId) {
        return geoCache.getUserSite(getUserSpaceId(userId), userId);
    }

    /** 获取空间中的所有用户 */
    public Set<Integer> getSpaceUsers(int spaceId) {
        return redissonUtils.setGet(RedisKey.SPACE_USER_SET + spaceId)
                .stream().map(id -> Integer.parseInt(id.toString())).collect(Collectors.toSet());
    }

    /** 获取空间的用户数量 */
    public int onlineUserNum(int spaceId) {
        return redissonUtils.setSize(RedisKey.SPACE_USER_SET + spaceId);
    }

    /**
     * 将空间包装为空间响应对象，添加了空间的在线人数
     * @param space 空间实体
     * @return 添加了空间在线人数的响应对象
     */
    public SpaceVO wrapSpace(Space space) {
        return new SpaceVO(space, onlineUserNum(space.getId()));
    }
}
