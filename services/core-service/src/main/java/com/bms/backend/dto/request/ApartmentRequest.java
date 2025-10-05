package com.bms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public class ApartmentRequest {

    @NotNull(message = "Property ID is required")
    private UUID propertyId;

    @NotBlank(message = "Unit number is required")
    private String unitNumber;
    
    private String unitType;
    
    @Positive(message = "Floor must be positive")
    private Integer floor;
    
    @Positive(message = "Bedrooms must be positive")
    private Integer bedrooms;
    
    @Positive(message = "Bathrooms must be positive")
    private BigDecimal bathrooms; // Support half baths like 2.5
    
    private Integer squareFootage;
    
    private String furnished;
    
    private String balcony;
    
    private BigDecimal rent;
    
    private BigDecimal securityDeposit;
    
    private BigDecimal maintenanceCharges;
    
    private String occupancyStatus;
    
    private String utilityMeterNumbers; // JSON format: {"electric":"123", "gas":"456", "water":"789"}
    
    private String documents; // JSON format: [{"name":"lease.pdf","url":"s3://...","type":"lease"}]
    
    private String tenantName;
    
    private String tenantEmail;
    
    private String tenantPhone;

    // Default constructor
    public ApartmentRequest() {}

    // Getters and setters
    public UUID getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(UUID propertyId) {
        this.propertyId = propertyId;
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

    public BigDecimal getRent() {
        return rent;
    }

    public void setRent(BigDecimal rent) {
        this.rent = rent;
    }

    public BigDecimal getSecurityDeposit() {
        return securityDeposit;
    }

    public void setSecurityDeposit(BigDecimal securityDeposit) {
        this.securityDeposit = securityDeposit;
    }

    public BigDecimal getMaintenanceCharges() {
        return maintenanceCharges;
    }

    public void setMaintenanceCharges(BigDecimal maintenanceCharges) {
        this.maintenanceCharges = maintenanceCharges;
    }

    public String getOccupancyStatus() {
        return occupancyStatus;
    }

    public void setOccupancyStatus(String occupancyStatus) {
        this.occupancyStatus = occupancyStatus;
    }

    public String getUtilityMeterNumbers() {
        return utilityMeterNumbers;
    }

    public void setUtilityMeterNumbers(String utilityMeterNumbers) {
        this.utilityMeterNumbers = utilityMeterNumbers;
    }

    public String getDocuments() {
        return documents;
    }

    public void setDocuments(String documents) {
        this.documents = documents;
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
}