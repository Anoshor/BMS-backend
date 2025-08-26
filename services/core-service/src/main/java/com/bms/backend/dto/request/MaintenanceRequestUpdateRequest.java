package com.bms.backend.dto.request;

import com.bms.backend.entity.MaintenanceRequest;

import java.time.Instant;
import java.util.UUID;

public class MaintenanceRequestUpdateRequest {
    
    private MaintenanceRequest.Status status;
    
    private UUID assignedTo;
    
    private Instant scheduledAt;
    
    private MaintenanceRequest.Priority priority;

    // Default constructor
    public MaintenanceRequestUpdateRequest() {}

    // Getters and setters
    public MaintenanceRequest.Status getStatus() {
        return status;
    }

    public void setStatus(MaintenanceRequest.Status status) {
        this.status = status;
    }

    public UUID getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public MaintenanceRequest.Priority getPriority() {
        return priority;
    }

    public void setPriority(MaintenanceRequest.Priority priority) {
        this.priority = priority;
    }
}