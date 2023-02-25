package top.iceclean.chatspace.realtime.constant;

import io.netty.util.AttributeKey;
import top.iceclean.chatspace.realtime.share.ServerSession;

/**
 * @author : Ice'Clean
 * @date : 2022-10-28
 */
public class ChannelKey {
    /** 权限校验常量 */
    public static final AttributeKey<String> TOKEN = AttributeKey.valueOf("token");
    /** 通过通道获取用户 ID 的 KEY */
    public static final AttributeKey<Integer> USER_ID_KEY = AttributeKey.valueOf("USER_ID_KEY");
    /** 通过通道获取会话的 KEY */
    public static final AttributeKey<ServerSession> SESSION_KEY = AttributeKey.valueOf("SESSION_KEY");
    /** 通过通道获取空间 ID */
    public static final AttributeKey<Integer> SPACE_ID_KEY = AttributeKey.valueOf("SPACE_ID_KEY");
    /** 强制下线标识 */
    public static final AttributeKey<Boolean> FORCED_OFFLINE = AttributeKey.valueOf("FORCED_OFFLINE");
}
