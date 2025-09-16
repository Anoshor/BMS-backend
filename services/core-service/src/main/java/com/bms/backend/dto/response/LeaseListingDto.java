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

        // Tenant information
        if (connection.getTenant() != null) {
            this.tenantId = connection.getTenant().getId();
            this.tenantName = connection.getTenant().getFirstName() + " " + connection.getTenant().getLastName();
            this.tenantEmail = connection.getTenant().getEmail();
            this.tenantPhone = connection.getTenant().getPhone();
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

        long months = java.time.Period.between(startDate, endDate).toTotalMonths();
        if (months == 12) {
            return "1 Year";
        } else if (months > 12) {
            long years = months / 12;
            long remainingMonths = months % 12;
            if (remainingMonths == 0) {
                return years + " Year" + (years > 1 ? "s" : "");
            } else {
                return years + " Year" + (years > 1 ? "s" : "") + " " + remainingMonths + " Month" + (remainingMonths > 1 ? "s" : "");
            }
        } else {
            return months + " Month" + (months > 1 ? "s" : "");
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