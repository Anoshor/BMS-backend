package com.bms.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "broadcast_message_recipients",
       uniqueConstraints = @UniqueConstraint(columnNames = {"broadcast_message_id", "tenant_id"}))
public class BroadcastMessageRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "broadcast_message_id", nullable = false)
    @JsonIgnore
    private BroadcastMessage broadcastMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @JsonIgnore
    private User tenant;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "is_dismissed")
    private Boolean isDismissed = false;

    @Column(name = "dismissed_at")
    private Instant dismissedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    // Constructors
    public BroadcastMessageRecipient() {}

    public BroadcastMessageRecipient(BroadcastMessage broadcastMessage, User tenant) {
        this.broadcastMessage = broadcastMessage;
        this.tenant = tenant;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BroadcastMessage getBroadcastMessage() {
        return broadcastMessage;
    }

    public void setBroadcastMessage(BroadcastMessage broadcastMessage) {
        this.broadcastMessage = broadcastMessage;
    }

    public User getTenant() {
        return tenant;
    }

    public void setTenant(User tenant) {
        this.tenant = tenant;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods
    public void markAsRead() {
        if (!Boolean.TRUE.equals(this.isRead)) {
            this.isRead = true;
            this.readAt = Instant.now();
        }
    }

    public void dismiss() {
        if (!Boolean.TRUE.equals(this.isDismissed)) {
            this.isDismissed = true;
            this.dismissedAt = Instant.now();
        }
    }
}
