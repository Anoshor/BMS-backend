package com.bms.backend.dto.response;

import com.bms.backend.entity.MaintenanceRequest;
import java.time.Instant;
import java.util.UUID;

public class MaintenanceRequestResponse {
    
    private UUID id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private String apartmentUnitNumber;
    private String serviceCategoryName;
    private String requesterEmail;
    private String tenantEmail;
    private String assignedToEmail;
    private Instant scheduledAt;
    private Instant submittedAt;
    private Instant resolvedAt;
    private Instant createdAt;
    private Instant updatedAt;

    // Constructors
    public MaintenanceRequestResponse() {}

    public MaintenanceRequestResponse(MaintenanceRequest request) {
        this.id = request.getId();
        this.title = request.getTitle();
        this.description = request.getDescription();
        this.priority = request.getPriority() != null ? request.getPriority().toString() : null;
        this.status = request.getStatus() != null ? request.getStatus().toString() : null;
        
        // Apartment info
        if (request.getApartment() != null) {
            this.apartmentUnitNumber = request.getApartment().getUnitNumber();
        }
        
        // Service category info
        if (request.getServiceCategory() != null) {
            this.serviceCategoryName = request.getServiceCategory().getName();
        }
        
        // User info
        if (request.getRequester() != null) {
            this.requesterEmail = request.getRequester().getEmail();
        }
        if (request.getTenant() != null) {
            this.tenantEmail = request.getTenant().getEmail();
        }
        if (request.getAssignedTo() != null) {
            this.assignedToEmail = request.getAssignedTo().getEmail();
        }
        
        this.scheduledAt = request.getScheduledAt();
        this.submittedAt = request.getSubmittedAt();
        this.resolvedAt = request.getResolvedAt();
        this.createdAt = request.getCreatedAt();
        this.updatedAt = request.getUpdatedAt();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApartmentUnitNumber() {
        return apartmentUnitNumber;
    }

    public void setApartmentUnitNumber(String apartmentUnitNumber) {
        this.apartmentUnitNumber = apartmentUnitNumber;
    }

    public String getServiceCategoryName() {
        return serviceCategoryName;
    }

    public void setServiceCategoryName(String serviceCategoryName) {
        this.serviceCategoryName = serviceCategoryName;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }

    public String getTenantEmail() {
        return tenantEmail;
    }

    public void setTenantEmail(String tenantEmail) {
        this.tenantEmail = tenantEmail;
    }

    public String getAssignedToEmail() {
        return assignedToEmail;
    }

    public void setAssignedToEmail(String assignedToEmail) {
        this.assignedToEmail = assignedToEmail;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}