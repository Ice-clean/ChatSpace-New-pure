package top.iceclean.chatspace.group.controller;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.iceclean.chatspace.group.server.GroupService;
import top.iceclean.chatspace.infrastructure.pojo.Response;

/**
 * @author : Ice'Clean
 * @date : 2022-06-09
 */
@RestController
@RequestMapping("/group")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Data
    private static class GroupNameBody {
        private String groupName;
    }

    /**
     * 创建群聊
     * @param groupBody 群聊名称
     * @return 返回结果
     */
    @PostMapping("")
    public Response createGroup(GroupNameBody groupBody) {
        return groupService.createGroup(groupBody.getGroupName());
    }

    /**
     * 删除一个群组
     * @param groupId 群组 ID
     * @return 删除结果
     */
    @DeleteMapping("")
    public Response deleteGroup(int groupId) {
        return null;
    }

    /**
     * 获取用户创建的所有群聊
     * @param userId 用户 ID
     * @return 由用户创建的所有群组
     */
    @GetMapping("/creator/{userId}")
    public Response getUserCreateGroups(@PathVariable int userId) {
        return groupService.getUserCreateGroups(userId);
    }

    /**
     * 获取群聊列表
     * @param userId 用户 ID
     * @return 群聊列表
     */
    @GetMapping("/list")
    public Response getGroupList(int userId) {
        return groupService.getGroupList(userId);
    }

    /**
     * 获取群聊中的所有用户
     * @param groupId 群聊 ID
     * @return 所有用户集合
     */
    @GetMapping("/list/user")
    public Response getUserList(int groupId) {
        return groupService.getUserList(groupId);
    }
}
