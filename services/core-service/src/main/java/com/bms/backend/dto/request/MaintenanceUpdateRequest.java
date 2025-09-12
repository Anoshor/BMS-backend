package com.bms.backend.dto.request;

import com.bms.backend.entity.MaintenanceUpdate;
import jakarta.validation.constraints.NotBlank;

public class MaintenanceUpdateRequest {
    
    @NotBlank(message = "Message is required")
    private String message;
    
    private MaintenanceUpdate.UpdateType updateType = MaintenanceUpdate.UpdateType.NOTE;

    // Default constructor
    public MaintenanceUpdateRequest() {}

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MaintenanceUpdate.UpdateType getUpdateType() {
        return updateType;
    }

    public void setUpdateType(MaintenanceUpdate.UpdateType updateType) {
        this.updateType = updateType;
    }
}