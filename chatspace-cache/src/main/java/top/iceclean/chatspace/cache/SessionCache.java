package top.iceclean.chatspace.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.iceclean.chatspace.infrastructure.constant.RedisKey;
import top.iceclean.chatspace.infrastructure.constant.SessionType;
import top.iceclean.chatspace.infrastructure.utils.RedissonUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 会话相关缓存
 * @author : Ice'Clean
 * @date : 2022-11-28
 */
@Component
public class SessionCache {

    private final RedissonUtils redissonUtils;
    private final UserCache userCache;
    private final GroupCache groupCache;
    private final ZoneCache zoneCache;
    private final SpaceCache spaceCache;
    /** 用户的临时会话状态缓存 */
    private final UserCache.State tempSessionState;

    @Autowired
    public SessionCache(RedissonUtils redissonUtils, UserCache userCache, GroupCache groupCache, ZoneCache zoneCache, SpaceCache spaceCache) {
        this.redissonUtils = redissonUtils;
        this.userCache = userCache;
        this.groupCache = groupCache;
        this.zoneCache = zoneCache;
        this.tempSessionState = UserCache.getIntState(RedisKey.TEMP_SESSION, redissonUtils, UserCache.State.Type.INT);
        this.spaceCache = spaceCache;
    }

    /**
     * 新建一个临时会话
     * @param userIds 进入临时会话的用户 ID 数组
     * @return 临时会话 ID（负值）
     */
    public int newTempSession(Integer ... userIds) {
        // 临时会话 ID 采用负值自减的方式
        int tempSessionId = (int) redissonUtils.decrementAndGetLog(RedisKey.TEMP_SESSION_ID);
        // 将用户放入该临时会话中
        redissonUtils.setAddAll(RedisKey.TEMP_USER_SET + tempSessionId, Arrays.asList(userIds));
        // 在每个用户的缓存中设置该临时会话，表示他们处于该会话中
        Arrays.stream(userIds).forEach(userId -> tempSessionState.set(userId, tempSessionId));
        // 返回临时会话 ID
        return tempSessionId;
    }

    /** 获取用户当前所在的临时会话 */
    public Integer getUserTempSession(int userId) {
        return tempSessionState.get(userId);
    }

    /**
     * 获取一个临时会话的所有用户 ID
     * @param tempSessionId 临时会话 ID
     * @return 临时会话中的用户 ID 集合
     */
    public Set<Integer> getTempSessionUsers(long tempSessionId) {
        return redissonUtils.setGet(RedisKey.TEMP_USER_SET + tempSessionId)
                .stream().map(id -> Integer.parseInt(id.toString())).collect(Collectors.toSet());
    }

    /**
     * 将用户从指定临时会话中移出，当会话中没有用户时，移除会话
     * @param userId 用户 Id
     * @param tempSessionId 目标临时会话
     * @return 移除成功的话，返回会话剩下的成员，否则返回 null
     */
    public Set<Integer> removeUserFromTempSession(int userId, long tempSessionId) {
        // 先清除用户的临时会话缓存
        tempSessionState.clean(userId);
        // 若移除用户成功，则返回会话中剩下的用户，并在会话中没有用户时移除会话
        if (redissonUtils.setRemove(RedisKey.TEMP_USER_SET + tempSessionId, userId)) {
            Set<Integer> tempSessionUsers = getTempSessionUsers(tempSessionId);
            if (tempSessionUsers.isEmpty()) {
                redissonUtils.delete(RedisKey.TEMP_USER_SET + tempSessionId);
            }
            return tempSessionUsers;
        }
        // 否则用户步在会话中，返回空集
        return new HashSet<>(0);
    }

    /**
     * 通过会话类型和对应的目标 ID，获取会话中所有的在线用户
     * @param sessionType 会话类型
     * @param targetId 会话 ID
     * @return 会话中所有在线用户的 ID 集合
     */
    public Set<Integer> getSessionUsers(SessionType sessionType, int targetId) {
        // 通过类型去不同的缓存中获取
        Set<Integer> userIdSet;
        switch (sessionType) {
            case TEMP: userIdSet = getTempSessionUsers(targetId); break;
            case FRIEND: userIdSet = userCache.getFriendSessionUsers(targetId); break;
            case GROUP: userIdSet = groupCache.getGroupUsers(targetId); break;
            case ZONE: userIdSet = zoneCache.getZoneUsers(targetId); break;
            case SPACE: userIdSet = spaceCache.getSpaceUsers(targetId); break;
            case UNIVERSE: userIdSet = userCache.getAllUsers(); break;
            default: userIdSet = new HashSet<>(0);
        }
        return userIdSet;
    }
}
