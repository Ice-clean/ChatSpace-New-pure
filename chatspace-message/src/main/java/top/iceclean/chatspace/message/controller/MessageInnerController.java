package top.iceclean.chatspace.message.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.iceclean.chatspace.infrastructure.dto.MessageDTO;
import top.iceclean.chatspace.infrastructure.po.Message;
import top.iceclean.chatspace.infrastructure.vo.MessageVO;
import top.iceclean.chatspace.message.service.MessageService;

/**
 * 内部调用的消息服务
 * @author : Ice'Clean
 * @date : 2022-10-09
 */
@RestController
public class MessageInnerController {

    @Autowired
    private MessageService messageService;

    /**
     * 获取指定会话最后一条消息的 ID
     * @param sessionId 会话 ID
     * @return 最后一条消息 ID
     */
    @GetMapping("/last-msg-id/{sessionId}")
    public int getLastMsgId(@PathVariable int sessionId) {
        return messageService.getLastMsgId(sessionId);
    }

    /**
     * 将消息对象转化成消息响应对象
     * @param message 消息对象
     * @param userId 当前用户 ID
     * @param info 是否需要详细信息（会话消息列表需要）
     * @return 消息响应对象
     */
    @GetMapping("/vo")
    public MessageVO toMessageVO(Message message, int userId, boolean info) {
        return messageService.toMessageVO(message, userId, info);
    }
}
