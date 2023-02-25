package top.iceclean.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;
import top.iceclean.chatspace.infrastructure.dto.MessageDTO;
import top.iceclean.chatspace.infrastructure.po.Message;
import top.iceclean.chatspace.infrastructure.vo.MessageVO;

/**
 * @author : Ice'Clean
 * @date : 2022-10-09
 */
@FeignClient(value = "chatspace-message", path = "/space")
public interface MessageClient {

    /**
     * 获取指定会话最后一条消息的 ID
     * @param sessionId 会话 ID
     * @return 最后一条消息 ID
     */
    @GetMapping("/last-msg-id/{sessionId}")
    int getLastMsgId(@PathVariable int sessionId);

    /**
     * 将消息对象转化成消息响应对象
     * @param message 消息对象
     * @param userId 当前用户 ID
     * @param info 是否需要详细信息（会话消息列表需要）
     * @return 消息响应对象
     */
    @GetMapping("/vo")
    MessageVO toMessageVO(@SpringQueryMap Message message, @RequestParam int userId, @RequestParam boolean info);
}
