package com.bms.backend.dto.response;

import com.bms.backend.entity.MaintenanceRequest;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class MaintenanceDetailsResponse {

    private UUID id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private UUID apartmentId;
    private String apartmentUnitNumber;
    private UUID serviceCategoryId;
    private String serviceCategoryName;
    private UUID requesterId;
    private String requesterName;
    private String requesterEmail;
    private UUID tenantId;
    private String tenantName;
    private String tenantEmail;
    private UUID assignedToId;
    private String assignedToName;
    private String assignedToEmail;

    // Landlord (Property Manager) information
    private UUID landlordId;
    private String landlordName;
    private String landlordEmail;
    private String landlordPhone;
    private String landlordProfileImageUrl;

    // Property information
    private String propertyAddress;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant submittedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant scheduledAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant resolvedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant updatedAt;

    private Boolean managerInitiated;

    private List<MaintenancePhotoResponse> photos;
    private List<MaintenanceProgressResponse> progressHistory;

    // Default constructor
    public MaintenanceDetailsResponse() {}

    // Constructor from entity
    public MaintenanceDetailsResponse(MaintenanceRequest request) {
        this.id = request.getId();
        this.title = request.getTitle();
        this.description = request.getDescription();
        this.priority = request.getPriority() != null ? request.getPriority().toString() : null;
        this.status = request.getStatus() != null ? request.getStatus().toString() : null;

        // Apartment info
        if (request.getApartment() != null) {
            this.apartmentId = request.getApartment().getId();
            this.apartmentUnitNumber = request.getApartment().getUnitNumber();
        }

        // Service category info
        if (request.getServiceCategory() != null) {
            this.serviceCategoryId = request.getServiceCategory().getId();
            this.serviceCategoryName = request.getServiceCategory().getName();
        }

        // Requester info
        if (request.getRequester() != null) {
            this.requesterId = request.getRequester().getId();
            this.requesterName = request.getRequester().getFirstName() + " " + request.getRequester().getLastName();
            this.requesterEmail = request.getRequester().getEmail();
        }

        // Tenant info
        if (request.getTenant() != null) {
            this.tenantId = request.getTenant().getId();
            this.tenantName = request.getTenant().getFirstName() + " " + request.getTenant().getLastName();
            this.tenantEmail = request.getTenant().getEmail();
        }

        // Assigned to info
        if (request.getAssignedTo() != null) {
            this.assignedToId = request.getAssignedTo().getId();
            this.assignedToName = request.getAssignedTo().getFirstName() + " " + request.getAssignedTo().getLastName();
            this.assignedToEmail = request.getAssignedTo().getEmail();
        }

        // Landlord (Property Manager) info
        if (request.getApartment() != null && request.getApartment().getProperty() != null) {
            var property = request.getApartment().getProperty();

            // Set property address
            this.propertyAddress = property.getAddress();

            // Set manager info
            if (property.getManager() != null) {
                var manager = property.getManager();
                this.landlordId = manager.getId();
                this.landlordName = manager.getFirstName() + " " + manager.getLastName();
                this.landlordEmail = manager.getEmail();
                this.landlordPhone = manager.getPhone();
                this.landlordProfileImageUrl = manager.getProfileImageUrl();
            }
        }

        this.submittedAt = request.getSubmittedAt();
        this.scheduledAt = request.getScheduledAt();
        this.resolvedAt = request.getResolvedAt();
        this.createdAt = request.getCreatedAt();
        this.updatedAt = request.getUpdatedAt();
        this.managerInitiated = request.getManagerInitiated();
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

    public UUID getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(UUID apartmentId) {
        this.apartmentId = apartmentId;
    }

    public String getApartmentUnitNumber() {
        return apartmentUnitNumber;
    }

    public void setApartmentUnitNumber(String apartmentUnitNumber) {
        this.apartmentUnitNumber = apartmentUnitNumber;
    }

    public UUID getServiceCategoryId() {
        return serviceCategoryId;
    }

    public void setServiceCategoryId(UUID serviceCategoryId) {
        this.serviceCategoryId = serviceCategoryId;
    }

    public String getServiceCategoryName() {
        return serviceCategoryName;
    }

    public void setServiceCategoryName(String serviceCategoryName) {
        this.serviceCategoryName = serviceCategoryName;
    }

    public UUID getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(UUID requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantEmail() {
        return tenantEmail;
    }

    public void setTenantEmail(String tenantEmail) {
        this.tenantEmail = tenantEmail;
    }

    public UUID getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(UUID assignedToId) {
        this.assignedToId = assignedToId;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public String getAssignedToEmail() {
        return assignedToEmail;
    }

    public void setAssignedToEmail(String assignedToEmail) {
        this.assignedToEmail = assignedToEmail;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
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

    public Boolean getManagerInitiated() {
        return managerInitiated;
    }

    public void setManagerInitiated(Boolean managerInitiated) {
        this.managerInitiated = managerInitiated;
    }

    public List<MaintenancePhotoResponse> getPhotos() {
        return photos;
    }

    public void setPhotos(List<MaintenancePhotoResponse> photos) {
        this.photos = photos;
    }

    public List<MaintenanceProgressResponse> getProgressHistory() {
        return progressHistory;
    }

    public void setProgressHistory(List<MaintenanceProgressResponse> progressHistory) {
        this.progressHistory = progressHistory;
    }

    public UUID getLandlordId() {
        return landlordId;
    }

    public void setLandlordId(UUID landlordId) {
        this.landlordId = landlordId;
    }

    public String getLandlordName() {
        return landlordName;
    }

    public void setLandlordName(String landlordName) {
        this.landlordName = landlordName;
    }

    public String getLandlordEmail() {
        return landlordEmail;
    }

    public void setLandlordEmail(String landlordEmail) {
        this.landlordEmail = landlordEmail;
    }

    public String getLandlordPhone() {
        return landlordPhone;
    }

    public void setLandlordPhone(String landlordPhone) {
        this.landlordPhone = landlordPhone;
    }

    public String getLandlordProfileImageUrl() {
        return landlordProfileImageUrl;
    }

    public void setLandlordProfileImageUrl(String landlordProfileImageUrl) {
        this.landlordProfileImageUrl = landlordProfileImageUrl;
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }
}