package top.iceclean.chatspace.gateway.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.iceclean.chatspace.gateway.common.Response;
import top.iceclean.chatspace.gateway.constant.HeaderConst;
import top.iceclean.chatspace.gateway.utils.JwtUtils;

import java.util.List;

/**
 * @author : Ice'Clean
 * @date : 2022-10-16
 */
@Slf4j
@Order(0)
@Component
public class TokenFilter implements GlobalFilter {

    /** 白名单 */
    private static final String[] WRITE_PATH = {
            "/space/user/login",
            "/space/user/register",
            "/space/user/code"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String reqPath = exchange.getRequest().getPath().toString();
        for (String path : WRITE_PATH) {
            if (reqPath.startsWith(path)) {
                // 白名单直接放行
                return chain.filter(exchange);
            }
        }
        // 管理员放行
        final List<String> list = exchange.getRequest().getHeaders().get(HeaderConst.ADMIN_TAG);
        // 直接判断
        if (list != null && !list.isEmpty() && "1".equals(list.get(0))) {
            return chain.filter(exchange);
        }

        try {
            // 提取 token 解析用户，并与当前线程绑定
            ServerHttpRequest request = exchange.getRequest();
            String token = request.getHeaders().getFirst(HeaderConst.TOKEN);
            String userId = JwtUtils.parseJWT(token).getSubject();
            request.mutate().header(HeaderConst.AUTH_USER, userId);
            log.info("认证成功，用户：" + userId);
            return chain.filter(exchange);
        } catch (Exception e) {
            log.info("token 鉴权失败");
            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().set("Content-Type", "application/json; charset=UTF-8");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(new Response(e).toBytes())));
        }
    }
}
