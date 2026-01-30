package com.bms.backend.enums;

public enum BroadcastPriority {
    INFO("info", "Information"),
    WARNING("warning", "Warning"),
    URGENT("urgent", "Urgent");

    private final String code;
    private final String displayName;

    BroadcastPriority(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static BroadcastPriority fromCode(String code) {
        for (BroadcastPriority priority : values()) {
            if (priority.code.equals(code)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown broadcast priority code: " + code);
    }
}
