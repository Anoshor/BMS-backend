package com.bms.backend.dto.request;

import com.bms.backend.enums.BroadcastPriority;
import com.bms.backend.enums.BroadcastTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CreateBroadcastRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Content cannot exceed 2000 characters")
    private String content;

    @NotNull(message = "Priority is required")
    private BroadcastPriority priority;

    @NotNull(message = "Target type is required")
    private BroadcastTargetType targetType;

    // For SPECIFIC_PROPERTY targeting
    private UUID targetPropertyId;

    // For SINGLE_TENANT or MULTIPLE_TENANTS targeting
    private List<UUID> targetTenantIds;

    @Size(max = 500, message = "Action URL cannot exceed 500 characters")
    private String actionUrl;

    @NotNull(message = "Expiration date is required")
    private Instant expiresAt;

    // Default constructor
    public CreateBroadcastRequest() {}

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public BroadcastPriority getPriority() {
        return priority;
    }

    public void setPriority(BroadcastPriority priority) {
        this.priority = priority;
    }

    public BroadcastTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(BroadcastTargetType targetType) {
        this.targetType = targetType;
    }

    public UUID getTargetPropertyId() {
        return targetPropertyId;
    }

    public void setTargetPropertyId(UUID targetPropertyId) {
        this.targetPropertyId = targetPropertyId;
    }

    public List<UUID> getTargetTenantIds() {
        return targetTenantIds;
    }

    public void setTargetTenantIds(List<UUID> targetTenantIds) {
        this.targetTenantIds = targetTenantIds;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
