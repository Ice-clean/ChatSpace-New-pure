package top.iceclean.chatspace.realtime.common;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 临时聊天会话仓库
 * @author : Ice'Clean
 * @date : 2022-10-17
 * @deprecated 应该使用 SessionCache 支持分布式会话
 */
@Deprecated
public class TempChatRepository {
    /** 自增的聊天会话 ID */
    private static final AtomicInteger TEMP_CHAT_ID = new AtomicInteger();
    /** 保存所有临时会话 */
    private static final ConcurrentMap<Integer, Set<Integer>> TEMP_CHAT_MAP = new ConcurrentHashMap<>();

    private TempChatRepository() {

    }

    /** 新创建一个临时聊天 */
    public static int newTempChat(int ... uIds) {
        // 获取一个新的临时聊天 ID
        int tempChatId = TEMP_CHAT_ID.incrementAndGet();
        Set<Integer> users = new HashSet<>();
        for (int uId : uIds) {
            users.add(uId);
        }
        TEMP_CHAT_MAP.put(tempChatId, users);
        // 返回会话 ID
        return tempChatId;
    }

    /** 移除一个临时聊天 */
    public static void removeTempChat(int tempChatId) {
        TEMP_CHAT_MAP.remove(tempChatId);
    }

    /** 获取一个临时会话的用户 */
    public static Set<Integer> getTempChatUsers(int tempChatId) {
        return TEMP_CHAT_MAP.get(tempChatId);
    }
}
