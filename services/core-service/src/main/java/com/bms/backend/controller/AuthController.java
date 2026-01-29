package com.bms.backend.controller;

import com.bms.backend.dto.request.LoginRequest;
import com.bms.backend.dto.request.RefreshTokenRequest;
import com.bms.backend.dto.request.SignupRequest;
import com.bms.backend.dto.request.UpdateContactInfoRequest;
import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.dto.response.AuthResponse;
import com.bms.backend.dto.response.UserDto;
import com.bms.backend.entity.User;
import com.bms.backend.enums.AccountStatus;
import com.bms.backend.enums.DeviceType;
import com.bms.backend.enums.UserRole;
import com.bms.backend.service.JwtService;
import com.bms.backend.service.OtpService;
import com.bms.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @Autowired
    private OtpService otpService;
    
    // ============================================
    // PRE-SIGNUP OTP ENDPOINTS (No auth required)
    // ============================================

    @PostMapping("/otp/send-email")
    public ResponseEntity<ApiResponse<String>> sendEmailOtp(@RequestParam String email) {
        try {
            // Basic email validation
            if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid email address"));
            }

            // Check if email is already registered
            if (userService.existsByEmail(email)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email is already registered"));
            }

            otpService.generateAndSendEmailVerificationOtp(email);
            return ResponseEntity.ok(ApiResponse.success(null, "OTP sent successfully to your email"));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to send OTP. Please try again."));
        }
    }

    @PostMapping("/otp/send-phone")
    public ResponseEntity<ApiResponse<String>> sendPhoneOtp(@RequestParam String phone) {
        try {
            // Basic phone validation
            if (phone == null || phone.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid phone number"));
            }

            // Check if phone is already registered
            if (userService.existsByPhone(phone)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Phone number is already registered"));
            }

            otpService.generateAndSendPhoneVerificationOtp(phone);
            return ResponseEntity.ok(ApiResponse.success(null, "OTP sent successfully to your phone"));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to send OTP. Please try again."));
        }
    }

    @PostMapping("/otp/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmailOtp(
            @RequestParam String email,
            @RequestParam String otp) {
        try {
            boolean isValid = otpService.verifyEmailOtp(email, otp);

            if (isValid) {
                return ResponseEntity.ok(ApiResponse.success(null, "Email verified successfully"));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid or expired OTP"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Verification failed. Please try again."));
        }
    }

    @PostMapping("/otp/verify-phone")
    public ResponseEntity<ApiResponse<String>> verifyPhoneOtp(
            @RequestParam String phone,
            @RequestParam String otp) {
        try {
            boolean isValid = otpService.verifyPhoneOtp(phone, otp);

            if (isValid) {
                return ResponseEntity.ok(ApiResponse.success(null, "Phone verified successfully"));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid or expired OTP"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Verification failed. Please try again."));
        }
    }

    // ============================================
    // LOGIN VIA OTP ENDPOINTS (Passwordless Login)
    // ============================================

    /**
     * Send OTP for passwordless login.
     * User must already exist and have an ACTIVE account.
     *
     * @param identifier Phone number or email
     * @param type "phone" or "email"
     * @param role "TENANT" or "MANAGER"
     */
    @PostMapping("/login/send-otp")
    public ResponseEntity<ApiResponse<String>> sendLoginOtp(
            @RequestParam String identifier,
            @RequestParam String type,
            @RequestParam String role) {
        try {
            // Validate type parameter
            boolean isPhone = "phone".equalsIgnoreCase(type);
            if (!isPhone && !"email".equalsIgnoreCase(type)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid type. Must be 'phone' or 'email'"));
            }

            // Find user by identifier
            Optional<User> userOpt = isPhone
                    ? userService.findByPhone(identifier)
                    : userService.findByEmail(identifier);

            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("No account found with this " + type));
            }

            User user = userOpt.get();

            // Validate role matches
            UserRole expectedRole = role.toUpperCase().equals("MANAGER")
                    ? UserRole.PROPERTY_MANAGER : UserRole.TENANT;
            if (user.getRole() != expectedRole) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("This " + type + " is registered with a different account type"));
            }

            // Check account status - allow ACTIVE accounts and PENDING accounts (they might be completing verification)
            if (user.getAccountStatus() == AccountStatus.SUSPENDED ||
                user.getAccountStatus() == AccountStatus.DEACTIVATED) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Account is not active. Please contact support."));
            }

            // Send OTP
            otpService.generateAndSendLoginOtp(identifier, isPhone);

            return ResponseEntity.ok(ApiResponse.success(null,
                    "OTP sent successfully to your " + type));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to send OTP. Please try again."));
        }
    }

    /**
     * Verify OTP and return tokens for passwordless login.
     *
     * @param identifier Phone number or email
     * @param otp The OTP code
     * @param role "TENANT" or "MANAGER"
     * @param deviceId Optional device ID for refresh token
     * @param deviceType Optional device type (android, ios, web)
     */
    @PostMapping("/login/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyLoginOtp(
            @RequestParam String identifier,
            @RequestParam String otp,
            @RequestParam String role,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false, defaultValue = "android") String deviceType) {
        try {
            // Verify OTP
            boolean isValid = otpService.verifyLoginOtp(identifier, otp);
            if (!isValid) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid or expired OTP"));
            }

            // Find user (could be by phone or email)
            Optional<User> userOpt = userService.findByPhone(identifier);
            if (userOpt.isEmpty()) {
                userOpt = userService.findByEmail(identifier);
            }

            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("User not found"));
            }

            User user = userOpt.get();

            // Validate role
            UserRole expectedRole = role.toUpperCase().equals("MANAGER")
                    ? UserRole.PROPERTY_MANAGER : UserRole.TENANT;
            if (user.getRole() != expectedRole) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid account type"));
            }

            // Check account status
            if (user.getAccountStatus() != AccountStatus.ACTIVE) {
                String message = switch (user.getAccountStatus()) {
                    case PENDING -> "Account verification is pending. Please complete verification first.";
                    case SUSPENDED -> "Account is suspended. Please contact support.";
                    case DEACTIVATED -> "Account is deactivated. Please contact support.";
                    default -> "Account is not active.";
                };
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(message));
            }

            // Generate tokens
            DeviceType devType = DeviceType.fromCode(deviceType);
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user, deviceId, devType);

            // Update last login
            userService.updateLastLogin(user);

            // Create response
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .requiresVerification(false)
                    .requiresDocuments(false)
                    .expiresIn(jwtService.getAccessTokenExpiration())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Login verification failed. Please try again."));
        }
    }

    // ============================================
    // SIGNUP & LOGIN ENDPOINTS
    // ============================================

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(
            @RequestBody @Valid SignupRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            request.setDeviceType(request.getDeviceType() != null ? request.getDeviceType() : "web");
            
            User user = userService.createUser(request);
            
            // For managers, don't return tokens until admin approval
            if (user.getRole() == UserRole.PROPERTY_MANAGER) {
                return ResponseEntity.ok(ApiResponse.success(null, 
                    "Manager registration successful. Please verify your email and phone. Your account will be activated once approved by admin."));
            }
            
            // For tenants, proceed with normal flow
            DeviceType deviceType = DeviceType.fromCode(request.getDeviceType());
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user, request.getDeviceId(), deviceType);
            
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .requiresVerification(true)
                    .requiresDocuments(false)
                    .expiresIn(jwtService.getAccessTokenExpiration())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(authResponse, 
                "Tenant registration successful. Please verify your email and phone."));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Registration failed. Please try again."));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            // Set IP address and user agent from request
            request.setIpAddress(getClientIpAddress(httpRequest));
            request.setUserAgent(httpRequest.getHeader("User-Agent"));
            request.setDeviceType(request.getDeviceType() != null ? request.getDeviceType() : "android");
            
            // Validate role and find user
            User user = userService.validateLoginWithRole(request.getIdentifier(), request.getRole());
            
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
                    .requiresVerification(false)
                    .requiresDocuments(false)
                    .expiresIn(jwtService.getAccessTokenExpiration())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
            
        } catch (Exception e) {
            e.printStackTrace(); // Add logging to see the actual error
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Login failed. Please try again."));
        }
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody @Valid RefreshTokenRequest request) {
        
        try {
            String refreshToken = request.getRefreshToken();
            
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
            
            // Generate new access token AND new refresh token (rotation)
            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user, request.getDeviceId(), DeviceType.fromCode(request.getDeviceType()));
            
            // Create response with both new tokens
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
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
            @RequestBody @Valid RefreshTokenRequest request) {
        
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
            @RequestBody @Valid RefreshTokenRequest request) {
        
        try {
            // Extract user from token
            String userId = jwtService.extractUserId(request.getRefreshToken());
            
            // In a complete implementation, you would:
            // 1. Revoke all refresh tokens for this user
            // 2. Add all active tokens to blacklist
            
            return ResponseEntity.ok(ApiResponse.success(null, "Logged out from all devices successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Logout from all devices failed"));
        }
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(
            @RequestParam String email,
            @RequestParam String otp) {
        
        try {
            userService.verifyEmail(email, otp);
            return ResponseEntity.ok(ApiResponse.success(null, "Email verified successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Email verification failed"));
        }
    }
    
    @PostMapping("/verify-phone")
    public ResponseEntity<ApiResponse<String>> verifyPhone(
            @RequestParam String phone,
            @RequestParam String otp) {
        
        try {
            userService.verifyPhone(phone, otp);
            return ResponseEntity.ok(ApiResponse.success(null, "Phone verified successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Phone verification failed"));
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> getProfile(
            @AuthenticationPrincipal User user) {
        
        try {
            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("User not authenticated"));
            }
            
            UserDto userDto = UserDto.from(user);
            return ResponseEntity.ok(ApiResponse.success(userDto, "Profile retrieved successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve profile"));
        }
    }
    
    @PutMapping("/update-contact")
    public ResponseEntity<ApiResponse<UserDto>> updateContactInfo(
            @RequestBody @Valid UpdateContactInfoRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        try {
            User updatedUser = userService.updateContactInfo(currentUser, request);
            UserDto userDto = UserDto.from(updatedUser);
            return ResponseEntity.ok(ApiResponse.success(userDto, "Contact information updated successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update contact information"));
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