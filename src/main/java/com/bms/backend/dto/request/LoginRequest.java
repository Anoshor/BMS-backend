package com.bms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Email or phone is required")
    private String identifier;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(TENANT|MANAGER|tenant|manager)$", message = "Role must be TENANT or MANAGER")
    private String role;
    
    private String deviceId;
    
    @Pattern(regexp = "^(ios|android|web)$", message = "Device type must be ios, android, or web")
    private String deviceType = "web";
    
    private String ipAddress;
    private String userAgent;
}