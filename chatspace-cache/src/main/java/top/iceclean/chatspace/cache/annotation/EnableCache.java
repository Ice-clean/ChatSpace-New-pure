package top.iceclean.chatspace.cache.annotation;

import org.springframework.context.annotation.Import;
import top.iceclean.chatspace.cache.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : Ice'Clean
 * @date : 2022-12-17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({GeoCache.class, GroupCache.class, SessionCache.class,
        SpaceCache.class, UserCache.class, ZoneCache.class})
public @interface EnableCache {
}
