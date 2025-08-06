package com.bms.backend.enums;

public enum OtpType {
    EMAIL_VERIFICATION("email_verification", "Email Verification"),
    PHONE_VERIFICATION("phone_verification", "Phone Verification"),
    PASSWORD_RESET("password_reset", "Password Reset"),
    LOGIN_VERIFICATION("login_verification", "Login Verification");
    
    private final String code;
    private final String displayName;
    
    OtpType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static OtpType fromCode(String code) {
        for (OtpType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown OTP type code: " + code);
    }
}