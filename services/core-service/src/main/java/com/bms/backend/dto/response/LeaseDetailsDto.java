package com.bms.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class LeaseDetailsDto {
    
    // Connection/Lease Information
    private UUID connectionId;
    private String leaseId; // Formatted lease ID like "LEASE-2025-0031"
    
    // Property Information
    private UUID propertyId;
    private String propertyName;
    private String propertyType;
    private String propertyAddress;
    private Double rentAmount; // Monthly rent from connection
    private List<PropertyImageDto> propertyImages;
    
    // Property Features (from apartment)
    private Integer bedrooms;
    private BigDecimal bathrooms;
    private Integer squareFootage; // Size in sq ft
    
    // Tenant Information
    private UUID tenantId;
    private String tenantName;
    private String tenantEmail;
    private String tenantPhone;
    
    // Lease Details
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate leaseStartDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd") 
    private LocalDate leaseEndDate;
    
    private String leaseDuration; // Calculated duration like "1 Year"
    
    // Deposit & Charges
    private Double securityDeposit;
    private BigDecimal maintenanceCharges;
    private String utilityMeterNumbers; // JSON string with meter numbers
    
    // Unit Details
    private UUID apartmentId;
    private String unitId; // Unit number like "A-101"
    private Integer floor;
    private String occupancyStatus; // occupied, vacant, maintenance
    private String furnished; // furnished, semi-furnished, unfurnished
    
    // Navigation Actions (boolean flags for UI)
    private boolean hasMaintenanceRequests;
    private boolean hasDocuments;
    
    // Nested DTO for property images
    public static class PropertyImageDto {
        private UUID imageId;
        private String imageUrl;
        private String imageData; // base64 if no URL
        private String imageName;
        private String description;
        private boolean isPrimary;
        private Integer displayOrder;
        
        // Constructors
        public PropertyImageDto() {}
        
        public PropertyImageDto(UUID imageId, String imageUrl, String imageData, 
                               String imageName, String description, boolean isPrimary, Integer displayOrder) {
            this.imageId = imageId;
            this.imageUrl = imageUrl;
            this.imageData = imageData;
            this.imageName = imageName;
            this.description = description;
            this.isPrimary = isPrimary;
            this.displayOrder = displayOrder;
        }
        
        // Getters and Setters
        public UUID getImageId() { return imageId; }
        public void setImageId(UUID imageId) { this.imageId = imageId; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public String getImageData() { return imageData; }
        public void setImageData(String imageData) { this.imageData = imageData; }
        
        public String getImageName() { return imageName; }
        public void setImageName(String imageName) { this.imageName = imageName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public boolean isPrimary() { return isPrimary; }
        public void setPrimary(boolean primary) { isPrimary = primary; }
        
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    }
    
    // Default constructor
    public LeaseDetailsDto() {}
    
    // Getters and Setters
    public UUID getConnectionId() {
        return connectionId;
    }
    
    public void setConnectionId(UUID connectionId) {
        this.connectionId = connectionId;
    }
    
    public String getLeaseId() {
        return leaseId;
    }
    
    public void setLeaseId(String leaseId) {
        this.leaseId = leaseId;
    }
    
    public UUID getPropertyId() {
        return propertyId;
    }
    
    public void setPropertyId(UUID propertyId) {
        this.propertyId = propertyId;
    }
    
    public String getPropertyName() {
        return propertyName;
    }
    
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
    
    public String getPropertyType() {
        return propertyType;
    }
    
    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }
    
    public String getPropertyAddress() {
        return propertyAddress;
    }
    
    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }
    
    public Double getRentAmount() {
        return rentAmount;
    }
    
    public void setRentAmount(Double rentAmount) {
        this.rentAmount = rentAmount;
    }
    
    public List<PropertyImageDto> getPropertyImages() {
        return propertyImages;
    }
    
    public void setPropertyImages(List<PropertyImageDto> propertyImages) {
        this.propertyImages = propertyImages;
    }
    
    public Integer getBedrooms() {
        return bedrooms;
    }
    
    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }
    
    public BigDecimal getBathrooms() {
        return bathrooms;
    }
    
    public void setBathrooms(BigDecimal bathrooms) {
        this.bathrooms = bathrooms;
    }
    
    public Integer getSquareFootage() {
        return squareFootage;
    }
    
    public void setSquareFootage(Integer squareFootage) {
        this.squareFootage = squareFootage;
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
    
    public String getTenantPhone() {
        return tenantPhone;
    }
    
    public void setTenantPhone(String tenantPhone) {
        this.tenantPhone = tenantPhone;
    }
    
    public LocalDate getLeaseStartDate() {
        return leaseStartDate;
    }
    
    public void setLeaseStartDate(LocalDate leaseStartDate) {
        this.leaseStartDate = leaseStartDate;
    }
    
    public LocalDate getLeaseEndDate() {
        return leaseEndDate;
    }
    
    public void setLeaseEndDate(LocalDate leaseEndDate) {
        this.leaseEndDate = leaseEndDate;
    }
    
    public String getLeaseDuration() {
        return leaseDuration;
    }
    
    public void setLeaseDuration(String leaseDuration) {
        this.leaseDuration = leaseDuration;
    }
    
    public Double getSecurityDeposit() {
        return securityDeposit;
    }
    
    public void setSecurityDeposit(Double securityDeposit) {
        this.securityDeposit = securityDeposit;
    }
    
    public BigDecimal getMaintenanceCharges() {
        return maintenanceCharges;
    }
    
    public void setMaintenanceCharges(BigDecimal maintenanceCharges) {
        this.maintenanceCharges = maintenanceCharges;
    }
    
    public String getUtilityMeterNumbers() {
        return utilityMeterNumbers;
    }
    
    public void setUtilityMeterNumbers(String utilityMeterNumbers) {
        this.utilityMeterNumbers = utilityMeterNumbers;
    }
    
    public UUID getApartmentId() {
        return apartmentId;
    }
    
    public void setApartmentId(UUID apartmentId) {
        this.apartmentId = apartmentId;
    }
    
    public String getUnitId() {
        return unitId;
    }
    
    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }
    
    public Integer getFloor() {
        return floor;
    }
    
    public void setFloor(Integer floor) {
        this.floor = floor;
    }
    
    public String getOccupancyStatus() {
        return occupancyStatus;
    }
    
    public void setOccupancyStatus(String occupancyStatus) {
        this.occupancyStatus = occupancyStatus;
    }
    
    public String getFurnished() {
        return furnished;
    }
    
    public void setFurnished(String furnished) {
        this.furnished = furnished;
    }
    
    public boolean isHasMaintenanceRequests() {
        return hasMaintenanceRequests;
    }
    
    public void setHasMaintenanceRequests(boolean hasMaintenanceRequests) {
        this.hasMaintenanceRequests = hasMaintenanceRequests;
    }
    
    public boolean isHasDocuments() {
        return hasDocuments;
    }
    
    public void setHasDocuments(boolean hasDocuments) {
        this.hasDocuments = hasDocuments;
    }
}