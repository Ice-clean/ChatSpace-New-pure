package top.iceclean.chatspace.gateway.loadbalance;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import top.iceclean.chatspace.gateway.common.NodeMessage;

import java.util.*;
import java.util.function.Predicate;

/**
 * 为 websocket 实现的一致性哈希算法
 * 哈希环目前使用红黑树实现，后续可以使用 Redis zset 实现（Gateway 集群）
 * @author : Ice'Clean
 * @date : 2023-01-22
 */
@Slf4j
@Component
public class ConsistentHashing {
    /** 节点哈希环 (hash -> serverId) */
    private final NavigableMap<Integer, String> serverRing;
    /** 用户哈希环 (hash -> userEntry) */
    private final NavigableMap<Integer, UserEntry> userRing;
    /** 服务实例 (serverId -> serverEntry) */
    private final Map<String, ServerEntry> serverMap;

    /** 虚拟节点个数 */
    private static final int VIRTUAL_NODES = 100;
    /** RabbitMQ 用于发送用户连接重置消息 */
    private final AmqpTemplate amqpTemplate;

    public ConsistentHashing(AmqpTemplate amqpTemplate) {
        serverRing = new TreeMap<>();
        userRing = new TreeMap<>();
        serverMap = new HashMap<>();
        this.amqpTemplate = amqpTemplate;
    }

    /**
     * 使用一致性哈希算法，选择一个节点
     * @param instances 现有所有实例列表
     * @param userId 请求的用户 ID
     * @param spaceId 请求的空间 ID
     * @return 要提供服务的的节点实例
     */
    public ServiceInstance getInstance(List<ServiceInstance> instances, String userId, String spaceId) {
        log.info("为（{}, {}）执行一致性哈希算法", userId, spaceId);
        // 对用户 ID 取哈希
        int userHash = getHash(String.join("-", userId, spaceId));
        int uId = Integer.parseInt(userId);
        int sId = Integer.parseInt(spaceId);

        // 把遍历到的节点存储起来，防止重复遍历虚拟节点
        Set<String> visited = new HashSet<>(serverMap.size());
        // 遍历节点哈希环，找到第一个能受理所请求空间的
        Map.Entry<Integer, String> ringEntry = serverRing.higherEntry(userHash);
        while (ringEntry != null) {
            // 获取虚拟节点对应的服务节点 ID
            String serverId = ringEntry.getValue();
            if (!visited.contains(serverId)) {
                visited.add(serverId);
                // 找到能受理的节点则退出
                ServerEntry serverEntry = serverMap.get(serverId);
                if (serverEntry != null && serverEntry.access(sId)) {
                    userRing.put(userHash, new UserEntry(uId, sId, serverEntry.getServerId()));
                    return getInstance(instances, serverEntry);
                }
                log.info("{} 无法受理，继续寻找下一个", serverId);
            }
            // 否则继续寻找下一个节点
            ringEntry = serverRing.higherEntry(ringEntry.getKey());
        }

        // 没找到的话，则尝试寻找第一个（环的概念）
        log.info("哈希环遍历到末尾，尝试寻找第一个");
        ServerEntry serverEntry = serverMap.get(serverRing.firstEntry().getValue());
        if (serverEntry != null && serverEntry.access(sId)) {
            userRing.put(userHash, new UserEntry(uId, sId, serverEntry.getServerId()));
            return getInstance(instances, serverEntry);
        }

        // 否则说明没有任何节点可以受理这个空间，返回空
        log.info("没有任何节点可以受理，返回空");
        return null;
    }

    /**
     * 获取服务节点对应的实例
     * @param instances 现有所有实例列表
     * @param serverEntry 选中的服务节点
     * @return 服务节点对应的实例
     */
    private ServiceInstance getInstance(List<ServiceInstance> instances, ServerEntry serverEntry) {
        log.info("接受受理：{}", serverEntry);
        // 遍历所有实例，找到 IP 和端口都相同的
        for (ServiceInstance instance : instances) {
            if (serverEntry.getIp().equals(instance.getHost()) &&
                    serverEntry.getPort().equals(instance.getPort())) {
                return instance;
            }
        }
        // 寻找到目标节点，却在实例中没有找到，说明该服务实际已经下线，补偿一下
        log.error("服务 {} 已无对应实例却仍存在与哈希环，执行自动下线", serverEntry);
        serverRemove(serverEntry.getIp(), serverEntry.getPort());
        return null;
    }

    /**
     * Netty 节点增加
     * @param newInstances 新增的节点实例列表
     */
    public void serverAdd(List<Instance> newInstances) {
        if (newInstances.isEmpty()) {
            return;
        }
        // 创建服务实体
        for (Instance instance : newInstances) {
            String id = instance.getIp() + ":" + instance.getPort();
            if (serverMap.containsKey(id)) {
                log.error("节点已经存在，添加失败：{}", id);
                continue;
            }
            ServerEntry newEntry = new ServerEntry(instance);
            serverMap.put(id, newEntry);

            // 节点创建成功，往哈希环中插入虚拟节点并保存他们的哈希值
            newEntry.setNodeHashList(addVirtualNode(id));
            // 当新的节点能够受理用户连接，则该用户连接需要重置
            userResetHandle(newEntry, userEntry ->
                    newEntry.isAccess(userEntry.getSpaceId()));
            log.info("节点增加成功：{}", id);
        }
    }

    /**
     * 为节点创建虚拟节点
     * @param serverId 节点 ID
     * @return 虚拟节点哈希值列表
     */
    private List<Integer> addVirtualNode(String serverId) {
        List<Integer> nodeHashList = new ArrayList<>(VIRTUAL_NODES);
        for (int i = 0; i < VIRTUAL_NODES; i++) {
            int hash = getHash(serverId + "#" + i);
            nodeHashList.add(hash);
            serverRing.put(hash, serverId);
        }
        return nodeHashList;
    }

    /**
     * Netty 节点移除
     * @param removeInstances 移除的节点实例列表
     */
    public void serverRemove(List<Instance> removeInstances) {
        if (removeInstances.isEmpty()) {
            return;
        }
        // 简单地将节点从哈希环中去除即可，由客户端进行重连
        removeInstances.forEach(i -> serverRemove(i.getIp(), i.getPort()));
    }

    /**
     * 通过 IP 和端口移除服务节点
     * @param ip 节点 IP 地址
     * @param port 节点端口号
     */
    private void serverRemove(String ip, int port) {
        String id = ip + ":" + port;
        ServerEntry entry = serverMap.remove(id);
        if (entry != null) {
            // 从哈希环上移除掉该节点的所有虚拟节点
            serverRing.keySet().removeAll(entry.getNodeHashList());
            log.info("成功移除节点：{}", id);
        } else {
            // 没有可移除的
            log.error("没有可移除的节点：{}", id);
        }
    }

    /**
     * Netty 节点元数据发生改变
     * @param metadataUpdates 元数据发生改变的节点实例列表
     */
    public void serverMetadataUpdate(List<Instance> metadataUpdates) {
        if (metadataUpdates.isEmpty()) {
            return;
        }
        for (Instance instance : metadataUpdates) {
            String id = instance.getIp() + ":" + instance.getPort();
            ServerEntry serverEntry = serverMap.get(id);
            if (serverEntry == null) {
                log.error("更新配置的服务节点不存在：{}", id);
                continue;
            }
            // 受理空间发生改变，需要更改用户连接
            if (serverEntry.updateMetadata(instance.getMetadata())) {
                // 处理移除的受理空间
                Set<Integer> removeSpaces = serverEntry.getRemoveSpaces();
                if (!removeSpaces.isEmpty()) {
                    log.info("{} 移除受理空间：{}", serverEntry.getServerId(), removeSpaces);
                    // 若用户连接的空间从本节点移除掉了，则该用户连接需要重置
                    userResetHandle(serverEntry, userEntry ->
                            removeSpaces.contains(userEntry.getSpaceId()));
                }
                // 处理新增的受理空间
                Set<Integer> newSpaces = serverEntry.getNewSpaces();
                if (!newSpaces.isEmpty()) {
                    log.info("{} 新增受理空间：{}", serverEntry.getServerId(), newSpaces);
                    // 当用户连接的空间是本节点可以受理了，则用户连接也需要重置
                    userResetHandle(serverEntry, userEntry ->
                            newSpaces.contains(userEntry.getSpaceId()));
                }
            }
        }
    }

    /**
     * 扫描指定节点的所有需要重置连接的候选用户
     * 即所有虚拟节点到前一个结点的所有用户
     * 并按提供的断言筛选出需要处理的用户（新增结点、元数据更改）
     * @param server 目标服务结点
     * @param removeRule 断言规则
     */
    public void userResetHandle(@NonNull ServerEntry server, Predicate<UserEntry> removeRule) {
        // 收集需要更改的用户实体
        List<UserEntry> resetUserList = new ArrayList<>();
        server.getNodeHashList().forEach(hash -> {
            // 找到该虚拟节点的前一个结点
            Integer beforeHash = serverRing.lowerKey(hash);
            // 收集要移除的用户连接
            SortedMap<Integer, UserEntry> removeHash;
            if (beforeHash == null) {
                // 如果前一个结点为 null，则说明该结点处在最开始的那段，最后一个键的尾部和到该键的头部是需要搜索的范围
                removeHash = userRing.tailMap(serverRing.lastKey());
                removeHash.putAll(userRing.headMap(hash));
            } else {
                // 否则，beforeHash 和 hash 为要搜索的范围
                removeHash = userRing.subMap(beforeHash, hash);
            }
            // 移除掉不需要更改的
            removeHash.entrySet().removeIf(entry -> !removeRule.test(entry.getValue()));
            // 收集起来准备通知，并从用户环中移除这些用户
            resetUserList.addAll(removeHash.values());
            userRing.keySet().removeAll(removeHash.keySet());
        });
        // 发送用户连接更变消息到消息队列，由各个节点自行处理
        log.info("需要更改连接的用户：{}", resetUserList);
        for (UserEntry userEntry : resetUserList) {
            amqpTemplate.convertAndSend("node-consumer-" + userEntry.getServerId(),
                    JSON.toJSONString(new NodeMessage(NodeMessage.Type.USER_RESET, userEntry.getUserId(), null)));
        }
    }

    public void userRemove(int userId) {
        UserEntry remove = userRing.remove(userId);
        if (remove != null) {
            serverMap.get(remove.getServerId()).userRemove();
        }
    }

    /** 使用 FNV1_32_HASH 算法计算服务器的 Hash 值 */
    private static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash ^ str.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        // 如果算出来的值为负数则取其绝对值
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }
}
