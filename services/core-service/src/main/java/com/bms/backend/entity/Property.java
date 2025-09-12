package com.bms.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "properties")
public class Property {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    @JsonIgnore
    private User manager;
    
    @Column(name = "property_name", nullable = false)
    private String propertyName;
    
    @Column(name = "property_manager_name")
    private String propertyManagerName;
    
    @Column(name = "property_address", nullable = false)
    private String propertyAddress;
    
    @Column(name = "property_type")
    private String propertyType;
    
    @Column(name = "square_footage")
    private Integer squareFootage;
    
    @Column(name = "number_of_units")
    private Integer numberOfUnits;
    
    @Column(name = "unit_number")
    private String unitNumber;
    
    @Column(name = "unit_type")
    private String unitType;
    
    @Column(name = "floor")
    private Integer floor;
    
    @Column(name = "bedrooms")
    private Integer bedrooms;
    
    @Column(name = "bathrooms")
    private Integer bathrooms;
    
    @Column(name = "furnished")
    private String furnished;
    
    @Column(name = "balcony")
    private String balcony;
    
    @Column(name = "rent", precision = 10, scale = 2)
    private BigDecimal rent;
    
    @Column(name = "security_deposit", precision = 10, scale = 2)
    private BigDecimal securityDeposit;
    
    @Column(name = "maintenance_charges", precision = 10, scale = 2)
    private BigDecimal maintenanceCharges;
    
    @Column(name = "occupancy")
    private String occupancy;
    
    @Column(name = "utility_meter_number")
    private Long utilityMeterNumber;

    // Default constructor
    public Property() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

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

    public Long getUtilityMeterNumber() {
        return utilityMeterNumber;
    }

    public void setUtilityMeterNumber(Long utilityMeterNumber) {
        this.utilityMeterNumber = utilityMeterNumber;
    }
}