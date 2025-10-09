package com.bms.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "apartments")
public class Apartment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @JsonIgnore
    private PropertyBuilding property;
    
    @Column(name = "unit_number", nullable = false)
    private String unitNumber;
    
    @Column(name = "unit_type")
    private String unitType; // 1BHK, 2BHK, Studio, etc.
    
    @Column(name = "floor")
    private Integer floor;
    
    @Column(name = "bedrooms")
    private Integer bedrooms;
    
    @Column(name = "bathrooms", precision = 3, scale = 1)
    private BigDecimal bathrooms; // Support half baths like 2.5
    
    @Column(name = "square_footage")
    private Integer squareFootage;
    
    @Column(name = "furnished")
    private String furnished; // furnished, semi-furnished, unfurnished
    
    @Column(name = "balcony")
    private String balcony; // yes, no

    // Base pricing - used for vacant units and as starting point for leases
    @Column(name = "base_rent", precision = 10, scale = 2)
    private BigDecimal baseRent;

    @Column(name = "base_security_deposit", precision = 10, scale = 2)
    private BigDecimal baseSecurityDeposit;

    @Column(name = "maintenance_charges", precision = 10, scale = 2)
    private BigDecimal maintenanceCharges;

    // Current pricing - DERIVED from active lease (TRANSIENT - not stored in DB)
    @Transient
    private BigDecimal currentRent;

    @Transient
    private BigDecimal currentSecurityDeposit;

    @Transient
    private UUID currentLeaseId;
    
    @Column(name = "occupancy_status")
    private String occupancyStatus; // vacant, occupied, maintenance
    
    @Column(name = "utility_meter_numbers", columnDefinition = "TEXT")
    private String utilityMeterNumbers; // JSON string: {"electric":"123", "gas":"456", "water":"789"}
    
    @Column(name = "tenant_name")
    private String tenantName;

    @Column(name = "tenant_email")
    private String tenantEmail;

    @Column(name = "tenant_phone")
    private String tenantPhone;

    @Transient
    private UUID tenantId;

    @Transient
    private UUID connectionId;

    @Column(name = "images", columnDefinition = "TEXT")
    private String images; // JSON string for image URLs: ["https://cdn.../image1.jpg", "https://cdn.../image2.jpg"]

    @Transient
    private java.util.List<String> imageUrls; // Deserialized list of image URLs for API response

    @Column(name = "documents", columnDefinition = "TEXT")
    private String documents; // JSON string for document URLs/metadata: [{"name":"lease.pdf","url":"s3://...","type":"lease"}]

    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ApartmentDocument> apartmentDocuments;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Default constructor
    public Apartment() {}

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PropertyBuilding getProperty() {
        return property;
    }

    public void setProperty(PropertyBuilding property) {
        this.property = property;
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

    public BigDecimal getCurrentRent() {
        return currentRent;
    }

    public void setCurrentRent(BigDecimal currentRent) {
        this.currentRent = currentRent;
    }

    public BigDecimal getCurrentSecurityDeposit() {
        return currentSecurityDeposit;
    }

    public void setCurrentSecurityDeposit(BigDecimal currentSecurityDeposit) {
        this.currentSecurityDeposit = currentSecurityDeposit;
    }

    public UUID getCurrentLeaseId() {
        return currentLeaseId;
    }

    public void setCurrentLeaseId(UUID currentLeaseId) {
        this.currentLeaseId = currentLeaseId;
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

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(UUID connectionId) {
        this.connectionId = connectionId;
    }

    public java.util.List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(java.util.List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getDocuments() {
        return documents;
    }

    public void setDocuments(String documents) {
        this.documents = documents;
    }

    public List<ApartmentDocument> getApartmentDocuments() {
        return apartmentDocuments;
    }

    public void setApartmentDocuments(List<ApartmentDocument> apartmentDocuments) {
        this.apartmentDocuments = apartmentDocuments;
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