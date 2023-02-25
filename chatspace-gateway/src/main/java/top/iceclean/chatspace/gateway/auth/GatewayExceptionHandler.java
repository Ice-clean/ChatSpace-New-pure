package top.iceclean.chatspace.gateway.auth;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import top.iceclean.chatspace.gateway.common.SecProtocolException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * @author : Ice'Clean
 * @date : 2023-01-23
 */
@Component
public class GatewayExceptionHandler extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        // 根据异常返回
        Throwable error = getError(request);
        // 特殊处理
        if (error instanceof SecProtocolException) {
            Map<String, Object> map = new HashMap<>(5);
            map.put("status", 400);
            map.put("message", error.getMessage());
            map.put("path", request.path());
            map.put("exception", error.getClass().getName());
            map.put("timestamp", new Date());
            return map;
        }
        // 一般处理
        return super.getErrorAttributes(request, options);
    }
}
