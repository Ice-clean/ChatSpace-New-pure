package top.iceclean.chatspace.realtime.common;

import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.iceclean.chatspace.infrastructure.po.Site;
import top.iceclean.chatspace.infrastructure.po.User;
import top.iceclean.chatspace.infrastructure.vo.SiteVO;
import top.iceclean.chatspace.infrastructure.vo.UserVO;
import top.iceclean.chatspace.realtime.constant.ChannelKey;
import top.iceclean.chatspace.realtime.service.SiteService;
import top.iceclean.chatspace.realtime.share.ServerSession;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 会话仓库，存放一个空间中的所有会话
 * 每个 realtime 服务单独为一个空间
 * 也能通过当前线程获取当前会话
 * @author : Ice'Clean
 * @date : 2022-10-28
 */
@Component
public class SessionRepository {
    /** 和当前线程绑定的用户 ID */
    private static final ThreadLocal<Integer> USER_ID_THREAD_LOCAL = new ThreadLocal<>();
    /** 保存所有的用户会话（ID -> Session） */
    private static final ConcurrentMap<Integer, ServerSession> USER_SESSION_MAP = new ConcurrentHashMap<>();

    private static SiteService siteService;

    @Autowired
    public void setSiteService(SiteService siteService) {
        SessionRepository.siteService = siteService;
    }

    public static ServerSession bind(Channel channel, User user, Site site) {
        // 将用户 ID 绑定到当前线程
        USER_ID_THREAD_LOCAL.set(user.getId());
        // 将用户、通道和位置信息绑定到会话中
        ServerSession serverSession = new ServerSession(channel, user, site);
        channel.attr(ChannelKey.SESSION_KEY).set(serverSession);
        USER_SESSION_MAP.put(user.getId(), serverSession);
        return serverSession;
    }

    public static void update(int userId) {
        // 由于存在线程复用，每一次都需要更新当前线程对应的用户 ID
        USER_ID_THREAD_LOCAL.set(userId);
    }

    public static void remove() {
        // 移除用户会话并将用户从线程中移除
        USER_SESSION_MAP.remove(USER_ID_THREAD_LOCAL.get());
        USER_ID_THREAD_LOCAL.remove();
    }

    /**
     * 获取当前用户的的会话
     * @return 会话实体
     */
    public static ServerSession get() {
        Integer userId = USER_ID_THREAD_LOCAL.get();
        // 用户消息没被抹除
        if (userId != null) {
            return get(userId);
        }
        return null;
    }

    /** 通过用户 ID 获取会话 */
    public static ServerSession get(int userId) {
        // TODO 在多机情况下可能会出问题
        return USER_SESSION_MAP.get(userId);
    }

    /** 通过通道获取会话 */
    public static ServerSession get(Channel channel) {
        return channel.attr(ChannelKey.SESSION_KEY).get();
    }


    /**
     * 获取当前用户
     * @return 当前用户的响应对象
     */
    public static UserVO getUser() {
        return get(USER_ID_THREAD_LOCAL.get()).getUser();
    }

    /**
     * 获取当前用户的位置信息
     * @return 当前用户位置信息的响应对象
     */
    public static SiteVO getSite() {
        return get(USER_ID_THREAD_LOCAL.get()).getSite();
    }

    /**
     * 获取指定用户的位置信息
     * @return 指定用户位置信息的响应对象
     */
    public static SiteVO getSite(int userId) {
        ServerSession serverSession = get(userId);
        if (serverSession == null) {
            // 如果为空的话，说明该用户连接的是其他的服务器，这时候需要从数据库获取位置信息
            return siteService.getSiteVO(userId);
        }
        // 否则直接返回
        return serverSession.getSite();
    }
}
