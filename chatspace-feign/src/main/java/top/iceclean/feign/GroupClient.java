package top.iceclean.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import top.iceclean.chatspace.infrastructure.po.Group;
import top.iceclean.chatspace.infrastructure.po.Space;
import top.iceclean.chatspace.infrastructure.po.UserGroup;
import top.iceclean.chatspace.infrastructure.po.Zone;
import top.iceclean.chatspace.infrastructure.pojo.Response;
import top.iceclean.chatspace.infrastructure.vo.ZoneVO;

import java.util.List;
import java.util.Set;

/**
 * 群组远程调用接口
 * @author : Ice'Clean
 * @date : 2022-10-07
 */
@FeignClient(value = "chatspace-group", path = "/space")
public interface GroupClient {
    /**
     * 将用户添加到群聊中
     * @param userId 用户 ID
     * @param groupId 群聊 ID
     * @return 是否添加成功
     */
    @PutMapping("/join")
    boolean joinGroup(@RequestParam int userId, @RequestParam int groupId);

    /**
     * 将用户移出群聊
     * @param userId 用户 ID
     * @param groupId 群聊 ID
     * @return 是否移出成功
     */
    @PutMapping("/exit")
    boolean exitGroup(@RequestParam int userId, @RequestParam int groupId);

    /**
     * 通过群聊 ID 获取群聊实体
     * @param groupId 群聊 ID
     * @return 群聊实体
     */
    @GetMapping("/{groupId}")
    Group getGroupById(@PathVariable int groupId);

    /**
     * 获取用户某条群聊记录
     * @param userId 用户 ID
     * @param groupId 群聊 ID
     * @return 用户群聊记录
     */
    @GetMapping("/user-group")
    UserGroup getUserGroup(@RequestParam int userId, @RequestParam int groupId);

    /**
     * 通过区域 ID 获取区域实体
     * @param zoneId 区域 ID
     * @return 区域实体
     */
    @GetMapping("/zone/{zoneId}")
    Zone getZoneById(@PathVariable int zoneId);

    /**
     * 获取群聊中所有用户 ID
     * @param groupId 群聊 ID
     * @return 所有用户 ID 集合
     */
    @GetMapping("/list/user/id/{groupId}")
    List<Integer> getUserIdList(@PathVariable int groupId);

    /**
     * 获取所有用户管理的群聊
     * @param userId 用户 ID
     * @return 群聊列表
     */
    @GetMapping("/manage/{userId}")
    List<Group> getUserManageGroups(@PathVariable int userId);

    /**
     * 获取用户的群聊主键列表
     * @param userId 用户 ID
     * @return 群聊主键列表
     */
    @GetMapping("/list/group/id/{userId}")
    List<Integer> getGroupIdList(@PathVariable int userId);

    /**
     * 更新用户在群聊会话中最后一条消息的 ID 为最新的
     * @param groupId 群聊 ID
     * @param userId 用户 ID
     * @param latestMsgId 最新消息 ID
     */
    @PutMapping("/last-msg-id")
    void updateLastMsgId(@RequestParam int groupId, @RequestParam int userId, @RequestParam int latestMsgId);

    /**
     * 获取指定空间中的所有区域
     * @param spaceId 空间 ID
     * @return 区域集合
     */
    @GetMapping("/list/zone/vo/{spaceId}/{zonePid}")
    List<ZoneVO> getZoneList(@PathVariable int spaceId, @PathVariable int zonePid);

    /**
     * 通过父级区域 ID 集合获取全部的子区域 ID 集合
     * @param spaceId 空间 ID
     * @param zonePid 父级区域 ID
     * @return 子区域 ID 集合
     */
    @GetMapping("/list/zone/child/id/{spaceId}/{zonePid}")
    List<Integer> getChildZoneIds(@PathVariable int spaceId, @PathVariable int zonePid);

    /**
     * 获取指定空间中的所有区域
     * @param spaceId 空间 ID
     * @return 区域集合
     */
    @GetMapping("/list/zone/{spaceId}")
    List<Zone> getZones(@PathVariable int spaceId);

    /**
     * 获取所有空间的 ID
     * @return 空间 ID 集合
     */
    @GetMapping("/list/space/id")
    List<Integer> getSpaceIdList();

    /**
     * 获取空间元数据
     * @param spaceId 空间 ID
     * @return 空间元数据对象
     */
    @GetMapping("/space/{spaceId}")
    Space getSpaceData(@PathVariable int spaceId);
}
