package top.iceclean.chatspace.group.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.iceclean.chatspace.infrastructure.constant.QueueConst;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 的延时队列配置
 * @author : Ice'Clean
 * @date : 2022-12-15
 */
@Configuration
public class DelayQueueConfig {
    @Bean
    CustomExchange boxExchange(){
        Map<String, Object> setting = new HashMap<>(1);
        setting.put("x-delayed-type", "direct");
        return new CustomExchange(QueueConst.BOX_EXCHANGE,
                QueueConst.DELAY_EXCHANGE_TYPE, true, false, setting);
    }

    @Bean
    Queue boxLockQueue() {
        return new Queue(QueueConst.BOX_LOCK_QUEUE, true, false, false);
    }

    @Bean
    Binding bindingMsg(Queue boxLockQueue, CustomExchange boxExchange){
        return BindingBuilder.bind(boxLockQueue)
                .to(boxExchange).with(QueueConst.BOX_LOCK_QUEUE).noargs();
    }
}
