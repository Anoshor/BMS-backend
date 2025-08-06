package com.bms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LoginRequest {
    
    @NotBlank(message = "Email or phone is required")
    private String identifier; // Can be email or phone
    
    @NotBlank(message = "Password is required")
    private String password;
    
    private String deviceId;
    
    @Pattern(regexp = "^(ios|android|web)$", message = "Device type must be ios, android, or web")
    private String deviceType = "android";
    
    private String ipAddress;
    private String userAgent;
    
    // Constructors
    public LoginRequest() {}
    
    public LoginRequest(String identifier, String password, String deviceId) {
        this.identifier = identifier;
        this.password = password;
        this.deviceId = deviceId;
    }
    
    // Getters and Setters
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getDeviceType() {
        return deviceType;
    }
    
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}