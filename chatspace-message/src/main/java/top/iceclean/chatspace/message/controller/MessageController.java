package top.iceclean.chatspace.message.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.iceclean.chatspace.message.service.MessageService;

/**
 * 消息服务
 * 这里的消息服务，url 使用的是 session 前缀，因为获取的资源是 session 资源
 * @author : Ice'Clean
 * @date : 2022-10-10
 */
@RestController
@RequestMapping("/session")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 获取用户在某个会话的历史消息
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param page 聊天记录的页数
     * @return 历史消息列表
     */
    @GetMapping("/history")
    public Object getChatHistory(int userId, int sessionId, int page) {
        return messageService.getChatHistory(userId, sessionId, page);
    }
}
