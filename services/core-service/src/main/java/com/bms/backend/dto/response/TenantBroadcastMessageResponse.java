package com.bms.backend.dto.response;

import com.bms.backend.entity.BroadcastMessage;
import com.bms.backend.entity.BroadcastMessageRecipient;

import java.time.Instant;
import java.util.UUID;

public class TenantBroadcastMessageResponse {

    private UUID id;
    private UUID messageId;
    private String title;
    private String content;
    private String priority;
    private String actionUrl;
    private Instant expiresAt;
    private Instant createdAt;

    // Manager info
    private UUID managerId;
    private String managerName;
    private String managerPhoto;

    // Recipient status
    private Boolean isRead;
    private Instant readAt;
    private Boolean isDismissed;
    private Instant dismissedAt;

    // Constructors
    public TenantBroadcastMessageResponse() {}

    public TenantBroadcastMessageResponse(BroadcastMessageRecipient recipient) {
        BroadcastMessage message = recipient.getBroadcastMessage();

        this.id = recipient.getId();
        this.messageId = message.getId();
        this.title = message.getTitle();
        this.content = message.getContent();
        this.priority = message.getPriority() != null ? message.getPriority().toString() : null;
        this.actionUrl = message.getActionUrl();
        this.expiresAt = message.getExpiresAt();
        this.createdAt = message.getCreatedAt();

        if (message.getManager() != null) {
            this.managerId = message.getManager().getId();
            this.managerName = message.getManager().getFirstName() + " " + message.getManager().getLastName();
            this.managerPhoto = message.getManager().getProfileImageUrl();
        }

        this.isRead = recipient.getIsRead();
        this.readAt = recipient.getReadAt();
        this.isDismissed = recipient.getIsDismissed();
        this.dismissedAt = recipient.getDismissedAt();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
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

    public String getManagerPhoto() {
        return managerPhoto;
    }

    public void setManagerPhoto(String managerPhoto) {
        this.managerPhoto = managerPhoto;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }

    public Boolean getIsDismissed() {
        return isDismissed;
    }

    public void setIsDismissed(Boolean isDismissed) {
        this.isDismissed = isDismissed;
    }

    public Instant getDismissedAt() {
        return dismissedAt;
    }

    public void setDismissedAt(Instant dismissedAt) {
        this.dismissedAt = dismissedAt;
    }
}
