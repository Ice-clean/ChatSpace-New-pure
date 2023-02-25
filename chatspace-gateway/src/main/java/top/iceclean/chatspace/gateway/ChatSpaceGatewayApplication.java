package top.iceclean.chatspace.gateway;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Bean;
import top.iceclean.chatspace.gateway.loadbalance.ConsistentHashingConfig;

/**
 * @author : Ice'Clean
 * @date : 2022-10-08
 */
@SpringBootApplication
@LoadBalancerClients(defaultConfiguration = ConsistentHashingConfig.class)
public class ChatSpaceGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatSpaceGatewayApplication.class, args);
    }

    /** 注入 Prometheus */
    @Bean
    MeterRegistryCustomizer<MeterRegistry> configurer(@Value("${spring.application.name}") String applicationName) {
        return registry -> registry.config().commonTags("application", applicationName);
    }
}
