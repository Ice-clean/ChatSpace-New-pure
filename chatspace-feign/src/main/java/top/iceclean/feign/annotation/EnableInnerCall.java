package top.iceclean.feign.annotation;

import org.springframework.context.annotation.Import;
import top.iceclean.feign.itercepter.InnerCall;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 允许进行内部调用（加了一个管理员的头）
 * @author : Ice'Clean
 * @date : 2022-12-17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(InnerCall.class)
public @interface EnableInnerCall {
}
