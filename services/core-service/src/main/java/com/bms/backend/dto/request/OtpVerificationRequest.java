package com.bms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class OtpVerificationRequest {
    
    @NotBlank(message = "Identifier (email or phone) is required")
    private String identifier;
    
    @NotBlank(message = "OTP code is required")
    @Size(min = 6, max = 6, message = "OTP code must be exactly 6 digits")
    @Pattern(regexp = "^\\d{6}$", message = "OTP code must contain only digits")
    private String otpCode;
    
    @NotBlank(message = "OTP type is required")
    @Pattern(regexp = "^(email_verification|phone_verification|password_reset|login_verification)$", 
             message = "Invalid OTP type")
    private String otpType;
    
    // Constructors
    public OtpVerificationRequest() {}
    
    public OtpVerificationRequest(String identifier, String otpCode, String otpType) {
        this.identifier = identifier;
        this.otpCode = otpCode;
        this.otpType = otpType;
    }
    
    // Getters and Setters
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public String getOtpCode() {
        return otpCode;
    }
    
    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
    
    public String getOtpType() {
        return otpType;
    }
    
    public void setOtpType(String otpType) {
        this.otpType = otpType;
    }
}