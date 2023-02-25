package top.iceclean.chatspace.infrastructure.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.iceclean.chatspace.infrastructure.utils.DateUtils;

import java.util.Map;

/**
 * Nacos 配置
 * @author : Ice'Clean
 * @date : 2023-01-24
 */
@Slf4j
@Configuration
public class NacosConfig {
    @Bean
    public NacosDiscoveryProperties nacosProperties() {
        // 服务注册时，添加启动时间
        NacosDiscoveryProperties nacosDiscoveryProperties = new NacosDiscoveryProperties();
        Map<String, String> metadata = nacosDiscoveryProperties.getMetadata();
        metadata.put("start-time", DateUtils.getTime());
        return nacosDiscoveryProperties;
    }
}
