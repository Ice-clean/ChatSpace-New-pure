package top.iceclean.chatspace.group;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import top.iceclean.chatspace.cache.annotation.EnableCache;
import top.iceclean.chatspace.infrastructure.annotation.EnableInfrastructure;
import top.iceclean.feign.*;
import top.iceclean.feign.annotation.EnableInnerCall;

/**
 * @author : Ice'Clean
 * @date : 2022-10-09
 */
@EnableCache
@EnableInnerCall
@EnableInfrastructure
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients(clients = {FriendClient.class, GroupClient.class, UserClient.class, MessageClient.class, SessionClient.class})
@SpringBootApplication
public class ChatSpaceGroupApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatSpaceGroupApplication.class, args);
    }
}
