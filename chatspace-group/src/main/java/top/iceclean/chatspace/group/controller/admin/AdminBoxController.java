package top.iceclean.chatspace.group.controller.admin;

import org.springframework.web.bind.annotation.*;
import top.iceclean.chatspace.group.server.BoxService;
import top.iceclean.chatspace.infrastructure.annotation.AdminCheck;
import top.iceclean.chatspace.infrastructure.constant.ItemType;
import top.iceclean.chatspace.infrastructure.constant.ResponseStatusEnum;
import top.iceclean.chatspace.infrastructure.constant.ZoneType;
import top.iceclean.chatspace.infrastructure.dto.ZoneDTO;
import top.iceclean.chatspace.infrastructure.po.Box;
import top.iceclean.chatspace.infrastructure.po.Site;
import top.iceclean.chatspace.infrastructure.po.Zone;
import top.iceclean.chatspace.infrastructure.pojo.Response;

import java.util.Map;

/**
 * 管理员的箱子区域服务
 * @author : Ice'Clean
 * @date : 2022-12-16
 */
@RestController
@RequestMapping("/zone/admin/box")
public class AdminBoxController {

    /** 箱子区域服务 */
    private final BoxService boxService;

    public AdminBoxController(BoxService boxService) {
        this.boxService = boxService;
    }

    /** 创建箱子区域 */
    @AdminCheck
    @PostMapping("")
    public Response createBoxZone(Zone zone) {
        Response createResp = boxService.createBoxZone(zone);
        if (createResp.isOk()) {
            return createResp.setMsg("管理员创建箱子成功")
                    .addData("zone", zone);
        }
        return createResp;
    }

    /** 查看箱子内容 */
    @AdminCheck
    @GetMapping("/list/item/{boxId}")
    public Response getBoxItemList(@PathVariable int boxId) {
        return new Response(ResponseStatusEnum.OK)
                .setData(boxService.getBoxItemList(boxId));
    }
}
