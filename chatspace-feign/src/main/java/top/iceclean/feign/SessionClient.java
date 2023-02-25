package top.iceclean.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import top.iceclean.chatspace.infrastructure.constant.SessionType;
import top.iceclean.chatspace.infrastructure.po.Session;
import top.iceclean.chatspace.infrastructure.vo.SessionVO;

/**
 * @author : Ice'Clean
 * @date : 2022-10-09
 */
@FeignClient(value = "chatspace-session", path = "/space")
public interface SessionClient {

    /**
     * 申请创建一个会话
     * @param type 会话类型
     * @param targetId 会话的目标 ID
     * @return 新建会话实体
     */
    @PutMapping("")
    Session createSession(@RequestParam SessionType type, @RequestParam int targetId);

    /**
     * 通过会话 ID 获取简单的会话响应对象
     * @param sessionId 会话 ID
     * @return 会话响应对象
     */
    @GetMapping("/vo")
    SessionVO getSessionVO(@RequestParam int sessionId);

    /**
     * 通过会话 ID 和当前用户 ID 获取详细的会话响应对象
     * @param sessionId 会话 ID
     * @param userId 用户 ID
     * @return 详细的会话响应对象
     */
    @GetMapping("/vo/user")
    SessionVO getSessionVO(@RequestParam int sessionId, @RequestParam int userId);

    /**
     * 通过会话类型和目标 ID 寻找对应会话的 ID
     * @param type 会话类型
     * @param targetId 目标 ID
     * @return 对应会话的 ID
     */
    @GetMapping("/find/id")
    Integer findSessionId(@RequestParam int type, @RequestParam int targetId);

    /**
     * 更新用户在会话中最后一条消息的 ID 为最新的
     * @param sessionId 会话 ID
     * @param userId 用户 ID
     */
    @PutMapping("/last-msg-id")
    void updateLastMsgId(@RequestParam int sessionId, @RequestParam int userId);
}
