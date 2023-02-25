package top.iceclean.chatspace.realtime.handler;

import io.jsonwebtoken.ExpiredJwtException;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import top.iceclean.chatspace.cache.UserCache;
import top.iceclean.chatspace.infrastructure.utils.JwtUtils;
import top.iceclean.chatspace.realtime.server.ServerProperties;

import java.util.Arrays;


/**
 * 权限校验处理器
 * @author : Ice'Clean
 * @date : 2022-09-29
 */
@Slf4j
@Component
public class AuthHandler {

    private AuthHandler() {

    }

    @ChannelHandler.Sharable
    public static class Inbound extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            log.info("请求：" + msg);
            // 获取头部
            HttpHeaders headers = ((FullHttpRequest) msg).headers();
            // 校验头部的 Sec-WebSocket-Protocol，里边存放了 token
            String[] param = headers.get("Sec-WebSocket-Protocol").split(",");
            // 获取 token 和选择的空间
            String token = param[0];
            String spaceId = param[1];
            HandlerHelper.setToken(ctx, token);
            if (spaceId == null) {
                log.warn("空间 ID 为空");
                ctx.close();
            } else {
                HandlerHelper.setSpaceId(ctx, Integer.parseInt(spaceId));
            }

            try {
                // 解析用户，并与当前线程绑定
                String userId = JwtUtils.parseJWT(token).getSubject();
                HandlerHelper.setUserId(ctx, Integer.parseInt(userId));
                log.info("用户请求连接 id: " + HandlerHelper.getUserId(ctx));
                super.channelRead(ctx, msg);
            } catch (NullPointerException e) {
                log.warn("用户 token 为空！");
                ctx.close();
            } catch (ExpiredJwtException e) {
                log.warn("用户 token 过期！");
                ctx.close();
            } catch (Exception e) {
                log.warn("token 解析失败：" + e.getMessage());
                log.warn(token);
                ctx.close();
            }
        }
    }

    @ChannelHandler.Sharable
    public static class Outbound extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            ((DefaultFullHttpResponse) msg).headers().set("Sec-WebSocket-Protocol", HandlerHelper.getToken(ctx));
            super.write(ctx, msg, promise);
        }
    }
}
