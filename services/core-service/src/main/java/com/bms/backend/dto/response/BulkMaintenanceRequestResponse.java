package com.bms.backend.dto.response;

import java.util.List;
import java.util.UUID;

public class BulkMaintenanceRequestResponse {

    private List<UUID> createdRequestIds;
    private int totalCreated;
    private int totalFailed;
    private List<String> failureMessages;

    // Default constructor
    public BulkMaintenanceRequestResponse() {}

    // Constructor
    public BulkMaintenanceRequestResponse(List<UUID> createdRequestIds, int totalCreated, int totalFailed, List<String> failureMessages) {
        this.createdRequestIds = createdRequestIds;
        this.totalCreated = totalCreated;
        this.totalFailed = totalFailed;
        this.failureMessages = failureMessages;
    }

    // Getters and Setters
    public List<UUID> getCreatedRequestIds() {
        return createdRequestIds;
    }

    public void setCreatedRequestIds(List<UUID> createdRequestIds) {
        this.createdRequestIds = createdRequestIds;
    }

    public int getTotalCreated() {
        return totalCreated;
    }

    public void setTotalCreated(int totalCreated) {
        this.totalCreated = totalCreated;
    }

    public int getTotalFailed() {
        return totalFailed;
    }

    public void setTotalFailed(int totalFailed) {
        this.totalFailed = totalFailed;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }

    public void setFailureMessages(List<String> failureMessages) {
        this.failureMessages = failureMessages;
    }
}