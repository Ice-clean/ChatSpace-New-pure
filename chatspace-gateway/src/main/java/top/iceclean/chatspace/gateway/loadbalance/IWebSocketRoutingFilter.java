package top.iceclean.chatspace.gateway.loadbalance;

import io.micrometer.core.lang.NonNullApi;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import top.iceclean.chatspace.gateway.utils.JwtUtils;

import java.net.URI;
import java.util.*;

import static org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter.filterRequest;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.*;

/**
 * 把整个 WebsocketRoutingFilter 拷下来了
 * 加入了自己的逻辑
 * @author : Ice'Clean
 * @date : 2023-01-28
 */
@Slf4j
@NonNullApi
// @Component
public class IWebSocketRoutingFilter implements GlobalFilter, Ordered {

    /**
     * Sec-Websocket protocol.
     */
    public static final String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";
    private final WebSocketClient webSocketClient;
    private final WebSocketService webSocketService;
    private final ObjectProvider<List<HttpHeadersFilter>> headersFiltersProvider;
    private volatile List<HttpHeadersFilter> headersFilters;

    /** 一致性哈希 */
    private final ConsistentHashing consistentHashing;

    public IWebSocketRoutingFilter(WebSocketClient webSocketClient, WebSocketService webSocketService,
                                   ObjectProvider<List<HttpHeadersFilter>> headersFiltersProvider,
                                   ConsistentHashing consistentHashing) {
        this.webSocketClient = webSocketClient;
        this.webSocketService = webSocketService;
        this.headersFiltersProvider = headersFiltersProvider;
        this.consistentHashing = consistentHashing;
    }

    static String convertHttpToWs(String scheme) {
        scheme = scheme.toLowerCase();
        return "http".equals(scheme) ? "ws" : "https".equals(scheme) ? "wss" : scheme;
    }

    @Override
    public int getOrder() {
        // Before NettyRoutingFilter since this routes certain http requests
        return Ordered.LOWEST_PRECEDENCE - 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("使用了自定义的过滤器");
        changeSchemeIfIsWebSocketUpgrade(exchange);

        URI requestUrl = exchange.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);
        String scheme = requestUrl.getScheme();

        if (isAlreadyRouted(exchange) || (!"ws".equals(scheme) && !"wss".equals(scheme))) {
            return chain.filter(exchange);
        }
        setAlreadyRouted(exchange);

        HttpHeaders headers = exchange.getRequest().getHeaders();
        HttpHeaders filtered = filterRequest(getHeadersFilters(), exchange);

        List<String> protocols = getProtocols(headers);

        return this.webSocketService.handleRequest(exchange,
                new IProxyWebSocketHandler(requestUrl, this.webSocketClient, filtered, protocols, consistentHashing));
    }

    /* for testing */ List<String> getProtocols(HttpHeaders headers) {
        List<String> protocols = headers.get(SEC_WEBSOCKET_PROTOCOL);
        if (protocols != null) {
            ArrayList<String> updatedProtocols = new ArrayList<>();
            for (String protocol : protocols) {
                updatedProtocols.addAll(Arrays.asList(StringUtils.tokenizeToStringArray(protocol, ",")));
            }
            protocols = updatedProtocols;
        }
        return protocols;
    }

    /* for testing */ List<HttpHeadersFilter> getHeadersFilters() {
        if (this.headersFilters == null) {
            this.headersFilters = this.headersFiltersProvider.getIfAvailable(ArrayList::new);

            // remove host header unless specifically asked not to
            headersFilters.add((headers, exchange) -> {
                HttpHeaders filtered = new HttpHeaders();
                filtered.addAll(headers);
                filtered.remove(HttpHeaders.HOST);
                boolean preserveHost = exchange.getAttributeOrDefault(PRESERVE_HOST_HEADER_ATTRIBUTE, false);
                if (preserveHost) {
                    String host = exchange.getRequest().getHeaders().getFirst(HttpHeaders.HOST);
                    filtered.add(HttpHeaders.HOST, host);
                }
                return filtered;
            });

            headersFilters.add((headers, exchange) -> {
                HttpHeaders filtered = new HttpHeaders();
                for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                    if (!entry.getKey().toLowerCase().startsWith("sec-websocket")) {
                        filtered.addAll(entry.getKey(), entry.getValue());
                    }
                }
                return filtered;
            });
        }

        return this.headersFilters;
    }

    static void changeSchemeIfIsWebSocketUpgrade(ServerWebExchange exchange) {
        // Check the Upgrade
        URI requestUrl = exchange.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);
        String scheme = requestUrl.getScheme().toLowerCase();
        String upgrade = exchange.getRequest().getHeaders().getUpgrade();
        // change the scheme if the socket client send a "http" or "https"
        if ("WebSocket".equalsIgnoreCase(upgrade) && ("http".equals(scheme) || "https".equals(scheme))) {
            String wsScheme = convertHttpToWs(scheme);
            boolean encoded = containsEncodedParts(requestUrl);
            URI wsRequestUrl = UriComponentsBuilder.fromUri(requestUrl).scheme(wsScheme).build(encoded).toUri();
            exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, wsRequestUrl);
            if (log.isTraceEnabled()) {
                log.trace("changeSchemeTo:[" + wsRequestUrl + "]");
            }
        }
    }

    private static class IProxyWebSocketHandler implements WebSocketHandler {
        private final WebSocketClient client;
        private final URI url;
        private final HttpHeaders headers;
        private final List<String> subProtocols;
        /** 一致性哈希 */
        private final ConsistentHashing consistentHashing;
        /** 用户 ID */
        private int userId;
        /** 客户端发送消息计数 */
        private int sendNum = 0;

        IProxyWebSocketHandler(URI url, WebSocketClient client, HttpHeaders headers, List<String> protocols, ConsistentHashing consistentHashing) {
            this.client = client;
            this.url = url;
            this.headers = headers;
            this.consistentHashing = consistentHashing;
            if (protocols != null) {
                this.subProtocols = protocols;
                this.userId = Integer.parseInt(JwtUtils.parseJWT(protocols.get(0)).getSubject());
            }
            else {
                this.subProtocols = Collections.emptyList();
            }
        }

        private Mono<Void> serverCloseHandle(CloseStatus status, WebSocketSession session) {
            log.info("服务端主动关闭连接，共发送消息：{} 条", sendNum);
            removeUserRing();
            return session.close(status);
        }

        private Mono<Void> proxyCloseHandle(CloseStatus status, WebSocketSession session) {
            log.info("【客户端】主动关闭连接，共发送消息：{} 条", sendNum);
            removeUserRing();
            return session.close(status);
        }

        /** 连接关闭后，从哈希环上移除用户连接 */
        private void removeUserRing() {
            consistentHashing.userRemove(userId);
        }

        private Publisher<WebSocketMessage> proxySessionSendHandle(WebSocketSession session) {
            return session.receive()
                    .doOnNext(WebSocketMessage::retain)
                    .doOnNext(message -> {
                        log.info("收到客户端消息：{}", message);
                        sendNum++;
                    });
        }

        private Publisher<WebSocketMessage> serverSessionSendHandle(WebSocketSession proxySession) {
            return proxySession.receive()
                    .doOnNext(WebSocketMessage::retain)
                    .doOnNext(message -> {
                        log.info("收到服务端消息：{}", message.getPayloadAsText());
                    });
        }

        @Override
        public List<String> getSubProtocols() {
            return this.subProtocols;
        }

        @Override
        public Mono<Void> handle(WebSocketSession session) {
            // pass headers along so custom headers can be sent through
            return client.execute(url, this.headers, new WebSocketHandler() {
                @Override
                public Mono<Void> handle(WebSocketSession proxySession) {
                    log.info("处理消息！！！");
                    // 【自定义】对连接的关闭作处理
                    Mono<Void> serverClose = proxySession.closeStatus().filter(closeStatus -> session.isOpen())
                            .flatMap(closeStatus -> serverCloseHandle(closeStatus, session));
                    Mono<Void> proxyClose = session.closeStatus().filter(closeStatus -> proxySession.isOpen())
                            .flatMap(closeStatus -> proxyCloseHandle(closeStatus, session));
                    Mono<Void> proxySessionSend = proxySession
                            .send(proxySessionSendHandle(session));
                    Mono<Void> serverSessionSend = session
                            .send(serverSessionSendHandle(proxySession));
                    Mono.when(serverClose, proxyClose).subscribe();
                    return Mono.zip(proxySessionSend, serverSessionSend).then();
                }

                /**
                 * Copy subProtocols so they are available downstream.
                 * @return available subProtocols.
                 */
                @Override
                public List<String> getSubProtocols() {
                    return IProxyWebSocketHandler.this.subProtocols;
                }
            });
        }

    }
}