package com.bms.backend.dto.response;

import com.bms.backend.entity.TenantPropertyConnection;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class LeaseListingDto {

    private UUID id;
    private String leaseId;
    private String propertyName;
    private String propertyAddress;
    private String propertyImage;
    private UUID propertyId;
    private UUID apartmentId;
    private String unitNumber;
    private String unitType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String leaseDuration;
    private Double monthlyRent;
    private Double securityDeposit;
    private String paymentFrequency;
    private Boolean isActive;
    private String leaseStatus;

    // Tenant Information
    private UUID tenantId;
    private String tenantName;
    private String tenantEmail;
    private String tenantPhone;
    private String tenantPhoto;

    // Manager Information
    private UUID managerId;
    private String managerName;
    private String managerEmail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant updatedAt;

    // Default constructor
    public LeaseListingDto() {}

    // Constructor from entity
    public LeaseListingDto(TenantPropertyConnection connection) {
        this.id = connection.getId();
        this.leaseId = generateLeaseId(connection.getId());
        this.propertyName = connection.getPropertyName();
        this.startDate = connection.getStartDate();
        this.endDate = connection.getEndDate();
        this.leaseDuration = calculateLeaseDuration(connection.getStartDate(), connection.getEndDate());
        this.monthlyRent = connection.getMonthlyRent();
        this.securityDeposit = connection.getSecurityDeposit();
        this.paymentFrequency = connection.getPaymentFrequency();
        this.isActive = connection.getIsActive();
        this.leaseStatus = determineLeaseStatus(connection);
        this.createdAt = connection.getCreatedAt();
        this.updatedAt = connection.getUpdatedAt();

        // Property and Apartment information from connection
        if (connection.getApartment() != null) {
            com.bms.backend.entity.Apartment apartment = connection.getApartment();

            // Apartment details
            this.apartmentId = apartment.getId();
            this.unitNumber = apartment.getUnitNumber();
            this.unitType = apartment.getUnitType();

            // Property details
            if (apartment.getProperty() != null) {
                this.propertyId = apartment.getProperty().getId();
                this.propertyAddress = apartment.getProperty().getAddress();

                // Get primary image or first image from property images
                if (apartment.getProperty().getImages() != null &&
                    !apartment.getProperty().getImages().isEmpty()) {
                    // Try to find primary image first
                    this.propertyImage = apartment.getProperty().getImages().stream()
                        .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
                        .map(img -> img.getImageUrl())
                        .findFirst()
                        .orElseGet(() ->
                            // If no primary image, get first image URL
                            apartment.getProperty().getImages().stream()
                                .map(img -> img.getImageUrl())
                                .filter(url -> url != null && !url.isEmpty())
                                .findFirst()
                                .orElse(null)
                        );
                }
            }
        }

        // Tenant information
        if (connection.getTenant() != null) {
            this.tenantId = connection.getTenant().getId();
            this.tenantName = connection.getTenant().getFirstName() + " " + connection.getTenant().getLastName();
            this.tenantEmail = connection.getTenant().getEmail();
            this.tenantPhone = connection.getTenant().getPhone();
            this.tenantPhoto = connection.getTenant().getProfileImageUrl();
        }

        // Manager information
        if (connection.getManager() != null) {
            this.managerId = connection.getManager().getId();
            this.managerName = connection.getManager().getFirstName() + " " + connection.getManager().getLastName();
            this.managerEmail = connection.getManager().getEmail();
        }
    }

    private String generateLeaseId(UUID connectionId) {
        return "LEASE-" + connectionId.toString().substring(0, 8).toUpperCase();
    }

    private String calculateLeaseDuration(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return "N/A";
        }

        // Calculate total months using Period
        java.time.Period period = java.time.Period.between(startDate, endDate);
        long totalMonths = period.toTotalMonths();

        // Handle edge case where Period might give 0 months for valid date ranges
        if (totalMonths <= 0 && !startDate.equals(endDate)) {
            // Fallback: calculate months manually
            totalMonths = java.time.temporal.ChronoUnit.MONTHS.between(startDate, endDate);
        }

        if (totalMonths == 0) {
            // Calculate days for very short leases
            long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
            return days + " Day" + (days != 1 ? "s" : "");
        } else if (totalMonths == 12) {
            return "1 Year";
        } else if (totalMonths > 12) {
            long years = totalMonths / 12;
            long remainingMonths = totalMonths % 12;
            if (remainingMonths == 0) {
                return years + " Year" + (years > 1 ? "s" : "");
            } else {
                return years + " Year" + (years > 1 ? "s" : "") + " " + remainingMonths + " Month" + (remainingMonths > 1 ? "s" : "");
            }
        } else {
            return totalMonths + " Month" + (totalMonths != 1 ? "s" : "");
        }
    }

    private String determineLeaseStatus(TenantPropertyConnection connection) {
        if (!connection.getIsActive()) {
            return "TERMINATED";
        }

        LocalDate now = LocalDate.now();
        LocalDate startDate = connection.getStartDate();
        LocalDate endDate = connection.getEndDate();

        if (now.isBefore(startDate)) {
            return "UPCOMING";
        } else if (now.isAfter(endDate)) {
            return "EXPIRED";
        } else {
            return "ACTIVE";
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(String leaseId) {
        this.leaseId = leaseId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    public String getPropertyImage() {
        return propertyImage;
    }

    public void setPropertyImage(String propertyImage) {
        this.propertyImage = propertyImage;
    }

    public UUID getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(UUID propertyId) {
        this.propertyId = propertyId;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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

    public String getPaymentFrequency() {
        return paymentFrequency;
    }

    public void setPaymentFrequency(String paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getLeaseStatus() {
        return leaseStatus;
    }

    public void setLeaseStatus(String leaseStatus) {
        this.leaseStatus = leaseStatus;
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

    public String getTenantPhoto() {
        return tenantPhoto;
    }

    public void setTenantPhoto(String tenantPhoto) {
        this.tenantPhoto = tenantPhoto;
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