package com.bms.backend.entity;

import com.bms.backend.enums.BroadcastPriority;
import com.bms.backend.enums.BroadcastTargetType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "broadcast_messages")
public class BroadcastMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    @JsonIgnore
    private User manager;

    @Column(name = "title", nullable = false)
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Content cannot exceed 2000 characters")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    @NotNull(message = "Priority is required")
    private BroadcastPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    @NotNull(message = "Target type is required")
    private BroadcastTargetType targetType;

    @Column(name = "target_property_id")
    private UUID targetPropertyId;

    @Column(name = "action_url", length = 500)
    @Size(max = 500, message = "Action URL cannot exceed 500 characters")
    private String actionUrl;

    @Column(name = "expires_at", nullable = false)
    @NotNull(message = "Expiration date is required")
    private Instant expiresAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "broadcastMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private List<BroadcastMessageRecipient> recipients = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Constructors
    public BroadcastMessage() {}

    public BroadcastMessage(User manager, String title, String content, BroadcastPriority priority,
                           BroadcastTargetType targetType, Instant expiresAt) {
        this.manager = manager;
        this.title = title;
        this.content = content;
        this.priority = priority;
        this.targetType = targetType;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
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

    public List<BroadcastMessageRecipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<BroadcastMessageRecipient> recipients) {
        this.recipients = recipients;
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

    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    public boolean isActiveAndNotExpired() {
        return Boolean.TRUE.equals(isActive) && !isExpired();
    }

    public void addRecipient(BroadcastMessageRecipient recipient) {
        recipients.add(recipient);
        recipient.setBroadcastMessage(this);
    }

    public void removeRecipient(BroadcastMessageRecipient recipient) {
        recipients.remove(recipient);
        recipient.setBroadcastMessage(null);
    }
}
