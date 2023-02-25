package top.iceclean.chatspace.gateway.loadbalance;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 需要使用一致性哈希的配置
 * @author : Ice'Clean
 * @date : 2023-01-23
 */
@Component
@ConfigurationProperties(prefix = "consistent-hashing")
public class ConsistentHashingProperties {
    /** 需要使用一致性哈希负载算法分配服务节点的请求 */
    private List<String> websocketUrls = new ArrayList<>();
    private List<String> httpUrls = new ArrayList<>();

    public List<String> getWebsocketUrls() {
        return websocketUrls;
    }

    public void setWebsocketUrls(List<String> websocketUrls) {
        this.websocketUrls = websocketUrls;
    }

    public List<String> getHttpUrls() {
        return httpUrls;
    }

    public void setHttpUrls(List<String> httpUrls) {
        this.httpUrls = httpUrls;
    }
}
