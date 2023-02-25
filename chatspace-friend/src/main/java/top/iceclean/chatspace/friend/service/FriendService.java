package top.iceclean.chatspace.friend.service;


import top.iceclean.chatspace.infrastructure.po.Friend;
import top.iceclean.chatspace.infrastructure.pojo.Response;
import top.iceclean.chatspace.infrastructure.vo.FriendVO;

import java.util.List;

/**
 * @author : Ice'Clean
 * @date : 2022-05-25
 */
public interface FriendService {

    /**
     * 绑定为朋友关系
     * @param userId 发送者用户 ID
     * @param toUserId 目标用户 ID
     * @return 是否绑定成功
     */
    boolean becomeFriends(int userId, int toUserId);

    /**
     * 获取用户在指定会话中的好友关系实体
     * @param friendId 好友 ID
     * @param userId 用户 ID
     * @return 好友关系实体
     */
    Friend getFriend(int friendId, int userId);

    /**
     * 通过好友双方获取好友映射
     * @param userId 主用户 ID
     * @param toUserId 目标好友用户 ID
     * @return 好友关系实体
     */
    Friend getFriendByUser(int userId, int toUserId);

    /**
     * 获取用户在指定好友 ID 中好友的响应对象
     * @param friendId 好友 ID
     * @param userId 当前用户 ID
     * @return 好友的响应对象
     */
    FriendVO getFriendVO(int friendId, int userId);

    /**
     * 通过好友 ID 获取好友双方的用户 ID
     * @param friendId 好友 ID
     * @return 用户 ID 集合
     */
    List<Integer> getFriendUserId(int friendId);

    /**
     * 获取用户所有的好友 ID
     * @param userId 用户 ID
     * @return 好友会话 ID 列表
     */
    List<Integer> getFriendIdList(int userId);

    /**
     * 获取两个好友之间的好友 ID
     * @param userId1 用户 1
     * @param userId2 用户 2
     * @return 会话 ID
     */
    Integer getFriendId(int userId1, int userId2);

    /**
     * 获取用户的好友用户 ID 列表
     * @param userId 用户 ID
     * @return 好友用户 ID 列表
     */
    List<Integer> getFriendUserIdList(int userId);

    /**
     * 更新用户在好友会话中最后一条消息的 ID 为最新的
     * @param friendId 好友 ID
     * @param userId 用户 ID
     * @param latestMsgId 最新消息 ID
     */
    void updateLastMsgId(int friendId, int userId, int latestMsgId);

    /**
     * 获取用户好友列表
     * @param userId 用户 ID
     * @return 好友列表
     */
    Response getFriendList(int userId);

    /**
     * 删除好友（身份验证为其中一个用户才可删除）
     * @param friendId 好友 ID
     * @return 是否删除成功
     */
    Response deleteFriend(int friendId);
}
