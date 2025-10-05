package com.bms.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TenantDetailsDto {

    // Tenant Basic Information
    private UUID tenantId;
    private String tenantName;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String photo;
    private String accountStatus;
    private Instant createdAt;

    // Properties and Units
    private List<TenantPropertyInfo> properties;

    // Lease Information Summary
    private Integer totalActiveLeases;
    private Integer totalProperties;
    private Double totalMonthlyRent;

    // Static inner class for property information
    public static class TenantPropertyInfo {
        private UUID connectionId;
        private UUID propertyId;
        private String propertyName;
        private String propertyType;
        private String propertyAddress;
        private UUID apartmentId;
        private String unitNumber;
        private String unitType;
        private Integer floor;
        private Integer bedrooms;
        private java.math.BigDecimal bathrooms;
        private Integer squareFootage;
        private String occupancyStatus;
        private String furnished;
        private String balcony;
        private String images;

        // Lease Details
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate leaseStartDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate leaseEndDate;

        private String leaseDuration;
        private Double monthlyRent;
        private Double securityDeposit;
        private java.math.BigDecimal maintenanceCharges;
        private String paymentFrequency;
        private String notes;
        private Boolean isActive;

        // Manager Information
        private UUID managerId;
        private String managerName;
        private String managerEmail;
        private String managerPhone;

        // Additional Info
        private Boolean hasMaintenanceRequests;
        private Boolean hasDocuments;
        private String utilityMeterNumbers;

        // Default constructor
        public TenantPropertyInfo() {}

        // Getters and Setters
        public UUID getConnectionId() {
            return connectionId;
        }

        public void setConnectionId(UUID connectionId) {
            this.connectionId = connectionId;
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

        public UUID getApartmentId() {
            return apartmentId;
        }

        public void setApartmentId(UUID apartmentId) {
            this.apartmentId = apartmentId;
        }

        public String getUnitNumber() {
            return unitNumber;
        }

        public void setUnitNumber(String unitNumber) {
            this.unitNumber = unitNumber;
        }

        public String getUnitType() {
            return unitType;
        }

        public void setUnitType(String unitType) {
            this.unitType = unitType;
        }

        public Integer getFloor() {
            return floor;
        }

        public void setFloor(Integer floor) {
            this.floor = floor;
        }

        public Integer getBedrooms() {
            return bedrooms;
        }

        public void setBedrooms(Integer bedrooms) {
            this.bedrooms = bedrooms;
        }

        public java.math.BigDecimal getBathrooms() {
            return bathrooms;
        }

        public void setBathrooms(java.math.BigDecimal bathrooms) {
            this.bathrooms = bathrooms;
        }

        public Integer getSquareFootage() {
            return squareFootage;
        }

        public void setSquareFootage(Integer squareFootage) {
            this.squareFootage = squareFootage;
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

        public String getBalcony() {
            return balcony;
        }

        public void setBalcony(String balcony) {
            this.balcony = balcony;
        }

        public String getImages() {
            return images;
        }

        public void setImages(String images) {
            this.images = images;
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

        public Double getMonthlyRent() {
            return monthlyRent;
        }

        public void setMonthlyRent(Double monthlyRent) {
            this.monthlyRent = monthlyRent;
        }

        public Double getSecurityDeposit() {
            return securityDeposit;
        }

        public void setSecurityDeposit(Double securityDeposit) {
            this.securityDeposit = securityDeposit;
        }

        public java.math.BigDecimal getMaintenanceCharges() {
            return maintenanceCharges;
        }

        public void setMaintenanceCharges(java.math.BigDecimal maintenanceCharges) {
            this.maintenanceCharges = maintenanceCharges;
        }

        public String getPaymentFrequency() {
            return paymentFrequency;
        }

        public void setPaymentFrequency(String paymentFrequency) {
            this.paymentFrequency = paymentFrequency;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public Boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }

        public UUID getManagerId() {
            return managerId;
        }

        public void setManagerId(UUID managerId) {
            this.managerId = managerId;
        }

        public String getManagerName() {
            return managerName;
        }

        public void setManagerName(String managerName) {
            this.managerName = managerName;
        }

        public String getManagerEmail() {
            return managerEmail;
        }

        public void setManagerEmail(String managerEmail) {
            this.managerEmail = managerEmail;
        }

        public String getManagerPhone() {
            return managerPhone;
        }

        public void setManagerPhone(String managerPhone) {
            this.managerPhone = managerPhone;
        }

        public Boolean getHasMaintenanceRequests() {
            return hasMaintenanceRequests;
        }

        public void setHasMaintenanceRequests(Boolean hasMaintenanceRequests) {
            this.hasMaintenanceRequests = hasMaintenanceRequests;
        }

        public Boolean getHasDocuments() {
            return hasDocuments;
        }

        public void setHasDocuments(Boolean hasDocuments) {
            this.hasDocuments = hasDocuments;
        }

        public String getUtilityMeterNumbers() {
            return utilityMeterNumbers;
        }

        public void setUtilityMeterNumbers(String utilityMeterNumbers) {
            this.utilityMeterNumbers = utilityMeterNumbers;
        }
    }

    // Default constructor
    public TenantDetailsDto() {}

    // Getters and Setters
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<TenantPropertyInfo> getProperties() {
        return properties;
    }

    public void setProperties(List<TenantPropertyInfo> properties) {
        this.properties = properties;
    }

    public Integer getTotalActiveLeases() {
        return totalActiveLeases;
    }

    public void setTotalActiveLeases(Integer totalActiveLeases) {
        this.totalActiveLeases = totalActiveLeases;
    }

    public Integer getTotalProperties() {
        return totalProperties;
    }

    public void setTotalProperties(Integer totalProperties) {
        this.totalProperties = totalProperties;
    }

    public Double getTotalMonthlyRent() {
        return totalMonthlyRent;
    }

    public void setTotalMonthlyRent(Double totalMonthlyRent) {
        this.totalMonthlyRent = totalMonthlyRent;
    }
}