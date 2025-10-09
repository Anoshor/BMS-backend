package com.bms.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "property_buildings")
public class PropertyBuilding {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    @JsonIgnore
    private User manager;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;
    
    @Column(name = "property_type")
    private String propertyType; // apartment complex, single family, etc.
    
    @Column(name = "residential_type")
    private String residentialType; // residential, commercial, mixed use
    
    @Column(name = "total_units")
    private Integer totalUnits;
    
    @Column(name = "total_floors")
    private Integer totalFloors;
    
    @Column(name = "year_built")
    private Integer yearBuilt;
    
    @Column(name = "amenities", columnDefinition = "TEXT")
    private String amenities; // JSON string or comma-separated
    
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Apartment> apartments;
    
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PropertyImage> images;

    @Transient
    private Long vacantUnits;

    @Transient
    private Long occupiedUnits;

    @Transient
    private Long underMaintenanceUnits;

    @Transient
    private String managerName;

    @Transient
    private List<String> imageUrls;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Default constructor
    public PropertyBuilding() {}

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

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

    public List<Apartment> getApartments() {
        return apartments;
    }

    public void setApartments(List<Apartment> apartments) {
        this.apartments = apartments;
    }

    public List<PropertyImage> getImages() {
        return images;
    }

    public void setImages(List<PropertyImage> images) {
        this.images = images;
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

    public Long getVacantUnits() {
        return vacantUnits;
    }

    public void setVacantUnits(Long vacantUnits) {
        this.vacantUnits = vacantUnits;
    }

    public Long getOccupiedUnits() {
        return occupiedUnits;
    }

    public void setOccupiedUnits(Long occupiedUnits) {
        this.occupiedUnits = occupiedUnits;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public Long getUnderMaintenanceUnits() {
        return underMaintenanceUnits;
    }

    public void setUnderMaintenanceUnits(Long underMaintenanceUnits) {
        this.underMaintenanceUnits = underMaintenanceUnits;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}