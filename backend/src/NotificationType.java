/**
 * Notification Type Enum
 * Defines the types of notifications supported by the system
 */
public enum NotificationType {
    SYSTEM("system", "系統公告"),
    BORROW("borrow", "借閱通知"),
    RETURN("return", "歸還通知"),
    REVIEW("review", "評論通知"),
    DUE("due", "到期提醒");

    private final String code;
    private final String description;

    NotificationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static NotificationType fromCode(String code) {
        for (NotificationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
