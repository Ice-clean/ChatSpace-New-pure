package top.iceclean.chatspace.message.mq;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.iceclean.chatspace.infrastructure.dto.MessageDTO;
import top.iceclean.chatspace.message.service.MessageService;

/**
 * 消息队列的消息接收者器
 * @author : Ice'Clean
 * @date : 2022-12-3
 */
@Component
public class MessageReceiver {

    private final MessageService messageService;

    public MessageReceiver(MessageService messageService) {
        this.messageService = messageService;
    }
    /**
     * 聊天消息保存消息
     * @param msg 消息
     */
    @RabbitListener(queuesToDeclare = @Queue("new-message"))
    public void saveMessage(String msg) {
        messageService.saveMessage(JSON.parseObject(msg, MessageDTO.class));
    }
}
