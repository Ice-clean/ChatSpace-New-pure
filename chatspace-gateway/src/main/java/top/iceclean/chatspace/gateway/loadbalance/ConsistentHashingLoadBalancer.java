package top.iceclean.chatspace.gateway.loadbalance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import top.iceclean.chatspace.gateway.common.SecProtocolException;
import top.iceclean.chatspace.gateway.utils.JwtUtils;

import java.util.Arrays;
import java.util.List;

/**
 * websocket 的负载均衡实现
 * @author : Ice'Clean
 * @date : 2023-01-22
 */
@Slf4j
public class ConsistentHashingLoadBalancer extends RandomLoadBalancer {
    /** 目标服务 ID */
    private final String serviceId;
    /** 目标服务的实例提供者 */
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    /** 需要进行一致性哈希负载均衡的请求配置 */
    private final ConsistentHashingProperties properties;
    /** 自己的一致性哈希算法实现 */
    private final ConsistentHashing consistentHashing;

    public ConsistentHashingLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
                                         String serviceId,
                                         ConsistentHashingProperties properties,
                                         ConsistentHashing consistentHashing) {
        super(serviceInstanceListSupplierProvider, serviceId);
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.serviceId = serviceId;
        this.properties = properties;
        this.consistentHashing = consistentHashing;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        // 获取请求路径
        DefaultRequestContext requestContext = (DefaultRequestContext) request.getContext();
        RequestData clientRequest = (RequestData) requestContext.getClientRequest();
        String uri = clientRequest.getUrl().getPath();

        // 处理 http 负载均衡
        if (properties.getHttpUrls().contains(uri)) {
            return httpChoose(clientRequest);
        }

        // 处理 websocket 负载均衡
        if (properties.getWebsocketUrls().contains(uri)) {
            return websocketChoose(request, clientRequest);
        }

        // 没有在配置文件中指定的请求，使用默认负载算法
        return super.choose(request);
    }

    private Mono<Response<ServiceInstance>> httpChoose(RequestData clientRequest) {
        // 暂时还没有 http 需要一致性哈希负载均衡
        return null;
    }

    @SuppressWarnings("rawtypes")
    private Mono<Response<ServiceInstance>> websocketChoose(Request request, RequestData clientRequest) {
        // 获取 ws 子协议
        List<String> list = clientRequest.getHeaders().get("Sec-WebSocket-Protocol");
        if (list == null || list.isEmpty()) {
            // 缺失子协议，抛出错误
            log.error("WebSocket 连接缺失子协议");
            throw new SecProtocolException("WebSocket 连接缺失子协议");
        }
        // 取出用户 ID 和目标空间 ID
        String[] split = list.get(0).split(", ");
        if (split.length < 2) {
            // 子协议错误
            log.error("WebSocket 子协议错误：{}", Arrays.toString(split));
            throw new SecProtocolException("WebSocket 子协议错误");
        }
        // 进行 token 检查
        String userId;
        try {
            userId = JwtUtils.parseJWT(split[0]).getSubject();
        } catch (Exception e) {
            String reason = new top.iceclean.chatspace.gateway.common.Response(e).toString();
            log.error("WebSocket 子协议 token 错误：{}", reason);
            throw new SecProtocolException(reason);
        }
        String spaceId = split[1];

        // 获取所有 Netty 服务
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next().map(serviceInstances ->
                processInstanceResponse(supplier, serviceInstances, userId, spaceId));
    }

    private Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier,
                                                              List<ServiceInstance> serviceInstances,
                                                              String userId, String spaceId) {
        Response<ServiceInstance> serviceInstanceResponse = getInstanceResponse(serviceInstances, userId, spaceId);
        if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
            ((SelectedInstanceCallback) supplier).selectedServiceInstance(serviceInstanceResponse.getServer());
        }
        return serviceInstanceResponse;
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances, String userId, String spaceId) {
        if (CollectionUtils.isEmpty(instances)) {
            log.info("无可用服务: {}", serviceId);
            return new EmptyResponse();
        }

        // 对接一致性哈希算法
        ServiceInstance instance = consistentHashing.getInstance(instances, userId, spaceId);
        if (instance == null) {
            log.info("找不到可受理的节点: userId={}, spaceId={}", userId, spaceId);
            return new EmptyResponse();
        }
        return new DefaultResponse(instance);
    }
}
