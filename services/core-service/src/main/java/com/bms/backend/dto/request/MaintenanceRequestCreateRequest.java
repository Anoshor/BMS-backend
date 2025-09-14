package com.bms.backend.dto.request;

import com.bms.backend.entity.MaintenanceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class MaintenanceRequestCreateRequest {
    
    @NotNull(message = "Apartment ID is required")
    private UUID apartmentId;
    
    @NotNull(message = "Service category ID is required")
    private UUID serviceCategoryId;
    
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private MaintenanceRequest.Priority priority = MaintenanceRequest.Priority.MEDIUM;
    
    private List<String> photos; // Base64 encoded images or URLs

    // Default constructor
    public MaintenanceRequestCreateRequest() {}

    // Getters and setters
    public UUID getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(UUID apartmentId) {
        this.apartmentId = apartmentId;
    }

    public UUID getServiceCategoryId() {
        return serviceCategoryId;
    }

    public void setServiceCategoryId(UUID serviceCategoryId) {
        this.serviceCategoryId = serviceCategoryId;
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

    public MaintenanceRequest.Priority getPriority() {
        return priority;
    }

    public void setPriority(MaintenanceRequest.Priority priority) {
        this.priority = priority;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }
}