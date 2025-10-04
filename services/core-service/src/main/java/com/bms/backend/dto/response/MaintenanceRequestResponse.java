package com.bms.backend.dto.response;

import com.bms.backend.entity.MaintenanceRequest;
import com.bms.backend.entity.MaintenanceRequestPhoto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MaintenanceRequestResponse {
    
    private UUID id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private String apartmentUnitNumber;
    private String apartmentUnitType;
    private UUID apartmentId;
    private String serviceCategoryName;
    private UUID serviceCategoryId;
    private String requesterEmail;
    private UUID requesterId;
    private String requesterName;
    private String tenantEmail;
    private UUID tenantId;
    private String tenantName;
    private String tenantPhoto;
    private String assignedToEmail;
    private UUID assignedToId;
    private String assignedToName;
    private String landlordEmail;
    private UUID landlordId;
    private String landlordName;
    private String landlordPhone;
    private boolean managerInitiated;
    private Instant scheduledAt;
    private Instant submittedAt;
    private Instant resolvedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<MaintenancePhotoResponse> photos;

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
            this.apartmentId = request.getApartment().getId();
            this.apartmentUnitNumber = request.getApartment().getUnitNumber();
            this.apartmentUnitType = request.getApartment().getUnitType();

            // Get landlord info from apartment's property
            if (request.getApartment().getProperty() != null &&
                request.getApartment().getProperty().getManager() != null) {
                var landlord = request.getApartment().getProperty().getManager();
                this.landlordId = landlord.getId();
                this.landlordName = landlord.getFirstName() + " " + landlord.getLastName();
                this.landlordEmail = landlord.getEmail();
                this.landlordPhone = landlord.getPhone();
            }
        }

        // Service category info
        if (request.getServiceCategory() != null) {
            this.serviceCategoryId = request.getServiceCategory().getId();
            this.serviceCategoryName = request.getServiceCategory().getName();
        }

        // User info
        if (request.getRequester() != null) {
            this.requesterId = request.getRequester().getId();
            this.requesterName = request.getRequester().getFirstName() + " " + request.getRequester().getLastName();
            this.requesterEmail = request.getRequester().getEmail();
        }
        if (request.getTenant() != null) {
            this.tenantId = request.getTenant().getId();
            this.tenantName = request.getTenant().getFirstName() + " " + request.getTenant().getLastName();
            this.tenantEmail = request.getTenant().getEmail();
            this.tenantPhoto = request.getTenant().getProfileImageUrl();
        }
        if (request.getAssignedTo() != null) {
            this.assignedToId = request.getAssignedTo().getId();
            this.assignedToName = request.getAssignedTo().getFirstName() + " " + request.getAssignedTo().getLastName();
            this.assignedToEmail = request.getAssignedTo().getEmail();
        }

        // Check if request was initiated by manager
        this.managerInitiated = request.getRequester() != null &&
                               (request.getRequester().getRole().name().equals("PROPERTY_MANAGER") ||
                                request.getRequester().getRole().name().equals("BUILDING_OWNER"));
        
        this.scheduledAt = request.getScheduledAt();
        this.submittedAt = request.getSubmittedAt();
        this.resolvedAt = request.getResolvedAt();
        this.createdAt = request.getCreatedAt();
        this.updatedAt = request.getUpdatedAt();

        // Photos info
        if (request.getPhotos() != null) {
            this.photos = request.getPhotos().stream()
                    .map(MaintenancePhotoResponse::new)
                    .collect(Collectors.toList());
        }
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

    public String getApartmentUnitType() {
        return apartmentUnitType;
    }

    public void setApartmentUnitType(String apartmentUnitType) {
        this.apartmentUnitType = apartmentUnitType;
    }

    public UUID getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(UUID apartmentId) {
        this.apartmentId = apartmentId;
    }

    public String getServiceCategoryName() {
        return serviceCategoryName;
    }

    public void setServiceCategoryName(String serviceCategoryName) {
        this.serviceCategoryName = serviceCategoryName;
    }

    public UUID getServiceCategoryId() {
        return serviceCategoryId;
    }

    public void setServiceCategoryId(UUID serviceCategoryId) {
        this.serviceCategoryId = serviceCategoryId;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
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

    public String getTenantEmail() {
        return tenantEmail;
    }

    public void setTenantEmail(String tenantEmail) {
        this.tenantEmail = tenantEmail;
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

    public String getTenantPhoto() {
        return tenantPhoto;
    }

    public void setTenantPhoto(String tenantPhoto) {
        this.tenantPhoto = tenantPhoto;
    }

    public String getAssignedToEmail() {
        return assignedToEmail;
    }

    public void setAssignedToEmail(String assignedToEmail) {
        this.assignedToEmail = assignedToEmail;
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

    public String getLandlordEmail() {
        return landlordEmail;
    }

    public void setLandlordEmail(String landlordEmail) {
        this.landlordEmail = landlordEmail;
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

    public String getLandlordPhone() {
        return landlordPhone;
    }

    public void setLandlordPhone(String landlordPhone) {
        this.landlordPhone = landlordPhone;
    }

    public boolean isManagerInitiated() {
        return managerInitiated;
    }

    public void setManagerInitiated(boolean managerInitiated) {
        this.managerInitiated = managerInitiated;
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

    public List<MaintenancePhotoResponse> getPhotos() {
        return photos;
    }

    public void setPhotos(List<MaintenancePhotoResponse> photos) {
        this.photos = photos;
    }
}