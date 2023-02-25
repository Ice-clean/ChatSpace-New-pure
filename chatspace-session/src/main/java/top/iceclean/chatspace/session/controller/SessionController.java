package top.iceclean.chatspace.session.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.iceclean.chatspace.infrastructure.constant.ResponseStatusEnum;
import top.iceclean.chatspace.infrastructure.pojo.Response;
import top.iceclean.chatspace.session.service.SessionService;

/**
 * @author : Ice'Clean
 * @date : 2022-05-25
 */
@RestController
@RequestMapping("session")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    /**
     * 通过会话 ID 和当前用户 ID 获取会话详情
     * @param sessionId 会话 ID
     * @param userId 用户 ID
     * @return 会话响应对象
     */
    @GetMapping("/{sessionId}")
    public Object getSession(@PathVariable int sessionId, int userId) {
        return new Response(ResponseStatusEnum.OK)
                .addData("session", sessionService.getSessionVO(sessionId, userId));
    }
}
