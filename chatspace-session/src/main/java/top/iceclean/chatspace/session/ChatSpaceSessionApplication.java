package top.iceclean.chatspace.session;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import top.iceclean.chatspace.cache.annotation.EnableCache;
import top.iceclean.chatspace.infrastructure.annotation.EnableInfrastructure;
import top.iceclean.feign.FriendClient;
import top.iceclean.feign.GroupClient;
import top.iceclean.feign.MessageClient;
import top.iceclean.feign.UserClient;
import top.iceclean.feign.annotation.EnableInnerCall;

/**
 * @author : Ice'Clean
 * @date : 2022-10-09
 */
@EnableCache
@EnableInnerCall
@EnableInfrastructure
@EnableFeignClients(clients = {FriendClient.class, GroupClient.class, MessageClient.class, UserClient.class})
@SpringBootApplication
public class ChatSpaceSessionApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatSpaceSessionApplication.class, args);
    }
}
