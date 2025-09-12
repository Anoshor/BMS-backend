package com.bms.backend.dto.request;

import com.bms.backend.entity.MaintenanceRequest;
import jakarta.validation.constraints.NotNull;

public class MaintenanceRequestUpdateRequest {
    
    @NotNull(message = "Status is required")
    private MaintenanceRequest.Status status;
    
    private String description;

    // Default constructor
    public MaintenanceRequestUpdateRequest() {}

    // Getters and setters
    public MaintenanceRequest.Status getStatus() {
        return status;
    }

    public void setStatus(MaintenanceRequest.Status status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}