package top.iceclean.chatspace.infrastructure.constant;

/**
 * 物品类型枚举，value 为 item_id
 * @author : Ice'Clean
 * @date : 2022-12-13
 */
public enum ItemType {
    /** 空间币 */
    SPACE_COIN(1),
    /** 区域资源 */
    ZONE_SOURCE(2),
    /** 箱子资源 */
    BOX_SOURCE(3),
    /** 传送门资源 */
    PORTAL_SOURCE(4),
    /** 空间发送器 */
    SPACE_TRANSMITTER(5),
    /** 世界发送器 */
    UNIVERSE_TRANSMITTER(6),
    /** 纸张 */
    PAPER(7),
    /** 电子卡 */
    ELECTRONIC_CARD(8),
    /** 钥匙 */
    KEY(9);

    private final int value;

    ItemType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static ItemType get(int value) {
        for (ItemType itemType : ItemType.values()) {
            if (itemType.value == value) {
                return itemType;
            }
        }
        return null;
    }

    public static ItemType getBoxType(int type) {
        if (type == BOX_SOURCE.value()) {
            return BOX_SOURCE;
        }
        return null;
    }

    /** 已收检查的类型，必定满足箱子类型 */
    public static ItemType getCheckedBoxType(int type) {
        return BOX_SOURCE;
    }
}
