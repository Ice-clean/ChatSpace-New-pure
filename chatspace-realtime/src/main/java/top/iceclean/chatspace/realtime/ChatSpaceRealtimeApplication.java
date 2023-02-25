package top.iceclean.chatspace.realtime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import top.iceclean.chatspace.cache.annotation.EnableCache;
import top.iceclean.chatspace.infrastructure.annotation.EnableInfrastructure;
import top.iceclean.chatspace.realtime.processor.message.SpaceProcessor;
import top.iceclean.chatspace.realtime.processor.site.SiteProcessor;
import top.iceclean.chatspace.realtime.server.WebSocketServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import top.iceclean.feign.*;
import top.iceclean.feign.annotation.EnableInnerCall;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * @author : Ice'Clean
 * @date : 2022-10-08
 */
@EnableInnerCall
@EnableInfrastructure
@EnableFeignClients(clients = {
        UserClient.class, MessageClient.class,
        GroupClient.class, FriendClient.class, SessionClient.class})
@SpringBootApplication
public class ChatSpaceRealtimeApplication {
    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext run = SpringApplication.run(ChatSpaceRealtimeApplication.class, args);
        // 缓存每个空间的区域信息
        run.getBean(SpaceProcessor.class).zoneInit();
        // 启动 Netty 服务
        run.getBean(WebSocketServer.class).start();
    }
}
