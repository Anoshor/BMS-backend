package com.bms.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDetailsRequest {

    @NotBlank(message = "Property name is required")
    private String propertyName;

    @NotBlank(message = "Property manager name is required")
    private String propertyManagerName;

    @NotBlank(message = "Property address is required")
    private String propertyAddress;

    @NotBlank(message = "Property type is required")
    @Pattern(regexp = "^(residential|commercial)$", message = "Property type must be residential or commercial")
    private String propertyType;

    @NotNull(message = "Square footage is required")
    @Min(value = 1, message = "Square footage must be positive")
    private Integer squareFootage;

    @NotNull(message = "Number of units is required")
    @Min(value = 1, message = "Number of units must be positive")
    private Integer numberOfUnits;

    @NotBlank(message = "Unit number is required")
    private String unitNumber;

    @NotBlank(message = "Unit type is required")
    private String unitType;

    @NotNull(message = "Floor number is required")
    @Min(value = 1, message = "Floor must be positive")
    private Integer floor;

    @NotNull(message = "Number of bedrooms is required")
    @Min(value = 0, message = "Bedrooms cannot be negative")
    private Integer bedrooms;

    @NotNull(message = "Number of bathrooms is required")
    @Min(value = 0, message = "Bathrooms cannot be negative")
    private Integer bathrooms;

    @NotBlank(message = "Furnished status is required")
    @Pattern(regexp = "^(furnished|semi-furnished|unfurnished)$", message = "Furnished must be furnished, semi-furnished, or unfurnished")
    private String furnished;

    @NotBlank(message = "Balcony status is required")
    @Pattern(regexp = "^(yes|no)$", message = "Balcony must be yes or no")
    private String balcony;

    @NotNull(message = "Rent is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rent must be positive")
    private BigDecimal rent;

    @NotNull(message = "Security deposit is required")
    @DecimalMin(value = "0.0", message = "Security deposit cannot be negative")
    private BigDecimal securityDeposit;

    @NotNull(message = "Maintenance charges are required")
    @DecimalMin(value = "0.0", message = "Maintenance charges cannot be negative")
    private BigDecimal maintenanceCharges;

    @NotBlank(message = "Occupancy status is required")
    @Pattern(regexp = "^(occupied|vacant|under-maintenance)$", message = "Occupancy must be occupied, vacant, or under-maintenance")
    private String occupancy;

    @NotNull(message = "Utility meter number is required")
    @Min(value = 1, message = "Utility meter number must be positive")
    private Integer utilityMeterNumber;

    // Manual getters for key fields that Lombok might not be generating
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyManagerName() {
        return propertyManagerName;
    }

    public void setPropertyManagerName(String propertyManagerName) {
        this.propertyManagerName = propertyManagerName;
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public Integer getSquareFootage() {
        return squareFootage;
    }

    public void setSquareFootage(Integer squareFootage) {
        this.squareFootage = squareFootage;
    }

    public Integer getNumberOfUnits() {
        return numberOfUnits;
    }

    public void setNumberOfUnits(Integer numberOfUnits) {
        this.numberOfUnits = numberOfUnits;
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

    public Integer getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
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

    public String getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(String occupancy) {
        this.occupancy = occupancy;
    }

    public Integer getUtilityMeterNumber() {
        return utilityMeterNumber;
    }

    public void setUtilityMeterNumber(Integer utilityMeterNumber) {
        this.utilityMeterNumber = utilityMeterNumber;
    }
}