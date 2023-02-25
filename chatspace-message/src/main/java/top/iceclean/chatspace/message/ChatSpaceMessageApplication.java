package top.iceclean.chatspace.message;

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

import java.util.PriorityQueue;

/**
 * @author : Ice'Clean
 * @date : 2022-10-09
 */
@EnableCache
@EnableInnerCall
@EnableInfrastructure
@EnableFeignClients(clients = {FriendClient.class, GroupClient.class, UserClient.class, SessionClient.class})
@SpringBootApplication
public class ChatSpaceMessageApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatSpaceMessageApplication.class, args);
    }
}
