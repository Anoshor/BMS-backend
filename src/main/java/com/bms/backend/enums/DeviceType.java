package com.bms.backend.enums;

public enum DeviceType {
    IOS("ios", "iOS Device"),
    ANDROID("android", "Android Device"),
    WEB("web", "Web Browser");
    
    private final String code;
    private final String displayName;
    
    DeviceType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static DeviceType fromCode(String code) {
        for (DeviceType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return ANDROID; // Default fallback
    }
}