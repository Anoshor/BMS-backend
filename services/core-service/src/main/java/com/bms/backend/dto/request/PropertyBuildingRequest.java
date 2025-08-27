package com.bms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PropertyBuildingRequest {
    
    @NotBlank(message = "Property name is required")
    private String name;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    private String propertyType;
    
    private String residentialType;
    
    @Positive(message = "Total units must be positive")
    private Integer totalUnits;
    
    @Positive(message = "Total floors must be positive")
    private Integer totalFloors;
    
    private Integer yearBuilt;
    
    private String amenities;

    // Default constructor
    public PropertyBuildingRequest() {}

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getResidentialType() {
        return residentialType;
    }

    public void setResidentialType(String residentialType) {
        this.residentialType = residentialType;
    }

    public Integer getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(Integer totalUnits) {
        this.totalUnits = totalUnits;
    }

    public Integer getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(Integer totalFloors) {
        this.totalFloors = totalFloors;
    }

    public Integer getYearBuilt() {
        return yearBuilt;
    }

    public void setYearBuilt(Integer yearBuilt) {
        this.yearBuilt = yearBuilt;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }
}