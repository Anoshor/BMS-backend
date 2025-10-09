package com.bms.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for updating an apartment/unit - propertyId is NOT required
 */
public class UpdateApartmentRequest {

    @NotBlank(message = "Unit number is required")
    private String unitNumber;

    private String unitType;

    @NotNull(message = "Floor is required")
    @Positive(message = "Floor must be a positive number")
    private Integer floor;

    @NotNull(message = "Bedrooms is required")
    @Positive(message = "Bedrooms must be a positive number")
    private Integer bedrooms;

    @NotNull(message = "Bathrooms is required")
    @Positive(message = "Bathrooms must be a positive number")
    private BigDecimal bathrooms;

    private Integer squareFootage;

    private String furnished;

    private String balcony;

    @JsonAlias("rent")  // Accept both "baseRent" and "rent" from frontend
    private BigDecimal baseRent;

    @JsonAlias({"securityDeposit", "security_deposit"})  // Accept multiple field names
    private BigDecimal baseSecurityDeposit;

    private BigDecimal maintenanceCharges;

    private String occupancyStatus;

    private String utilityMeterNumbers;

    private String documents;

    // Optional: array of S3 image URLs - if provided, replaces existing images
    private List<String> images;

    // Default constructor
    public UpdateApartmentRequest() {}

    // Getters and setters
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

    public BigDecimal getBaseRent() {
        return baseRent;
    }

    public void setBaseRent(BigDecimal baseRent) {
        this.baseRent = baseRent;
    }

    public BigDecimal getBaseSecurityDeposit() {
        return baseSecurityDeposit;
    }

    public void setBaseSecurityDeposit(BigDecimal baseSecurityDeposit) {
        this.baseSecurityDeposit = baseSecurityDeposit;
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

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
