package top.iceclean.chatspace.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.iceclean.chatspace.infrastructure.constant.RedisKey;
import top.iceclean.chatspace.infrastructure.po.User;
import top.iceclean.chatspace.infrastructure.utils.RedissonUtils;
import top.iceclean.chatspace.infrastructure.vo.UserVO;
import top.iceclean.feign.FriendClient;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户相关缓存
 * @author : Ice'Clean
 * @date : 2022-11-28
 */
@Component
public class UserCache {

    /** 用户各种整型状态的缓存，给外部用 */
    public static class State {
        /** 指定类型，用于序列化 */
        enum Type {
            INT, STRING;
        }

        /** 状态名称 */
        private final String stateName;
        /** 缓存操作 */
        private final RedissonUtils redissonUtils;
        /** 所属类型 */
        private final Type type;

        public State(String stateName, RedissonUtils redissonUtils, Type type) {
            this.stateName = stateName;
            this.redissonUtils = redissonUtils;
            this.type = type;
        }

        /** 设置指定用户该状态的当前值 */
        public void set(int userId, Object value) {
            if (value != null) {
                redissonUtils.hashSet(RedisKey.USER_LOGIN_HASH + userId, stateName, value);
            }
        }

        /** 获取指定用户该状态的当前值 */
        public Integer get(int userId) {
            if (type != Type.INT) {
                throw new IllegalStateException(type.name() + "类型，不允许获取 INT 类型的值");
            }
            Object value = redissonUtils.hashGet(RedisKey.USER_LOGIN_HASH + userId, stateName);
            return value == null ? null : Integer.parseInt(value.toString());
        }

        public String getString(int userId) {
            if (type != Type.STRING) {
                throw new IllegalStateException(type.name() + "类型，不允许获取 STRING 类型的值");
            }
            Object value = redissonUtils.hashGet(RedisKey.USER_LOGIN_HASH + userId, stateName);
            return value == null ? null : value.toString();
        }

        /** 清除指定用户的该状态 */
        public void clean(int userId) {
            redissonUtils.hashDel(RedisKey.USER_LOGIN_HASH + userId, stateName);
        }
    }

    private final RedissonUtils redissonUtils;
    private FriendClient friendClient;

    /** 各种状态缓存 */
    private final State friendSessionState;
    private final State userBoxState;
    private final State serverNodeState;

    public UserCache(RedissonUtils redissonUtils) {
        this.redissonUtils = redissonUtils;
        this.friendSessionState = getIntState(RedisKey.FRIEND_SESSION, redissonUtils, State.Type.INT);
        this.userBoxState = getIntState(RedisKey.USER_BOX, redissonUtils, State.Type.INT);
        this.serverNodeState = getIntState(RedisKey.SERVER_NODE, redissonUtils, State.Type.STRING);
    }

    @Autowired
    public void setFriendClient(FriendClient friendClient) {
        this.friendClient = friendClient;
    }

    /** 获取一个用户状态缓存操作对象 */
    public static State getIntState(String stateName, RedissonUtils redissonUtils, State.Type type) {
        return new State(stateName, redissonUtils, type);
    }

    /** 用户登录缓存 */
    public void cachedLogin(User user, String token, String authorityString) {
        String loginKey = RedisKey.USER_LOGIN_HASH + user.getId();
        // 首先删除之前可能遗留的缓存
        redissonUtils.delete(loginKey);
        // 设置缓存状态
        redissonUtils.hashSet(loginKey, RedisKey.TOKEN, token);
        redissonUtils.hashSet(loginKey, RedisKey.AUTHORITIES, authorityString);
        userBoxState.set(user.getId(), user.getBoxId());
        redissonUtils.expire(loginKey, 3, TimeUnit.HOURS);
    }

    /** 获取用户登录时的 token */
    public String getToken(int userId) {
        Object tokenCache = redissonUtils.hashGet(RedisKey.USER_LOGIN_HASH + userId, RedisKey.TOKEN);
        return tokenCache == null ? null : tokenCache.toString();
    }

    /** 获取用户登陆时的权限 */
    public String getAuthorityString(String userId) {
        Object authorityCache = redissonUtils.hashGet(RedisKey.USER_LOGIN_HASH + userId, RedisKey.AUTHORITIES);
        return authorityCache == null ? null : authorityCache.toString();
    }

    /** 获取用户随身空间箱的 ID */
    public Integer getUserBoxId(int userId) {
        return userBoxState.get(userId);
    }

    /** 获取验证码缓存 */
    public String getCaptcha(String userName) {
        Object code = redissonUtils.get(RedisKey.USER_CODE + userName);
        if (code != null) {
            return code.toString();
        }
        return null;
    }

    /**
     * 设置用户验证码
     * @param userName 用户注册的用户名名
     * @param content 验证码内容（邮箱+六位验证码）绑定
     * @param expire 过期时间（单位：分钟）
     */
    public void setCaptcha(String userName, String content, int expire) {
        redissonUtils.set(RedisKey.USER_CODE + userName, content, expire, TimeUnit.MINUTES);
    }

    /** 设置用户上下线标记 */
    public void setOnline(int userId, boolean online) {
        redissonUtils.setBit(RedisKey.USER_ONLINE_BIT, userId, online);
    }

    /** 判断用户是否在线 */
    public boolean isOnline(int userId) {
        return redissonUtils.getBit(RedisKey.USER_ONLINE_BIT, userId);
    }

    /** 获取系统在先总人数 */
    public long onlineUserNum() {
        return redissonUtils.countBit(RedisKey.USER_ONLINE_BIT);
    }

    /** 获取所有在线的用户 */
    public Set<Integer> getAllUsers() {
        return redissonUtils.getBitValueSet(RedisKey.USER_ONLINE_BIT);
    }

    /**
     * 将用户包装为用户响应对象，添加了用户的在线状态
     * @param user 用户实体
     * @return 添加了用户在线状态的响应对象
     */
    public UserVO wrapUser(User user) {
        return new UserVO(user, isOnline(user.getId()));
    }

    /** 获取两个用户的会话 ID */
    public Integer getFriendSession(int userId1, int userId2) {
        Integer friendId = friendClient.getFriendId(userId1, userId2);
        // 将 sessionId 所对应的两个用户的映射缓存起来，加速后面的发送消息
        redissonUtils.hashSet(RedisKey.FRIEND_USER_HASH, friendId, userId1 + "-" + userId2);
        // 同时在两个用户的缓存中，设置该好友会话，便于后期的查询
        friendSessionState.set(userId1, friendId);
        friendSessionState.set(userId2, friendId);
        return friendId;
    }

    /** 获取用户当前所在的好友会话 */
    public Integer getUserFriendSession(int userId) {
        return friendSessionState.get(userId);
    }

    /** 移除好友会话缓存 */
    public void removeFriendSession(int friendId) {
        // 获取好友会话中的用户，并清除他们缓存中的好友会话
        getFriendSessionUsers(friendId).forEach(friendSessionState::clean);
        // 将好友会话移除
        redissonUtils.hashDel(RedisKey.FRIEND_USER_HASH, friendId);
    }

    /** 获取好友会话的用户 */
    public Set<Integer> getFriendSessionUsers(int friendId) {
        Object usersCache = redissonUtils.hashGet(RedisKey.FRIEND_USER_HASH, friendId);
        if (usersCache == null) {
            // 为空的话，重新查询出来
            HashSet<Integer> friendIds = new HashSet<>(friendClient.getFriendUserIdList(friendId));
            // 将其缓存起来
            redissonUtils.hashSet(RedisKey.FRIEND_USER_HASH, friendId,
                    friendIds.stream().map(Object::toString).collect(Collectors.joining("-")));
            return friendIds;
        }
        // 否则直接返回
        return Arrays.stream(usersCache.toString().split("-"))
                .map(Integer::parseInt).collect(Collectors.toSet());
    }

    /** 获取用户所在节点 */
    public String getServerNode(int userId) {
        return serverNodeState.getString(userId);
    }

    public void setServerNode(int userId, String serverId) {
        serverNodeState.set(userId, serverId);
    }
}
