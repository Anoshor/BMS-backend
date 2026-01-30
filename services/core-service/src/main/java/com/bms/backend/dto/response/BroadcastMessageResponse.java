package com.bms.backend.dto.response;

import com.bms.backend.entity.BroadcastMessage;

import java.time.Instant;
import java.util.UUID;

public class BroadcastMessageResponse {

    private UUID id;
    private String title;
    private String content;
    private String priority;
    private String targetType;
    private UUID targetPropertyId;
    private String targetPropertyName;
    private String actionUrl;
    private Instant expiresAt;
    private Boolean isActive;
    private Boolean isExpired;
    private Instant createdAt;
    private Instant updatedAt;

    // Manager info
    private UUID managerId;
    private String managerName;

    // Delivery stats
    private Long totalRecipients;
    private Long readCount;
    private Long dismissedCount;

    // Constructors
    public BroadcastMessageResponse() {}

    public BroadcastMessageResponse(BroadcastMessage message) {
        this.id = message.getId();
        this.title = message.getTitle();
        this.content = message.getContent();
        this.priority = message.getPriority() != null ? message.getPriority().toString() : null;
        this.targetType = message.getTargetType() != null ? message.getTargetType().toString() : null;
        this.targetPropertyId = message.getTargetPropertyId();
        this.actionUrl = message.getActionUrl();
        this.expiresAt = message.getExpiresAt();
        this.isActive = message.getIsActive();
        this.isExpired = message.isExpired();
        this.createdAt = message.getCreatedAt();
        this.updatedAt = message.getUpdatedAt();

        if (message.getManager() != null) {
            this.managerId = message.getManager().getId();
            this.managerName = message.getManager().getFirstName() + " " + message.getManager().getLastName();
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public UUID getTargetPropertyId() {
        return targetPropertyId;
    }

    public void setTargetPropertyId(UUID targetPropertyId) {
        this.targetPropertyId = targetPropertyId;
    }

    public String getTargetPropertyName() {
        return targetPropertyName;
    }

    public void setTargetPropertyName(String targetPropertyName) {
        this.targetPropertyName = targetPropertyName;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsExpired() {
        return isExpired;
    }

    public void setIsExpired(Boolean isExpired) {
        this.isExpired = isExpired;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UUID getManagerId() {
        return managerId;
    }

    public void setManagerId(UUID managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public Long getTotalRecipients() {
        return totalRecipients;
    }

    public void setTotalRecipients(Long totalRecipients) {
        this.totalRecipients = totalRecipients;
    }

    public Long getReadCount() {
        return readCount;
    }

    public void setReadCount(Long readCount) {
        this.readCount = readCount;
    }

    public Long getDismissedCount() {
        return dismissedCount;
    }

    public void setDismissedCount(Long dismissedCount) {
        this.dismissedCount = dismissedCount;
    }
}
