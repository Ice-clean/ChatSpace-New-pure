package top.iceclean.chatspace.message.service;

import top.iceclean.chatspace.infrastructure.dto.MessageDTO;
import top.iceclean.chatspace.infrastructure.po.Message;
import top.iceclean.chatspace.infrastructure.pojo.Response;
import top.iceclean.chatspace.infrastructure.vo.MessageVO;
import top.iceclean.feign.MessageClient;

/**
 * @author : Ice'Clean
 * @date : 2022-05-25
 */
public interface MessageService extends MessageClient {

    /**
     * 获取用户在某个接收域的历史消息
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param page 聊天记录的页数
     * @return 历史消息列表
     */
    Response getChatHistory(int userId, int sessionId, int page);

    /**
     * 将消息对象转化成消息响应对象
     * @param message 消息对象
     * @param userId 当前用户 ID
     * @param info 是否需要详细信息（会话消息列表需要）
     * @return 消息响应对象
     */
    @Override
    MessageVO toMessageVO(Message message, int userId, boolean info);

    /**
     * 将消息接收对象持久化，并返回消息实体
     * @param messageDTO 消息接收对象
     */
    void saveMessage(MessageDTO messageDTO);

    /**
     * 获取指定会话最后一条消息的 ID
     * @param sessionId 会话 ID
     * @return 最后一条消息 ID
     */
    @Override
    int getLastMsgId(int sessionId);
}
