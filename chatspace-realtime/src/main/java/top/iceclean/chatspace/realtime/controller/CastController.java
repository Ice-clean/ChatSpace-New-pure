package top.iceclean.chatspace.realtime.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.iceclean.chatspace.infrastructure.annotation.AdminCheck;
import top.iceclean.chatspace.infrastructure.constant.ResponseStatusEnum;
import top.iceclean.chatspace.infrastructure.constant.WsType;
import top.iceclean.chatspace.infrastructure.pojo.Response;
import top.iceclean.chatspace.realtime.common.DataGenerator;
import top.iceclean.chatspace.realtime.processor.message.BaseProcessor;
import top.iceclean.chatspace.realtime.processor.message.SpaceProcessor;

/**
 * @author : Ice'Clean
 * @date : 2022-12-03
 */
@RestController
@RequestMapping("/cast")
public class CastController {
    /** 空间消息处理 */
    private final SpaceProcessor spaceProcessor;

    @Autowired
    public CastController(SpaceProcessor spaceProcessor) {
        this.spaceProcessor = spaceProcessor;
    }

    /**
     * 发送空间广播消息
     * @param msg 消息内容
     * @return 响应
     */
    @PostMapping("/space")
    public Response castSpaceMessage(String msg) {
        return spaceProcessor.castSpaceMessage(msg);
    }

    /**
     * 发送世界消息
     * @param msg 消息内容
     * @return 响应
     */
    @PostMapping("/universe")
    public Response castUniverseMessage(String msg) {
        return spaceProcessor.castUniverseMessage(msg);
    }

    /**
     * 管理员直接发送空间广播消息
     * @param msg 消息内容
     * @return 响应
     */
    @AdminCheck
    @PostMapping("/admin/space/{spaceId}")
    public Response adminCastSpaceMessage(@PathVariable int spaceId, String msg) {
        BaseProcessor.castMessage(WsType.SEND_MESSAGE,
                new DataGenerator.SpaceCast(spaceId, msg));
        return new Response(ResponseStatusEnum.OK);
    }

    /**
     * 管理员直接发送世界消息
     * @param msg 消息内容
     * @return 响应
     */
    @AdminCheck
    @PostMapping("/admin/universe")
    public Response adminCastUniverseMessage(String msg) {
        BaseProcessor.castMessage(WsType.SEND_MESSAGE,
                new DataGenerator.UniverseCast(msg));
        return new Response(ResponseStatusEnum.OK);
    }
}
