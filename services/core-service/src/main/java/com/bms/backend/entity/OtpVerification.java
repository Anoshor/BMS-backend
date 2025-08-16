package com.bms.backend.entity;

import com.bms.backend.enums.OtpType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "otp_verifications",
       uniqueConstraints = @UniqueConstraint(columnNames = {"identifier", "otp_type"}))
public class OtpVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "identifier", nullable = false)
    private String identifier; // email or phone
    
    @Column(name = "otp_code", nullable = false)
    private String otpCode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "otp_type", nullable = false)
    private OtpType otpType;
    
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    
    @Column(name = "attempts")
    private Integer attempts = 0;
    
    @Column(name = "is_used")
    private Boolean isUsed = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    
    // Constructors
    public OtpVerification() {}
    
    public OtpVerification(String identifier, String otpCode, OtpType otpType, Instant expiresAt) {
        this.identifier = identifier;
        this.otpCode = otpCode;
        this.otpType = otpType;
        this.expiresAt = expiresAt;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public String getOtpCode() {
        return otpCode;
    }
    
    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
    
    public OtpType getOtpType() {
        return otpType;
    }
    
    public void setOtpType(OtpType otpType) {
        this.otpType = otpType;
    }
    
    public Instant getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Integer getAttempts() {
        return attempts;
    }
    
    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }
    
    public Boolean getIsUsed() {
        return isUsed;
    }
    
    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    // Helper methods
    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }
    
    public boolean isValid() {
        return !Boolean.TRUE.equals(isUsed) && !isExpired() && attempts < 3;
    }
    
    public void incrementAttempts() {
        this.attempts = (this.attempts == null ? 0 : this.attempts) + 1;
    }
    
    public void markAsUsed() {
        this.isUsed = true;
    }
    
    public boolean canAttempt() {
        return attempts < 3 && !Boolean.TRUE.equals(isUsed) && !isExpired();
    }
}