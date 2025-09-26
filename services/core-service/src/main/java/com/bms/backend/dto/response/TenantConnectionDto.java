package com.bms.backend.dto.response;

import com.bms.backend.entity.TenantPropertyConnection;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.UUID;

public class TenantConnectionDto {

    private UUID tenantId;
    private String tenantName;
    private String tenantEmail;
    private String tenantPhone;
    private String tenantImage;
    private UUID apartmentId;
    private UUID propertyId;
    private String propertyName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate rentStart;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate rentEnd;
    
    private Double rentAmount;
    private Double securityDeposit;
    private String paymentFrequency;
    private String notes;
    private Boolean isActive;
    
    // Placeholder for future implementation
    private LocalDate lastPaidOn;
    
    // Default constructor
    public TenantConnectionDto() {}
    
    // Constructor from TenantPropertyConnection
    public TenantConnectionDto(TenantPropertyConnection connection) {
        if (connection.getTenant() != null) {
            this.tenantId = connection.getTenant().getId();
            this.tenantName = connection.getTenant().getFirstName() + " " + connection.getTenant().getLastName();
            this.tenantEmail = connection.getTenant().getEmail();
            this.tenantPhone = connection.getTenant().getPhone();
            this.tenantImage = connection.getTenant().getProfileImageUrl();
        }
        this.propertyName = connection.getPropertyName();
        this.rentStart = connection.getStartDate();
        this.rentEnd = connection.getEndDate();
        this.rentAmount = connection.getMonthlyRent();
        this.securityDeposit = connection.getSecurityDeposit();
        this.paymentFrequency = connection.getPaymentFrequency();
        this.notes = connection.getNotes();
        this.isActive = connection.getIsActive();
        // apartmentId and propertyId will need to be set separately
    }
    
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

    public String getTenantImage() {
        return tenantImage;
    }

    public void setTenantImage(String tenantImage) {
        this.tenantImage = tenantImage;
    }

    public UUID getApartmentId() {
        return apartmentId;
    }
    
    public void setApartmentId(UUID apartmentId) {
        this.apartmentId = apartmentId;
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
    
    public LocalDate getRentStart() {
        return rentStart;
    }
    
    public void setRentStart(LocalDate rentStart) {
        this.rentStart = rentStart;
    }
    
    public LocalDate getRentEnd() {
        return rentEnd;
    }
    
    public void setRentEnd(LocalDate rentEnd) {
        this.rentEnd = rentEnd;
    }
    
    public Double getRentAmount() {
        return rentAmount;
    }
    
    public void setRentAmount(Double rentAmount) {
        this.rentAmount = rentAmount;
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
    
    public LocalDate getLastPaidOn() {
        return lastPaidOn;
    }
    
    public void setLastPaidOn(LocalDate lastPaidOn) {
        this.lastPaidOn = lastPaidOn;
    }
}