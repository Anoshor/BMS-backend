package com.bms.backend.dto.response;

import com.bms.backend.entity.TenantPropertyConnection;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class TenantPropertyDto {
    
    private UUID connectionId;
    private String propertyName;
    private String propertyAddress;
    private UUID propertyId;
    private UUID apartmentId;
    private String unitId; // Unit identifier like "A101"
    private String unitNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd") 
    private LocalDate endDate;
    
    private Double monthlyRent;
    private Double securityDeposit;
    private String paymentFrequency;
    private String notes;
    private Boolean isActive;
    
    // Manager details
    private String managerName;
    private String managerEmail;
    private String managerPhone;
    
    private Instant createdAt;
    private Instant updatedAt;
    
    // Default constructor
    public TenantPropertyDto() {}
    
    // Constructor from TenantPropertyConnection
    public TenantPropertyDto(TenantPropertyConnection connection) {
        this.connectionId = connection.getId();
        this.propertyName = connection.getPropertyName();
        this.startDate = connection.getStartDate();
        this.endDate = connection.getEndDate();
        this.monthlyRent = connection.getMonthlyRent();
        this.securityDeposit = connection.getSecurityDeposit();
        this.paymentFrequency = connection.getPaymentFrequency();
        this.notes = connection.getNotes();
        this.isActive = connection.getIsActive();
        this.createdAt = connection.getCreatedAt();
        this.updatedAt = connection.getUpdatedAt();
        
        // Manager details
        if (connection.getManager() != null) {
            this.managerName = connection.getManager().getFirstName() + " " + connection.getManager().getLastName();
            this.managerEmail = connection.getManager().getEmail();
            this.managerPhone = connection.getManager().getPhone();
        }
    }
    
    // Getters and Setters
    public UUID getConnectionId() {
        return connectionId;
    }
    
    public void setConnectionId(UUID connectionId) {
        this.connectionId = connectionId;
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
    
    public String getUnitId() {
        return unitId;
    }
    
    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }
    
    public String getUnitNumber() {
        return unitNumber;
    }
    
    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
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