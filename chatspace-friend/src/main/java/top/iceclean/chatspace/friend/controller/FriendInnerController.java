package top.iceclean.chatspace.friend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.iceclean.chatspace.friend.service.FriendService;
import top.iceclean.chatspace.infrastructure.vo.FriendVO;

import java.util.List;

/**
 * 内部调用的好友服务
 * @author : Ice'Clean
 * @date : 2022-10-10
 */
@RestController
public class FriendInnerController {

    @Autowired
    private FriendService friendService;

    /**
     * 绑定为朋友关系
     * @param userId 发送者用户 ID
     * @param toUserId 目标用户 ID
     * @return 是否成功
     */
    @PutMapping("/become")
    public boolean becomeFriends(int userId, int toUserId) {
        return friendService.becomeFriends(userId, toUserId);
    }

    /**
     * 获取用户在指定好友 ID 中好友的响应对象
     * @param friendId 好友 ID
     * @param userId 当前用户 ID
     * @return 好友响应对象
     */
    @GetMapping("/vo")
    public FriendVO getFriendVO(int friendId, int userId) {
        return friendService.getFriendVO(friendId, userId);
    }

    /**
     * 通过好友 ID 获取好友双方的用户 ID
     * @param friendId 好友 ID
     * @return 用户 ID 集合
     */
    @GetMapping("/ids/{friendId}")
    public List<Integer> getFriendUserId(@PathVariable int friendId) {
        return friendService.getFriendUserId(friendId);
    }

    /**
     * 获取用户所有的好友会话 ID
     * @param userId 用户 ID
     * @return 好友主键列表
     */
    @GetMapping("/list/friend/{userId}")
    List<Integer> getFriendIdList(@PathVariable int userId) {
        return friendService.getFriendIdList(userId);
    }

    /**
     * 获取两个好友之间的好友 ID
     * @param userId1 用户 1
     * @param userId2 用户 2
     * @return 会话 ID
     */
    @GetMapping("/friend")
    Integer getFriendId(int userId1, int userId2) {
        return friendService.getFriendId(userId1, userId2);
    }

    /**
     * 获取用户的好友 ID 列表
     * @param userId 用户 ID
     * @return 好友 ID 列表
     */
    @GetMapping("/list/user/{userId}")
    public List<Integer> getFriendUserIdList(@PathVariable int userId) {
        return friendService.getFriendUserIdList(userId);
    }

    /**
     * 更新用户在好友会话中最后一条消息的 ID 为最新的
     * @param sessionId 会话 ID
     * @param userId 用户 ID
     * @param latestMsgId 最新消息 ID
     */
    @PutMapping("/last-msg-id")
    public void updateLastMsgId(int sessionId, int userId, int latestMsgId) {
        friendService.updateLastMsgId(sessionId, userId, latestMsgId);
    }
}
