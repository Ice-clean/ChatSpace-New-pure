package top.iceclean.chatspace.cache.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.iceclean.chatspace.cache.*;

/**
 * 缓存的自动装载
 * @author : Ice'Clean
 * @date : 2022-12-23
 */
@Configuration
@Import({GeoCache.class, GroupCache.class, SessionCache.class,
        SpaceCache.class, UserCache.class, ZoneCache.class})
public class ChatSpaceCacheAutoConfigure {
}
