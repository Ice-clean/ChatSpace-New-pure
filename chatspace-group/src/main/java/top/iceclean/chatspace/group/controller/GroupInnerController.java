package top.iceclean.chatspace.group.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.iceclean.chatspace.group.server.GroupService;
import top.iceclean.chatspace.group.server.SpaceService;
import top.iceclean.chatspace.group.server.ZoneService;
import top.iceclean.chatspace.infrastructure.po.Group;
import top.iceclean.chatspace.infrastructure.po.Space;
import top.iceclean.chatspace.infrastructure.po.UserGroup;
import top.iceclean.chatspace.infrastructure.po.Zone;
import top.iceclean.chatspace.infrastructure.vo.ZoneVO;

import java.util.List;
import java.util.Set;

/**
 * 内部调用的群组服务
 * @author : Ice'Clean
 * @date : 2022-10-09
 */
@RestController
public class GroupInnerController {

    private final GroupService groupService;
    private final ZoneService zoneService;
    private final SpaceService spaceService;

    public GroupInnerController(GroupService groupService, ZoneService zoneService, SpaceService spaceService) {
        this.groupService = groupService;
        this.zoneService = zoneService;
        this.spaceService = spaceService;
    }

    /**
     * 将用户添加到群聊中
     * @param userId 用户 ID
     * @param groupId 群聊 ID
     * @return 是否添加成功
     */
    @PutMapping("/join")
    public Boolean joinGroup(int userId, int groupId) {
        return groupService.joinGroup(userId, groupId);
    }

    /**
     * 将用户移出群聊
     * @param userId 用户 ID
     * @param groupId 群聊 ID
     * @return 是否移出成功
     */
    @PutMapping("/exit")
    public Boolean exitGroup(int userId, int groupId) {
        return groupService.exitGroup(userId, groupId);
    }

    /**
     * 通过群聊 ID 获取群聊实体
     * @param groupId 群聊 ID
     * @return 群聊实体
     */
    @GetMapping("/{groupId}")
    public Group getGroupById(@PathVariable int groupId) {
        return groupService.getGroupById(groupId);
    }

    /**
     * 获取用户某条群聊记录
     * @param userId 用户 ID
     * @param groupId 群聊 ID
     * @return 用户群聊记录
     */
    @GetMapping("/user-group")
    UserGroup getUserGroup(@RequestParam int userId, @RequestParam int groupId) {
        return groupService.getUserGroup(userId, groupId);
    }

    /**
     * 通过区域 ID 获取区域实体
     * @param zoneId 区域 ID
     * @return 区域实体
     */
    @GetMapping("/zone/{zoneId}")
    public Zone getZoneById(@PathVariable int zoneId) {
        return zoneService.getZoneById(zoneId);
    }

    /**
     * 获取所有用户管理的群聊
     * @param userId 用户 ID
     * @return 群聊列表
     */
    @GetMapping("/manage/{userId}")
    public List<Group> getUserManageGroups(@PathVariable int userId) {
        return groupService.getUserManageGroups(userId);
    }

    /**
     * 获取群聊中所有用户 ID
     * @param groupId 群聊 ID
     * @return 所有用户 ID 集合
     */
    @GetMapping("/list/user/id/{groupId}")
    public List<Integer> getUserIdList(@PathVariable int groupId) {
        return groupService.getUserIdList(groupId);
    }

    /**
     * 获取用户的群聊主键列表
     * @param userId 用户 ID
     * @return 群聊主键列表
     */
    @GetMapping("/list/group/id/{userId}")
    public List<Integer> getGroupIdList(@PathVariable int userId) {
        return groupService.getGroupIdList(userId);
    }

    /**
     * 更新用户在群聊会话中最后一条消息的 ID 为最新的
     * @param groupId 群聊 ID
     * @param userId 用户 ID
     * @param latestMsgId 最新消息 ID
     */
    @PutMapping("/last-msg-id")
    public void updateLastMsgId(int groupId, int userId, int latestMsgId) {
        groupService.updateLastMsgId(groupId, userId, latestMsgId);
    }

    /**
     * 获取指定空间中指定嵌套区域的所有区域
     * @param spaceId 空间 ID
     * @return 区域集合
     */
    @GetMapping("/list/zone/vo/{spaceId}/{zonePid}")
    public List<ZoneVO> getZoneList(@PathVariable int spaceId, @PathVariable int zonePid) {
        return zoneService.getZoneList(spaceId, zonePid);
    }

    /**
     * 通过父级区域 ID 集合获取全部的子区域 ID 集合
     * @param spaceId 空间 ID
     * @param zonePid 父级区域 ID
     * @return 子区域 ID 集合
     */
    @GetMapping("/list/zone/child/id/{spaceId}/{zonePid}")
    public List<Integer> getChildZoneIds(@PathVariable int spaceId, @PathVariable Integer zonePid) {
        return zoneService.getChildZoneIds(spaceId, zonePid);
    }

    /**
     * 获取指定空间中的所有区域
     * @param spaceId 空间 ID
     * @return 区域集合
     */
    @GetMapping("/list/zone/{spaceId}")
    public List<Zone> getZones(@PathVariable int spaceId) {
        return zoneService.getZones(spaceId);
    }

    /**
     * 获取所有空间的 ID
     * @return 空间 ID 集合
     */
    @GetMapping("/list/space/id")
    public List<Integer> getSpaceIdList() {
        return spaceService.getSpaceIdList();
    }

    /**
     * 获取空间元数据
     * @param spaceId 空间 ID
     * @return 空间元数据对象
     */
    @GetMapping("/space/{spaceId}")
    public Space getSpaceData(@PathVariable int spaceId) {
        return spaceService.getSpaceData(spaceId);
    }
}
