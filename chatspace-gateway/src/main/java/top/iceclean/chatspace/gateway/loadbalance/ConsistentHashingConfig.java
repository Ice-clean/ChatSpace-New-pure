package top.iceclean.chatspace.gateway.loadbalance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 启用一致性哈希算法
 * @author : Ice'Clean
 * @date : 2023-01-23
 */
@Slf4j
public class ConsistentHashingConfig {
    private final ConsistentHashingProperties consistentHashingProperties;
    private final ConsistentHashing consistentHashing;

    public ConsistentHashingConfig(ConsistentHashingProperties consistentHashingProperties,
                                   ConsistentHashing consistentHashing) {
        this.consistentHashingProperties = consistentHashingProperties;
        this.consistentHashing = consistentHashing;
    }

    @Bean
    public ReactorLoadBalancer<ServiceInstance> consistentHashingLoadBalancer(
            Environment environment, LoadBalancerClientFactory loadBalancerClientFactory) {
        String clientName = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        ObjectProvider<ServiceInstanceListSupplier> lazyProvider = loadBalancerClientFactory.getLazyProvider(clientName, ServiceInstanceListSupplier.class);
        log.info("为 {} 添加一致性哈希负载均衡器", clientName);
        return new ConsistentHashingLoadBalancer(lazyProvider, clientName, consistentHashingProperties, consistentHashing);
    }
}
