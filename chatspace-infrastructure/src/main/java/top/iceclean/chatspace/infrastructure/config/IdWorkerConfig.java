package top.iceclean.chatspace.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.iceclean.chatspace.infrastructure.utils.IdWorker;

/**
 * 雪花 ID 生成器配置类
 * @author : Ice'Clean
 * @date : 2022-12-17
 */
@Configuration
public class IdWorkerConfig {

    @Bean
    public IdWorker idWorker() {
        // 先固定为 1
        return new IdWorker(1, 1, 1);
    }
}
