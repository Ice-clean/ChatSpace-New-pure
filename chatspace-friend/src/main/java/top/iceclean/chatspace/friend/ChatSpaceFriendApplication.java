package top.iceclean.chatspace.friend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import top.iceclean.chatspace.cache.annotation.EnableCache;
import top.iceclean.chatspace.infrastructure.annotation.EnableInfrastructure;
import top.iceclean.feign.FriendClient;
import top.iceclean.feign.GroupClient;
import top.iceclean.feign.SessionClient;
import top.iceclean.feign.UserClient;
import top.iceclean.feign.annotation.EnableInnerCall;

/**
 * @author : Ice'Clean
 * @date : 2022-10-10
 */
@EnableCache
@EnableInnerCall
@EnableInfrastructure
@EnableFeignClients(clients = {FriendClient.class, GroupClient.class, UserClient.class, SessionClient.class})
@SpringBootApplication
public class ChatSpaceFriendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatSpaceFriendApplication.class, args);
    }
}
