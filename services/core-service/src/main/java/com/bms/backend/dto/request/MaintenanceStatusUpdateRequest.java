package com.bms.backend.dto.request;

import com.bms.backend.entity.MaintenanceRequest;
import jakarta.validation.constraints.NotNull;

public class MaintenanceStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private MaintenanceRequest.Status status;

    private String notes;

    // Default constructor
    public MaintenanceStatusUpdateRequest() {}

    // Constructor
    public MaintenanceStatusUpdateRequest(MaintenanceRequest.Status status, String notes) {
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public MaintenanceRequest.Status getStatus() {
        return status;
    }

    public void setStatus(MaintenanceRequest.Status status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}