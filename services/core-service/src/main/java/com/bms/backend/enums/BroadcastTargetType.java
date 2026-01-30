package com.bms.backend.enums;

public enum BroadcastTargetType {
    SINGLE_TENANT("single_tenant", "Single Tenant"),
    MULTIPLE_TENANTS("multiple_tenants", "Multiple Tenants"),
    SPECIFIC_PROPERTY("specific_property", "Specific Property"),
    ALL_TENANTS("all_tenants", "All Tenants");

    private final String code;
    private final String displayName;

    BroadcastTargetType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static BroadcastTargetType fromCode(String code) {
        for (BroadcastTargetType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown broadcast target type code: " + code);
    }
}
