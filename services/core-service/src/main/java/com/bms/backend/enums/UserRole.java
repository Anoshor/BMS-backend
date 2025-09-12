package com.bms.backend.enums;

public enum UserRole {
    TENANT("tenant", "Property Tenant"),
    PROPERTY_MANAGER("property_manager", "Property Manager"),
    BUILDING_OWNER("building_owner", "Building Owner");
    
    private final String code;
    private final String displayName;
    
    UserRole(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static UserRole fromCode(String code) {
        for (UserRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown user role code: " + code);
    }
}