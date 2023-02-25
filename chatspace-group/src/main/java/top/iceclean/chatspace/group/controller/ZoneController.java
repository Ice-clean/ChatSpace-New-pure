package top.iceclean.chatspace.group.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.iceclean.chatspace.cache.SpaceCache;
import top.iceclean.chatspace.cache.ZoneCache;
import top.iceclean.chatspace.group.server.BoxService;
import top.iceclean.chatspace.group.server.ZoneService;
import top.iceclean.chatspace.infrastructure.annotation.AdminCheck;
import top.iceclean.chatspace.infrastructure.auth.UserAuth;
import top.iceclean.chatspace.infrastructure.constant.ZoneType;
import top.iceclean.chatspace.infrastructure.dto.ZoneDTO;
import top.iceclean.chatspace.infrastructure.po.Site;
import top.iceclean.chatspace.infrastructure.po.Zone;
import top.iceclean.chatspace.infrastructure.pojo.Response;

/**
 * @author : Ice'Clean
 * @date : 2022-12-03
 */
@RestController
@RequestMapping("/zone")
public class ZoneController {
    /** 区域服务 */
    private final ZoneService zoneService;
    /** 区域缓存 */
    private final ZoneCache zoneCache;

    public ZoneController(ZoneService zoneService, ZoneCache zoneCache) {
        this.zoneService = zoneService;
        this.zoneCache = zoneCache;
    }

    /**
     * 划分出一块私有区域
     * @param zoneDTO 区域信息
     */
    @PostMapping("/private")
    public Response createPrivateZone(ZoneDTO zoneDTO) {
        return zoneService.checkedCreateZone(zoneDTO);
    }

    /**
     * 直接指定区域实体创建区域
     * @param zone 区域实体
     * @return 是否创建成功
     */
    @PostMapping("/direct")
    @AdminCheck
    public Response directCrateZone(Zone zone) {
        return zoneService.createZone(zone);
    }

    /**
     * 获取群组中的所有区域
     * @param groupId 群组 ID
     * @return 所有的区域集合
     */
    @GetMapping("/list/zone")
    public Response getGroupZoneList(int groupId) {
        return zoneService.getGroupZoneList(groupId);
    }

    @DeleteMapping("")
    public Response deleteZone(int zoneId) {
        return zoneService.deleteZone(zoneCache.getZone(zoneId));
    }
}
