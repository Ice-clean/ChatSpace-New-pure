package top.iceclean.chatspace.cache;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.iceclean.chatspace.infrastructure.constant.RedisKey;
import top.iceclean.chatspace.infrastructure.po.Group;
import top.iceclean.chatspace.infrastructure.utils.RedissonUtils;
import top.iceclean.chatspace.infrastructure.vo.GroupVO;
import top.iceclean.feign.GroupClient;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 群组相关缓存
 * @author : Ice'Clean
 * @date : 2022-11-27
 */
@Component
public class GroupCache {

    private final RedissonUtils redissonUtils;
    private final GroupClient groupClient;

    @Autowired
    public GroupCache(RedissonUtils redissonUtils, GroupClient groupClient) {
        this.redissonUtils = redissonUtils;
        this.groupClient = groupClient;
    }

    /**
     * 根据群组 ID 获取群组实体
     * 缓存中找不到时，则去数据库查找并回种
     * @param groupId 群组 ID
     * @return 群组实体
     */
    public Group getGroup(int groupId) {
        // 先在缓存中获取
        Object groupCache = redissonUtils.hashGet(RedisKey.GROUP_HASH, groupId);
        if (groupCache == null) {
            // 缓存中不存在，则访问服务获取
            Group group = groupClient.getGroupById(groupId);
            // 然后回种缓存并返回
            redissonUtils.hashSet(RedisKey.GROUP_HASH, groupId, JSON.toJSONString(group));
            return group;
        }

        // 缓存中存在，直接反序列化后返回
        return JSON.parseObject(groupCache.toString(), Group.class);
    }

    /**
     * 根据群组 ID 获取群组响应对象
     * 直接通过缓存构建
     * @param groupId 群组 ID
     * @return 群组响应对象
     */
    public GroupVO getGroupVO(int groupId) {
        return new GroupVO(getGroup(groupId), onlineUserNum(groupId));
    }

    /**
     * 根据群组 ID 和用户 ID 获取有该用户信息的群组响应对象
     * 简单响应对象直接通过缓存构建，用户群聊信息通过查库
     * @param groupId 群组 ID
     * @param userId 用户 ID
     * @return 用户的群组响应对象
     */
    public GroupVO getUserGroupVO(int groupId, int userId) {
        GroupVO groupVO = getGroupVO(groupId);
        return groupVO.setUserGroup(groupClient.getUserGroup(userId, groupId));
    }

    /** 群组中的用户上线 */
    public void setUserOnline(int groupId, int userId, boolean online) {
        // 上线则加入集合
        if (online) {
            redissonUtils.setAdd(RedisKey.GROUP_USER_SET + groupId, userId);
            return;
        }
        // 否则从集合中移出
        redissonUtils.setRemove(RedisKey.GROUP_USER_SET + groupId, userId);
    }

    /** 获取群组中的在线人数 */
    public int onlineUserNum(int groupId) {
        return redissonUtils.setSize(RedisKey.GROUP_USER_SET + groupId);
    }

    /** 判断用户是否在指定群组中 */
    public boolean containsUser(int groupId, int userId) {
        return redissonUtils.setContains(RedisKey.GROUP_USER_SET + groupId, userId);
    }

    /**
     * 获取群组中所有的在线用户
     * @param groupId 群组 ID
     * @return 群组中所有在线用户的 ID 集合
     */
    public Set<Integer> getGroupUsers(int groupId) {
        return redissonUtils.setGet(RedisKey.GROUP_USER_SET + groupId)
                .stream().map(id -> Integer.parseInt(id.toString())).collect(Collectors.toSet());
    }
}
