package com.bms.backend.controller;

import com.bms.backend.dto.request.CreateBroadcastRequest;
import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.dto.response.BroadcastMessageResponse;
import com.bms.backend.dto.response.TenantBroadcastMessageResponse;
import com.bms.backend.entity.User;
import com.bms.backend.enums.UserRole;
import com.bms.backend.service.BroadcastMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/broadcast")
@Tag(name = "Broadcast Messages", description = "APIs for managing broadcast messages between property managers and tenants")
public class BroadcastMessageController {

    @Autowired
    private BroadcastMessageService broadcastMessageService;

    // ==================== MANAGER ENDPOINTS ====================

    @Operation(summary = "Create a broadcast message",
               description = "Create a new broadcast message to send to tenants. Requires PROPERTY_MANAGER or BUILDING_OWNER role.")
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<BroadcastMessageResponse>> createBroadcast(
            @Valid @RequestBody CreateBroadcastRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            if (user.getRole() != UserRole.PROPERTY_MANAGER && user.getRole() != UserRole.BUILDING_OWNER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Only property managers can create broadcast messages"));
            }

            BroadcastMessageResponse response = broadcastMessageService.createBroadcast(request, user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, response, "Broadcast message created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to create broadcast message: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get all broadcasts for manager",
               description = "Retrieve all broadcast messages created by the logged-in manager with delivery statistics.")
    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<List<BroadcastMessageResponse>>> getManagerBroadcasts() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            if (user.getRole() != UserRole.PROPERTY_MANAGER && user.getRole() != UserRole.BUILDING_OWNER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Only property managers can view broadcast messages"));
            }

            List<BroadcastMessageResponse> responses = broadcastMessageService.getManagerBroadcasts(user);
            return ResponseEntity.ok(new ApiResponse<>(true, responses, "Broadcast messages retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve broadcast messages: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get broadcast details",
               description = "Retrieve detailed information about a specific broadcast message including delivery stats.")
    @GetMapping("/messages/{id}")
    public ResponseEntity<ApiResponse<BroadcastMessageResponse>> getBroadcastDetails(
            @Parameter(description = "Broadcast message ID") @PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            if (user.getRole() != UserRole.PROPERTY_MANAGER && user.getRole() != UserRole.BUILDING_OWNER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Only property managers can view broadcast details"));
            }

            BroadcastMessageResponse response = broadcastMessageService.getBroadcastDetails(id, user);
            return ResponseEntity.ok(new ApiResponse<>(true, response, "Broadcast message details retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve broadcast message details: " + e.getMessage()));
        }
    }

    @Operation(summary = "Expire a broadcast",
               description = "Manually expire a broadcast message before its expiration date. Message will no longer be visible to tenants.")
    @PutMapping("/messages/{id}/expire")
    public ResponseEntity<ApiResponse<BroadcastMessageResponse>> expireBroadcast(
            @Parameter(description = "Broadcast message ID") @PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            if (user.getRole() != UserRole.PROPERTY_MANAGER && user.getRole() != UserRole.BUILDING_OWNER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Only property managers can expire broadcast messages"));
            }

            BroadcastMessageResponse response = broadcastMessageService.expireBroadcast(id, user);
            return ResponseEntity.ok(new ApiResponse<>(true, response, "Broadcast message expired successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to expire broadcast message: " + e.getMessage()));
        }
    }

    @Operation(summary = "Delete a broadcast",
               description = "Permanently delete a broadcast message and all its recipient records.")
    @DeleteMapping("/messages/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBroadcast(
            @Parameter(description = "Broadcast message ID") @PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            if (user.getRole() != UserRole.PROPERTY_MANAGER && user.getRole() != UserRole.BUILDING_OWNER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Only property managers can delete broadcast messages"));
            }

            broadcastMessageService.deleteBroadcast(id, user);
            return ResponseEntity.ok(new ApiResponse<>(true, null, "Broadcast message deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to delete broadcast message: " + e.getMessage()));
        }
    }

    // ==================== TENANT ENDPOINTS ====================

    @Operation(summary = "Get active messages for tenant",
               description = "Retrieve active (non-expired, non-dismissed) broadcast messages for the carousel display. Requires TENANT role.")
    @GetMapping("/tenant/messages")
    public ResponseEntity<ApiResponse<List<TenantBroadcastMessageResponse>>> getActiveMessagesForTenant() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            if (user.getRole() != UserRole.TENANT) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Only tenants can access this endpoint"));
            }

            List<TenantBroadcastMessageResponse> responses = broadcastMessageService.getActiveMessagesForTenant(user);
            return ResponseEntity.ok(new ApiResponse<>(true, responses, "Active messages retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve active messages: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get all messages for tenant",
               description = "Retrieve all broadcast messages (including expired and dismissed) for the history view.")
    @GetMapping("/tenant/messages/all")
    public ResponseEntity<ApiResponse<List<TenantBroadcastMessageResponse>>> getAllMessagesForTenant() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            if (user.getRole() != UserRole.TENANT) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Only tenants can access this endpoint"));
            }

            List<TenantBroadcastMessageResponse> responses = broadcastMessageService.getAllMessagesForTenant(user);
            return ResponseEntity.ok(new ApiResponse<>(true, responses, "All messages retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve all messages: " + e.getMessage()));
        }
    }

    @Operation(summary = "Mark message as read",
               description = "Mark a broadcast message as read by the tenant.")
    @PutMapping("/tenant/messages/{id}/read")
    public ResponseEntity<ApiResponse<TenantBroadcastMessageResponse>> markAsRead(
            @Parameter(description = "Recipient record ID") @PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            if (user.getRole() != UserRole.TENANT) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Only tenants can access this endpoint"));
            }

            TenantBroadcastMessageResponse response = broadcastMessageService.markAsRead(id, user);
            return ResponseEntity.ok(new ApiResponse<>(true, response, "Message marked as read"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to mark message as read: " + e.getMessage()));
        }
    }

    @Operation(summary = "Dismiss message",
               description = "Dismiss a broadcast message from the carousel. Message will still appear in history.")
    @PutMapping("/tenant/messages/{id}/dismiss")
    public ResponseEntity<ApiResponse<TenantBroadcastMessageResponse>> dismissMessage(
            @Parameter(description = "Recipient record ID") @PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            if (user.getRole() != UserRole.TENANT) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Only tenants can access this endpoint"));
            }

            TenantBroadcastMessageResponse response = broadcastMessageService.dismissMessage(id, user);
            return ResponseEntity.ok(new ApiResponse<>(true, response, "Message dismissed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to dismiss message: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get unread message count",
               description = "Get the count of unread active messages for badge display.")
    @GetMapping("/tenant/messages/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            if (user.getRole() != UserRole.TENANT) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Only tenants can access this endpoint"));
            }

            Long count = broadcastMessageService.getUnreadCountForTenant(user);
            return ResponseEntity.ok(new ApiResponse<>(true, count, "Unread count retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve unread count: " + e.getMessage()));
        }
    }
}
