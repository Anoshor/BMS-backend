package com.bms.backend.dto.response;

import com.bms.backend.entity.MaintenanceRequestPhoto;

import java.util.UUID;

public class MaintenancePhotoResponse {

    private UUID id;
    private String photoData;
    private String fileName;
    private String contentType;
    private String description;

    // Default constructor
    public MaintenancePhotoResponse() {}

    // Constructor from entity
    public MaintenancePhotoResponse(MaintenanceRequestPhoto photo) {
        this.id = photo.getId();
        this.photoData = photo.getPhotoData();
        // MaintenanceRequestPhoto doesn't have fileName, contentType, description fields
        this.fileName = null;
        this.contentType = null;
        this.description = null;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPhotoData() {
        return photoData;
    }

    public void setPhotoData(String photoData) {
        this.photoData = photoData;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}