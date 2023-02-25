package top.iceclean.chatspace.infrastructure.constant;

/**
 * 消息类型
 * @author : Ice'Clean
 * @date : 2022-12-02
 */
public enum MessageType {
    /** 文本类型 */
    TEXT(0),
    /** 图片类型 */
    IMAGE(1),
    /** 语音类型 */
    VOICE(2),
    /** 文件类型 */
    FILE(3);

    private final int value;

    MessageType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static MessageType get(int type) {
        MessageType messageType;
        switch (type) {
            case 0: messageType = TEXT; break;
            case 1: messageType = IMAGE; break;
            case 2: messageType = VOICE; break;
            case 3: messageType = FILE; break;
            default: messageType = null;
        }
        return messageType;
    }
}
