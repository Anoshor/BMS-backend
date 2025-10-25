package com.bms.backend.controller;

import com.bms.backend.dto.request.ChangePasswordRequest;
import com.bms.backend.dto.request.ForgotPasswordRequest;
import com.bms.backend.dto.request.PasswordResetRequest;
import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.entity.User;
import com.bms.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Password Management", description = "Password change and reset APIs for all user types")
public class PasswordResetController {

    @Autowired
    private UserService userService;

    /**
     * Change password for logged-in user (requires authentication)
     * Works for all user types (TENANT, MANAGER, ADMIN)
     * No email/SMS service required - uses old password verification
     */
    @PostMapping("/change-password")
    @Operation(
            summary = "Change password",
            description = "Change password for logged-in user by verifying old password. Works for all user types."
    )
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            userService.changePassword(user, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(new ApiResponse<>(true, null,
                    "Password changed successfully. Please login with your new password."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null,
                            "Failed to change password: " + e.getMessage()));
        }
    }

    /**
     * Step 1: Request password reset - sends OTP to email
     * Works for all user types (TENANT, MANAGER, ADMIN)
     * NOTE: Requires email service to be configured (Twilio/AWS SES)
     */
    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request password reset",
            description = "Sends OTP to user's email for password reset. Works for all user types."
    )
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            userService.initiatePasswordReset(request.getEmail());
            return ResponseEntity.ok(new ApiResponse<>(true, null,
                    "Password reset OTP sent to your email. Please check your inbox."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null,
                            "Failed to send password reset email: " + e.getMessage()));
        }
    }

    /**
     * Step 2: Reset password using OTP
     * Works for all user types (TENANT, MANAGER, ADMIN)
     * NOTE: Requires email service to be configured (Twilio/AWS SES)
     */
    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password with OTP",
            description = "Resets user password using OTP verification. Works for all user types. Requires email service."
    )
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            userService.resetPassword(request.getEmail(), request.getOtpCode(), request.getNewPassword());
            return ResponseEntity.ok(new ApiResponse<>(true, null,
                    "Password reset successful. You can now login with your new password."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null,
                            "Failed to reset password: " + e.getMessage()));
        }
    }
}
