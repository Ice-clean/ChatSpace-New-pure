package top.iceclean.chatspace.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.iceclean.chatspace.cache.UserCache;
import top.iceclean.chatspace.friend.mapper.FriendMapper;
import top.iceclean.chatspace.friend.service.FriendService;
import top.iceclean.chatspace.infrastructure.auth.UserAuth;
import top.iceclean.chatspace.infrastructure.constant.ResponseStatusEnum;
import top.iceclean.chatspace.infrastructure.constant.SessionType;
import top.iceclean.chatspace.infrastructure.po.Friend;
import top.iceclean.chatspace.infrastructure.po.Session;
import top.iceclean.chatspace.infrastructure.po.User;
import top.iceclean.chatspace.infrastructure.pojo.Response;
import top.iceclean.chatspace.infrastructure.utils.DateUtils;
import top.iceclean.chatspace.infrastructure.vo.FriendVO;
import top.iceclean.feign.SessionClient;
import top.iceclean.feign.UserClient;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : Ice'Clean
 * @date : 2022-05-25
 */
@Service
@Slf4j
public class FriendServiceImpl implements FriendService {
    @Autowired
    private FriendMapper friendMapper;
    @Autowired
    private UserClient userClient;
    @Lazy
    @Autowired
    private SessionClient sessionClient;

    /** 用户缓存服务 */
    private final UserCache userCache;

    public FriendServiceImpl(UserCache userCache) {
        this.userCache = userCache;
    }

    @Override
    @Transactional
    public boolean becomeFriends(int userId, int toUserId) {
        // 首先去申请一个会话，会话 ID 作为他们之间的好友 ID
        Session session = sessionClient.createSession(SessionType.FRIEND, -1);
        if (session == null) {
            log.info("申请会话失败！");
            return false;
        }

        // 建立双向的映射关系，并返回是否执行成功
        friendMapper.insert(new Friend(session.getId(), userId, toUserId));
        friendMapper.insert(new Friend(session.getId(), toUserId, userId));
        return true;
    }

    @Override
    public Friend getFriend(int friendId, int userId) {
        return friendMapper.selectOne(new LambdaQueryWrapper<Friend>()
                .isNull(Friend::getDeleteTime)
                .eq(Friend::getFriendId, friendId)
                .eq(Friend::getUserId, userId));
    }

    @Override
    public Friend getFriendByUser(int userId, int toUserId) {
        return friendMapper.selectOne(new LambdaQueryWrapper<Friend>()
                .isNull(Friend::getDeleteTime)
                .eq(Friend::getUserId, userId)
                .eq(Friend::getToUserId, toUserId));
    }

    @Override
    public FriendVO getFriendVO(int friendId, int userId) {
        // 获取用户在该会话中的好友关系
        Friend friend = getFriend(friendId, userId);
        // 获取好友的用户对象
        User friendUser = userClient.getUserById(friend.getToUserId());
        // 构造好友的响应对象
        return new FriendVO(userCache.wrapUser(friendUser), friend);
    }

    @Override
    public List<Integer> getFriendUserId(int friendId) {
        // 找到对应的朋友记录，将发送方和接收方 ID 放入集合中即可
        Friend friend = friendMapper.selectOne(new LambdaQueryWrapper<Friend>()
                .isNull(Friend::getDeleteTime)
                .eq(Friend::getFriendId, friendId).last("limit 1"));
        return Arrays.asList(friend.getUserId(), friend.getToUserId());
    }

    @Override
    public List<Integer> getFriendIdList(int userId) {
        return friendMapper.selectList(new LambdaQueryWrapper<Friend>()
                .select(Friend::getFriendId)
                .isNull(Friend::getDeleteTime)
                .eq(Friend::getUserId, userId))
                .stream().map(Friend::getFriendId).collect(Collectors.toList());
    }

    @Override
    public Integer getFriendId(int userId1, int userId2) {
        return friendMapper.selectOne(new LambdaQueryWrapper<Friend>()
                .select(Friend::getFriendId)
                .isNull(Friend::getDeleteTime)
                .eq(Friend::getUserId, userId1)
                .eq(Friend::getToUserId, userId2)).getFriendId();
    }

    @Override
    public List<Integer> getFriendUserIdList(int userId) {
        return friendMapper.selectList(new LambdaQueryWrapper<Friend>()
                .select(Friend::getToUserId)
                .isNull(Friend::getDeleteTime)
                .eq(Friend::getUserId, userId))
                .stream().map(Friend::getToUserId).collect(Collectors.toList());
    }

    @Override
    public void updateLastMsgId(int friendId, int userId, int latestMsgId) {
        // 先获得好友映射，更新完最新消息 ID 再持久化
        Friend friend = getFriend(friendId, userId);
        friend.setLastMsgId(latestMsgId);
        friendMapper.updateById(friend);
    }

    @Override
    public Response getFriendList(int userId) {
        // 获取该用户所有的好友会话
        List<Integer> friendIdList = getFriendIdList(userId);
        List<FriendVO> friendList = friendIdList.stream().map(friendId ->
                getFriendVO(friendId, userId)).collect(Collectors.toList());
        return new Response(ResponseStatusEnum.OK).addData("friendList", friendList);
    }

    @Override
    public Response deleteFriend(int toUserId) {
        // 获取当前用户 ID
        int userId = UserAuth.getUserId();
        // 获取好友映射
        Friend friend = getFriendByUser(userId, toUserId);
        // 不为空的话表示是好友，才是合法操作
        if (friend != null) {
            // 将自己的好友映射标记为删除
            Date deleteTime = DateUtils.getDateTime();
            friend.setDeleteTime(deleteTime);
            friendMapper.updateById(friend);
            // 同时需要将对方也删除了
            friend = getFriendByUser(toUserId, userId);
            friend.setDeleteTime(deleteTime);
            friendMapper.updateById(friend);
            // 返回删除成功
            return new Response(ResponseStatusEnum.OK).setMsg("删除好友成功");
        }
        // 否则是非法操作
        return new Response(ResponseStatusEnum.NOT_FRIEND_ERROR);
    }
}
