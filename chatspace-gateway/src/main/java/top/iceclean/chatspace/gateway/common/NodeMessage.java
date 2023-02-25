package top.iceclean.chatspace.gateway.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * （冗余）
 * @author : Ice'Clean
 * @date : 2023-02-23
 */
@Data
@AllArgsConstructor
public class NodeMessage {
    /** 消息类型枚举 */
    public enum Type {
        /** 用户连接重置 */
        USER_RESET,
        /** 用户跨节点消息 */
        USER_MESSAGE
    }
    /** 消息类型 */
    Type type;
    /** 目标用户 */
    private int userId;
    /** 数据实体（序列化后） */
    private String data;
}
