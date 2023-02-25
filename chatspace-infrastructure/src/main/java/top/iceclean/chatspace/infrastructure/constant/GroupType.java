package top.iceclean.chatspace.infrastructure.constant;

/**
 * 群组类型
 * @author : Ice'Clean
 * @date : 2022-12-13
 */
public enum GroupType {
    /** 用户群组 */
    USER(0),
    /** 系统群组 */
    SYSTEM(1);

    private final int value;

    GroupType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
