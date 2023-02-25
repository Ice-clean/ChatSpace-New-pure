package top.iceclean.chatspace.infrastructure.annotation;

import org.springframework.context.annotation.Import;
import top.iceclean.chatspace.infrastructure.auth.UserAuth;
import top.iceclean.chatspace.infrastructure.config.*;
import top.iceclean.chatspace.infrastructure.handler.GlobalExceptionHandler;
import top.iceclean.chatspace.infrastructure.utils.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启动基础设施的必要服务
 * @author : Ice'Clean
 * @date : 2022-12-17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({UserAuth.class,
        MybatisPlusConfig.class, MyPrometheusConfig.class, NacosConfig.class,
        RedisConfig.class, IdWorkerConfig.class,
        GlobalExceptionHandler.class,
        GeoUtils.class, MailUtils.class, RedissonUtils.class, RedisTemplateUtils.class})
public @interface EnableInfrastructure {
}
