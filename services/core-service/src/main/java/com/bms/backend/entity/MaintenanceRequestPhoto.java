package com.bms.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "maintenance_request_photos")
public class MaintenanceRequestPhoto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_request_id", nullable = false)
    @JsonIgnore
    private MaintenanceRequest maintenanceRequest;
    
    @Column(name = "photo_url", nullable = true, length = 500)
    private String photoUrl;
    
    @Column(name = "photo_data", columnDefinition = "TEXT")
    private String photoData;
    
    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private Instant uploadedAt;
    
    @Column(name = "created_at")
    private Instant createdAt;

    // Default constructor
    public MaintenanceRequestPhoto() {}

    // Constructor
    public MaintenanceRequestPhoto(MaintenanceRequest maintenanceRequest, String photoUrl) {
        this.maintenanceRequest = maintenanceRequest;
        this.photoUrl = photoUrl;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MaintenanceRequest getMaintenanceRequest() {
        return maintenanceRequest;
    }

    public void setMaintenanceRequest(MaintenanceRequest maintenanceRequest) {
        this.maintenanceRequest = maintenanceRequest;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getPhotoData() {
        return photoData;
    }

    public void setPhotoData(String photoData) {
        this.photoData = photoData;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}