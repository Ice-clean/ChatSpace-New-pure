package top.iceclean.chatspace.friend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.iceclean.chatspace.friend.service.FriendService;
import top.iceclean.chatspace.infrastructure.pojo.Response;

/**
 * @author : Ice'Clean
 * @date : 2022-06-09
 */
@RestController
@RequestMapping("/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    /**
     * 获取用户的好友列表
     * @param userId 用户 ID
     * @return 好友列表
     */
    @GetMapping("/list")
    public Object getFriendList(int userId) {
        return friendService.getFriendList(userId);
    }

    /**
     * 删除好友（身份验证为其中一个用户才可删除）
     * @param toUserId 好友 ID
     * @return 是否删除成功
     */
    @DeleteMapping
    public Response deleteFriend(int toUserId) {
        return friendService.deleteFriend(toUserId);
    }
}
