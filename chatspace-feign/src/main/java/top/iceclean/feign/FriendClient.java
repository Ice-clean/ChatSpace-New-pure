package top.iceclean.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import top.iceclean.chatspace.infrastructure.vo.FriendVO;

import java.util.List;

/**
 * @author : Ice'Clean
 * @date : 2022-10-09
 */
@FeignClient(value = "chatspace-friend", path = "/space")
public interface FriendClient {
    /**
     * 绑定为朋友关系
     * @param userId 发送者用户 ID
     * @param toUserId 目标用户 ID
     * @return 是否成功
     */
    @PutMapping("/become")
    boolean becomeFriends(@RequestParam int userId, @RequestParam int toUserId);

    /**
     * 获取用户在指定会话中的好友
     * @param friendId 好友 ID
     * @param userId 当前用户 ID
     * @return 好友响应对象
     */
    @GetMapping("/vo")
    FriendVO getFriendVO(@RequestParam int friendId, @RequestParam int userId);

    /**
     * 通过好友 ID 获取好友双方的用户 ID
     * @param friendId 好友 ID
     * @return 用户 ID 集合
     */
    @GetMapping("/ids/{friendId}")
    List<Integer> getFriendUserId(@PathVariable int friendId);

    /**
     * 获取用户所有的好友会话 ID
     * @param userId 用户 ID
     * @return 好友主键列表
     */
    @GetMapping("/list/friend/{userId}")
    List<Integer> getFriendIdList(@PathVariable int userId);

    /**
     * 获取两个好友之间的好友 ID
     * @param userId1 用户 1
     * @param userId2 用户 2
     * @return 会话 ID
     */
    @GetMapping("/friend")
    Integer getFriendId(@RequestParam int userId1, @RequestParam int userId2);

    /**
     * 获取用户的好友 ID 列表
     * @param userId 用户 ID
     * @return 好友 ID 列表
     */
    @GetMapping("/list/user/{userId}")
    List<Integer> getFriendUserIdList(@PathVariable int userId);

    /**
     * 更新用户在好友会话中最后一条消息的 ID 为最新的
     * @param friendId 好友 ID
     * @param userId 用户 ID
     * @param latestMsgId 最新消息 ID
     */
    @PutMapping("/last-msg-id")
    void updateLastMsgId(@RequestParam int friendId, @RequestParam int userId, @RequestParam int latestMsgId);
}
