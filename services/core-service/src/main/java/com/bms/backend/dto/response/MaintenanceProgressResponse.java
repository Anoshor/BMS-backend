package com.bms.backend.dto.response;

import com.bms.backend.entity.MaintenanceUpdate;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.UUID;

public class MaintenanceProgressResponse {

    private UUID id;
    private UUID maintenanceRequestId;
    private String updateType;
    private String currentStatus;
    private String message;
    private String notes;
    private UUID updatedById;
    private String updatedByName;
    private String updatedByEmail;
    private String updatedByRole;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant timestamp;

    // Default constructor
    public MaintenanceProgressResponse() {}

    // Constructor from entity
    public MaintenanceProgressResponse(MaintenanceUpdate update) {
        this.id = update.getId();
        this.maintenanceRequestId = update.getMaintenanceRequest() != null ? update.getMaintenanceRequest().getId() : null;
        this.updateType = update.getUpdateType() != null ? update.getUpdateType().toString() : null;
        this.currentStatus = update.getCurrentStatus() != null ? update.getCurrentStatus().toString() : null;
        this.message = update.getMessage();
        this.notes = update.getNotes();
        this.timestamp = update.getCreatedAt();

        // Updated by info
        if (update.getUpdatedBy() != null) {
            this.updatedById = update.getUpdatedBy().getId();
            this.updatedByName = update.getUpdatedBy().getFirstName() + " " + update.getUpdatedBy().getLastName();
            this.updatedByEmail = update.getUpdatedBy().getEmail();
            this.updatedByRole = update.getUpdatedBy().getRole() != null ? update.getUpdatedBy().getRole().toString() : null;
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMaintenanceRequestId() {
        return maintenanceRequestId;
    }

    public void setMaintenanceRequestId(UUID maintenanceRequestId) {
        this.maintenanceRequestId = maintenanceRequestId;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public UUID getUpdatedById() {
        return updatedById;
    }

    public void setUpdatedById(UUID updatedById) {
        this.updatedById = updatedById;
    }

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }

    public String getUpdatedByEmail() {
        return updatedByEmail;
    }

    public void setUpdatedByEmail(String updatedByEmail) {
        this.updatedByEmail = updatedByEmail;
    }

    public String getUpdatedByRole() {
        return updatedByRole;
    }

    public void setUpdatedByRole(String updatedByRole) {
        this.updatedByRole = updatedByRole;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}