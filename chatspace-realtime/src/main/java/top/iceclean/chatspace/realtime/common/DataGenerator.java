package top.iceclean.chatspace.realtime.common;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.iceclean.chatspace.cache.*;
import top.iceclean.chatspace.infrastructure.constant.MessageType;
import top.iceclean.chatspace.infrastructure.constant.SessionType;
import top.iceclean.chatspace.infrastructure.dto.MessageDTO;
import top.iceclean.chatspace.infrastructure.po.SessionRequest;
import top.iceclean.chatspace.infrastructure.po.Zone;
import top.iceclean.chatspace.infrastructure.vo.*;
import top.iceclean.feign.*;

import java.util.*;

/**
 * 生成 websocket 所需的数据
 * @author : Ice'Clean
 * @date : 2022-06-03
 */
@Component
public class DataGenerator {
    /** 内部函数式接口，用于生成相应消息 */
    public interface Generator {
        /**
         * 获取该消息的目标用户 ID
         * @return 用户 ID 集合
         */
        Set<Integer> target();

        /**
         * 生成发送消息所需的对象数据
         * @param toUserId 目标用户 ID
         * @return 消息对象数据
         */
        Object exec(int toUserId);
    }

    private static UserClient userClient;
    private static FriendClient friendClient;
    private static GroupClient groupClient;

    /** 会话缓存 */
    private static SessionCache sessionCache;
    private static GroupCache groupCache;
    private static UserCache userCache;
    private static SpaceCache spaceCache;
    private static ZoneCache zoneCache;

    /** 注入用户服务 */
    @Autowired
    public void setUserClient(UserClient userClient) {
        DataGenerator.userClient = userClient;
    }

    /** 注入朋友服务 */
    @Autowired
    public void setFriendService(FriendClient friendClient) {
        DataGenerator.friendClient = friendClient;
    }

    /** 注入群聊服务 */
    @Autowired
    public void setGroupService(GroupClient groupClient) {
        DataGenerator.groupClient = groupClient;
    }

    /** 注入会话缓存服务 */
    @Autowired
    public void setSessionCache(SessionCache sessionCache) {
        DataGenerator.sessionCache = sessionCache;
    }

    /** 注入用户缓存服务 */
    @Autowired
    public void setGroupCache(GroupCache groupCache) {
        DataGenerator.groupCache = groupCache;
    }

    /** 注入用户缓存服务 */
    @Autowired
    public void setUserCache(UserCache userCache) {
        DataGenerator.userCache = userCache;
    }

    @Autowired
    public void setSpaceCache(SpaceCache spaceCache) {
        DataGenerator.spaceCache = spaceCache;
    }

    @Autowired
    public void setZoneCache(ZoneCache zoneCache) {
        DataGenerator.zoneCache = zoneCache;
    }

    /** 聊天消息生成器 */
    public static class ChatMessage implements Generator {
        /** 要发送的消息 */
        private final MessageVO messageVO;

        public ChatMessage(MessageDTO messageDTO) {
            // 获取发送的用户
            UserVO sender = userCache.wrapUser(userClient.getUserById(messageDTO.getSenderId()));
            this.messageVO = new MessageVO(messageDTO, sender);
        }

        @Override
        public Set<Integer> target() {
            // 会话中的所有在线的用户 ID，就是目标通知用户 ID
            return sessionCache.getSessionUsers(messageVO.sessionType(), messageVO.getTargetId());
        }

        @Override
        public MessageVO exec(int toUserId) {
            return messageVO.setSelf(toUserId);
        }
    }

    /** 用户上线状态消息生成器 */
    public static class UserOnline implements Generator {
        /** 用户的 ID */
        private final Integer userId;
        /** 要发送的消息 */
        private final UserOnlineVO userOnlineVO;

        public UserOnline(int userId, boolean online) {
            // 获得必要的元数据
            this.userId = userId;
            this.userOnlineVO = new UserOnlineVO(userId, online);
        }

        @Override
        public Set<Integer> target() {
            // 初始化目标集合，首先找到的是该用户所有的好友
            Set<Integer>  targetUserIdSet = new HashSet<>(friendClient.getFriendUserIdList(userId));
            // 然后找到所有和该用户在同一个群聊中的用户 ID
            for (Integer groupKey : groupClient.getGroupIdList(userId)) {
                targetUserIdSet.addAll(groupClient.getUserIdList(groupKey));
            }
            // 最后除去本身，即使所有需要通知的对象
            targetUserIdSet.remove(userId);
            return targetUserIdSet;
        }

        @Override
        public UserOnlineVO exec(int toUserId) {
            return userOnlineVO;
        }
    }

    /** 会话请求消息生成器 */
    public static class RequestMessage implements Generator {
        /** 会话请求实体 */
        private final SessionRequest request;
        /** 发送者响应对象 */
        private final UserVO sender;
        /** 发送者申请加入的群聊对象 */
        private GroupVO group = null;

        public RequestMessage(SessionRequest request) {
            this.request = request;
            // 拿到发送者响应对象
            this.sender = userCache.wrapUser(userClient.getUserById(request.getSenderId()));
            // 在群聊请求时，拿到发送者请求的群的响应对象
            if (request.getType() == SessionType.GROUP.value()) {
                this.group = groupCache.getGroupVO(request.getTargetId());
            }
        }

        @Override
        public Set<Integer> target() {
            Set<Integer> targetUserIdSet = new HashSet<>();
            // 如果是群聊的话，需要发送给群主（或管理员，待增加）,否则，直接发送给指定的用户
            targetUserIdSet.add(
                    request.getType() == SessionType.GROUP.value() ?
                    group.getCreatorId() : request.getTargetId());
            return targetUserIdSet;
        }

        @Override
        public Object exec(int toUserId) {
            // 他人申请，附上发送者信息，不需要自己的信息
            return new SessionRequestVO(request, toUserId).setUser(sender).setGroup(group);
        }
    }

    /** token 过期消息生成器 */
    @AllArgsConstructor
    public static class TokenExpire implements Generator {
        /** token 过期的用户 ID */
        private final Integer userId;

        @Override
        public Set<Integer> target() {
            // 发送给用户自己
            HashSet<Integer> targetId = new HashSet<>(1);
            targetId.add(userId);
            return targetId;
        }

        @Override
        public Object exec(int toUserId) {
            return "用户 ID=" + toUserId + " token 失效，断开连接";
        }
    }

    /** 位置状态更改消息生成器（用户的发现、消失和更新） */
    public static class SiteChange implements Generator {
        /** 消息的目标用户集合 */
        private final Set<Integer> targetSet;
        /** 消息内容中的用户 */
        private final Set<SiteVO> infoSet;

        public SiteChange(int userId, Set<Integer> userIdSet) {
            // 当前用户单独为目标用户
            targetSet = new HashSet<>(1);
            targetSet.add(userId);

            // 而消息的内容是集合中用户对位置信息
            infoSet = new HashSet<>(userIdSet.size());
            userIdSet.forEach(uId -> infoSet.add(SessionRepository.getSite(uId)));
        }

        public SiteChange(Set<Integer> userIdSet, int userId) {
            // 消息的目标用户是集合中的每一个用户
            targetSet = userIdSet;

            // 而消息的内容则是当前用户的位置信息（先设为空，需要动态添加
            infoSet = new HashSet<>(1);
            infoSet.add(SessionRepository.getSite(userId));
        }

        @Override
        public Set<Integer> target() {
            return targetSet;
        }

        @Override
        public Object exec(int toUserId) {
            return infoSet;
        }
    }

    /** 空间广播消息生成器 */
    public static class SpaceCast implements Generator {
        /** 消息的目标用户集合 */
        private final Set<Integer> targetSet;
        /** 要发送的消息 */
        private final MessageVO messageVO = new MessageVO();

        public SpaceCast(UserVO sender, String msg) {
            // 获取用户所在的空间
            int spaceId = spaceCache.getUserSpaceId(sender.getUserId());
            // 空间中所有的用户为目标用户
            targetSet = sessionCache.getSessionUsers(SessionType.SPACE, spaceId);
            // 构建消息
            initMessageVO(sender, spaceId, msg);
        }

        public SpaceCast(int spaceId, String msg) {
            // 指定空间中所有的用户为目标用户
            targetSet = spaceCache.getSpaceUsers(spaceId);
            // 构建消息，sender 是 null 表示是系统广播，只有系统可以直接指定要发送到哪个空间
            initMessageVO(null, spaceId, msg);
        }

        public void initMessageVO(UserVO sender, int spaceId, String msg) {
            messageVO.setSender(sender);
            messageVO.setSessionType(SessionType.SPACE);
            messageVO.setTargetId(spaceId);
            messageVO.setMessageType(MessageType.TEXT);
            messageVO.setContent(msg);
        }

        @Override
        public Set<Integer> target() {
            return targetSet;
        }

        @Override
        public Object exec(int toUserId) {
            return messageVO.setSelf(toUserId);
        }
    }

    /** 世界广播消息生成器 */
    public static class UniverseCast implements Generator {
        /** 消息的目标用户集合 */
        private final Set<Integer> targetSet;
        /** 要发送的消息 */
        private final MessageVO messageVO = new MessageVO();

        public UniverseCast(UserVO sender, String msg) {
            // 在线的所有用户为目标用户
            targetSet = userCache.getAllUsers();
            // 构建消息
            initMessageVO(sender, -1, msg);
        }

        public UniverseCast(String msg) {
            // 在线的所有用户为目标用户
            targetSet = userCache.getAllUsers();
            // 构建消息，sender 是 null 表示是系统广播，只有系统可以直接指定要发送到哪个空间
            initMessageVO(null, -1, msg);
        }

        public void initMessageVO(UserVO sender, int spaceId, String msg) {
            messageVO.setSender(sender);
            messageVO.setSessionType(SessionType.UNIVERSE);
            messageVO.setTargetId(spaceId);
            messageVO.setMessageType(MessageType.TEXT);
            messageVO.setContent(msg);
        }

        @Override
        public Set<Integer> target() {
            return targetSet;
        }

        @Override
        public Object exec(int toUserId) {
            return messageVO.setSelf(toUserId);
        }
    }

    @AllArgsConstructor
    public static class NewZoneCast implements Generator {
        private final ZoneVO zoneVO;

        @Override
        public Set<Integer> target() {
            // 父级 ID 为 0，则获取空间中的所有用户
            if (zoneVO.getZonePid() == 0) {
                return spaceCache.getSpaceUsers(zoneVO.getSpaceId());
            }
            // 区域所在父级区域的所有用户，为目标通知用户
            return zoneCache.getZoneUsers(zoneVO.getZonePid());
        }

        @Override
        public Object exec(int toUserId) {
            return zoneVO;
        }
    }
}
