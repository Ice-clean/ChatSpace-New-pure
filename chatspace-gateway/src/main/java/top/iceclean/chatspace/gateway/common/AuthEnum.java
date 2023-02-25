package top.iceclean.chatspace.gateway.common;

/**
 * @author : Ice'Clean
 * @date : 2022-02-07
 */
public enum AuthEnum {
    /** 鉴权错误码 */
    EMPTY_TOKEN(1100, "token is empty"),
    EXPIRE_TOKEN(1101, "token expired"),
    ILLEGAL_TOKEN(1102, "token illegal");

    /** 状态码 */
    private final int value;

    /** 附加信息 */
    private final String msg;

    AuthEnum(int value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    public int value() {
        return value;
    }

    public String msg() {
        return msg;
    }
}
