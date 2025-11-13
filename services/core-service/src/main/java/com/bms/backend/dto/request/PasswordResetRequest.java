package com.bms.backend.dto.request;

public class PasswordResetRequest {

    private String email;
    private String otpCode;
    private String newPassword;

    // Constructors
    public PasswordResetRequest() {}

    public PasswordResetRequest(String email, String otpCode, String newPassword) {
        this.email = email;
        this.otpCode = otpCode;
        this.newPassword = newPassword;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
