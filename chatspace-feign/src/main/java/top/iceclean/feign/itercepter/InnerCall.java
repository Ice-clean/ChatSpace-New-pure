package top.iceclean.feign.itercepter;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

/**
 * 内部调用的拦截器
 * @author : Ice'Clean
 * @date : 2022-12-17
 */
@Component
public class InnerCall implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 加入 AUTH-USER 请求头
        requestTemplate.header("auth-user", "1");
    }
}
