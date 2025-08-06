package com.bms.backend.controller;

import com.bms.backend.dto.request.LoginRequest;
import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.dto.response.AuthResponse;
import com.bms.backend.dto.response.UserDto;
import com.bms.backend.entity.User;
import com.bms.backend.enums.AccountStatus;
import com.bms.backend.enums.DeviceType;
import com.bms.backend.service.JwtService;
import com.bms.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            // Set IP address and user agent from request
            request.setIpAddress(getClientIpAddress(httpRequest));
            request.setUserAgent(httpRequest.getHeader("User-Agent"));
            request.setDeviceType(request.getDeviceType() != null ? request.getDeviceType() : "android");
            
            // Find user by email or phone
            Optional<User> userOpt = userService.findByEmailOrPhone(request.getIdentifier());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid credentials"));
            }
            
            User user = userOpt.get();
            
            // Check if account is locked
            if (userService.isAccountLocked(user)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Account is temporarily locked due to multiple failed login attempts"));
            }
            
            // Validate password
            if (!userService.validatePassword(user, request.getPassword())) {
                userService.incrementFailedLoginAttempts(user);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid credentials"));
            }
            
            // Check account status
            if (user.getAccountStatus() != AccountStatus.ACTIVE) {
                String message = switch (user.getAccountStatus()) {
                    case PENDING -> "Account verification is pending. Please verify your email and phone number.";
                    case SUSPENDED -> "Account is suspended. Please contact support.";
                    case DEACTIVATED -> "Account is deactivated. Please contact support.";
                    default -> "Account is not active.";
                };
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(message));
            }
            
            // Generate tokens
            DeviceType deviceType = DeviceType.fromCode(request.getDeviceType());
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user, request.getDeviceId(), deviceType);
            
            // Update last login
            userService.updateLastLogin(user);
            
            // Create response
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(UserDto.from(user))
                    .requiresVerification(false)
                    .requiresDocuments(false)
                    .expiresIn(jwtService.getAccessTokenExpiration())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Login failed. Please try again."));
        }
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestParam String refreshToken) {
        
        try {
            // Validate refresh token
            if (!jwtService.isValidToken(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid refresh token"));
            }
            
            // Extract user from token
            String userId = jwtService.extractUserId(refreshToken);
            Optional<User> userOpt = userService.findById(java.util.UUID.fromString(userId));
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid refresh token"));
            }
            
            User user = userOpt.get();
            
            // Check if account is still active
            if (user.getAccountStatus() != AccountStatus.ACTIVE) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Account is not active"));
            }
            
            // Generate new access token
            String newAccessToken = jwtService.generateAccessToken(user);
            
            // Create response (only return new access token, keep same refresh token)
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .user(UserDto.from(user))
                    .requiresVerification(false)
                    .requiresDocuments(false)
                    .expiresIn(jwtService.getAccessTokenExpiration())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token refresh failed"));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestParam String refreshToken) {
        
        try {
            // In a complete implementation, you would:
            // 1. Add the refresh token to a blacklist
            // 2. Remove it from the refresh_tokens table
            // 3. Optionally blacklist the access token as well
            
            // For now, just return success
            return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Logout failed"));
        }
    }
    
    @PostMapping("/logout-all-devices")
    public ResponseEntity<ApiResponse<String>> logoutAllDevices(
            @RequestParam String refreshToken) {
        
        try {
            // Extract user from token
            String userId = jwtService.extractUserId(refreshToken);
            
            // In a complete implementation, you would:
            // 1. Revoke all refresh tokens for this user
            // 2. Add all active tokens to blacklist
            
            return ResponseEntity.ok(ApiResponse.success(null, "Logged out from all devices successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Logout from all devices failed"));
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}