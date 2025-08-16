package com.bms.backend.controller;

import com.bms.backend.dto.request.OtpVerificationRequest;
import com.bms.backend.dto.request.TenantRegistrationRequest;
import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.dto.response.AuthResponse;
import com.bms.backend.dto.response.UserDto;
import com.bms.backend.entity.User;
import com.bms.backend.enums.DeviceType;
import com.bms.backend.service.JwtService;
import com.bms.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/tenant")
@CrossOrigin(origins = "*")
public class TenantAuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerTenant(
            @RequestBody @Valid TenantRegistrationRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            // Set IP address for tracking
            request.setDeviceType(request.getDeviceType() != null ? request.getDeviceType() : "android");
            
            // Create tenant user
            User user = userService.createTenantUser(request);
            
            // Generate tokens
            DeviceType deviceType = DeviceType.fromCode(request.getDeviceType());
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user, request.getDeviceId(), deviceType);
            
            // Create response
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(UserDto.from(user))
                    .requiresVerification(true)
                    .requiresDocuments(false)
                    .expiresIn(jwtService.getAccessTokenExpiration())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(authResponse, "Tenant registration successful. Please verify your email and phone."));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Registration failed. Please try again."));
        }
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<UserDto>> verifyEmail(
            @RequestBody @Valid OtpVerificationRequest request) {
        
        try {
            if (!"email_verification".equals(request.getOtpType())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid OTP type for email verification"));
            }
            
            User user = userService.verifyEmail(request.getIdentifier(), request.getOtpCode());
            UserDto userDto = UserDto.from(user);
            
            String message = user.isVerificationComplete() ? 
                    "Email verified successfully. Account is now active." :
                    "Email verified successfully. Please verify your phone number to activate your account.";
            
            return ResponseEntity.ok(ApiResponse.success(userDto, message));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Email verification failed. Please try again."));
        }
    }
    
    @PostMapping("/verify-phone")
    public ResponseEntity<ApiResponse<UserDto>> verifyPhone(
            @RequestBody @Valid OtpVerificationRequest request) {
        
        try {
            if (!"phone_verification".equals(request.getOtpType())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid OTP type for phone verification"));
            }
            
            User user = userService.verifyPhone(request.getIdentifier(), request.getOtpCode());
            UserDto userDto = UserDto.from(user);
            
            String message = user.isVerificationComplete() ? 
                    "Phone verified successfully. Account is now active." :
                    "Phone verified successfully. Please verify your email to activate your account.";
            
            return ResponseEntity.ok(ApiResponse.success(userDto, message));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Phone verification failed. Please try again."));
        }
    }
    
    @PostMapping("/resend-email-verification")
    public ResponseEntity<ApiResponse<String>> resendEmailVerification(
            @RequestParam String email) {
        
        try {
            userService.resendEmailVerification(email);
            return ResponseEntity.ok(ApiResponse.success(null, "Verification email sent successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to send verification email. Please try again."));
        }
    }
    
    @PostMapping("/resend-phone-verification")
    public ResponseEntity<ApiResponse<String>> resendPhoneVerification(
            @RequestParam String phone) {
        
        try {
            userService.resendPhoneVerification(phone);
            return ResponseEntity.ok(ApiResponse.success(null, "Verification SMS sent successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to send verification SMS. Please try again."));
        }
    }
}