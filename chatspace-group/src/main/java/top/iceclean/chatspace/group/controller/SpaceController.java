package top.iceclean.chatspace.group.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.iceclean.chatspace.group.server.SpaceService;
import top.iceclean.chatspace.infrastructure.pojo.Response;

/**
 * @author : Ice'Clean
 * @date : 2022-12-03
 */
@RestController
@RequestMapping("/space")
public class SpaceController {
    /** 空间服务 */
    private final SpaceService spaceService;

    @Autowired
    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    /**
     * 获取空间列表
     * @return 空间列表响应
     */
    @GetMapping("/list")
    public Response getSpaceList() {
        return spaceService.getSpaceList();
    }
}
