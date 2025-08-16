package com.bms.backend.enums;

public enum AccountStatus {
    PENDING("pending", "Account pending verification"),
    ACTIVE("active", "Account is active"),
    SUSPENDED("suspended", "Account is suspended"),
    DEACTIVATED("deactivated", "Account is deactivated");
    
    private final String code;
    private final String description;
    
    AccountStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static AccountStatus fromCode(String code) {
        for (AccountStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown account status code: " + code);
    }
}