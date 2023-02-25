package top.iceclean.chatspace.session.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.iceclean.chatspace.infrastructure.constant.SessionType;
import top.iceclean.chatspace.infrastructure.po.Session;
import top.iceclean.chatspace.infrastructure.vo.SessionVO;
import top.iceclean.chatspace.session.service.SessionService;

import java.util.List;

/**
 * 内部调用的会话服务
 * @author : Ice'Clean
 * @date : 2022-10-10
 */
@RestController
public class SessionInnerController {

    @Autowired
    private SessionService sessionService;

    /**
     * 创建会话
     * @param type 会话类型
     * @return 新建会话对象
     */
    @PutMapping("")
    public Session createSession(SessionType type, int targetId) {
        return sessionService.createSession(type, targetId);
    }

    /**
     * 通过会话 ID 获取简单的会话响应对象
     * @param sessionId 会话 ID
     * @return 会话响应对象
     */
    @GetMapping("/vo")
    public SessionVO getSessionVO(int sessionId) {
        return sessionService.getSessionVO(sessionId);
    }

    /**
     * 通过会话 ID 和当前用户 ID 获取详细的会话响应对象
     * @param sessionId 会话 ID
     * @param userId 用户 ID
     * @return 详细的会话响应对象
     */
    @GetMapping("/vo/user")
    public SessionVO getSessionVO(int sessionId, int userId) {
        return sessionService.getSessionVO(sessionId, userId);
    }

    /**
     * 通过会话类型和目标 ID 寻找对应会话的 ID
     * @param type 会话类型
     * @param targetId 目标 ID
     * @return 对应会话的 ID
     */
    @GetMapping("/find/id")
    public Integer findSessionId(int type, int targetId) {
        return sessionService.findSessionId(type, targetId);
    }

    /**
     * 更新用户在会话中最后一条消息的 ID 为最新的
     * @param sessionId 会话 ID
     * @param userId 用户 ID
     */
    @PutMapping("/last-msg-id")
    public void updateLastMsgId(int sessionId, int userId) {
        sessionService.updateLastMsgId(sessionId, userId);
    }
}
