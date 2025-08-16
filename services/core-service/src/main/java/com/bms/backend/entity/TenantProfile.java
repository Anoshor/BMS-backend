package com.bms.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tenant_profiles")
public class TenantProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    
    @Column(name = "occupation")
    private String occupation;
    
    @Column(name = "employer_name")
    private String employerName;
    
    @Column(name = "monthly_income", precision = 12, scale = 2)
    private BigDecimal monthlyIncome;
    
    @Column(name = "emergency_contact_name")
    private String emergencyContactName;
    
    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;
    
    @Column(name = "emergency_contact_relation")
    private String emergencyContactRelation;
    
    @Column(name = "id_proof_url")
    private String idProofUrl;
    
    @Column(name = "income_proof_url")
    private String incomeProofUrl;
    
    @Column(name = "preferred_rent_range_min", precision = 10, scale = 2)
    private BigDecimal preferredRentRangeMin;
    
    @Column(name = "preferred_rent_range_max", precision = 10, scale = 2)
    private BigDecimal preferredRentRangeMax;
    
    @ElementCollection
    @CollectionTable(name = "tenant_preferred_locations", joinColumns = @JoinColumn(name = "tenant_profile_id"))
    @Column(name = "location")
    private List<String> preferredLocations;
    
    @Column(name = "profile_completed")
    private Boolean profileCompleted = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    // Constructors
    public TenantProfile() {}
    
    public TenantProfile(User user) {
        this.user = user;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getOccupation() {
        return occupation;
    }
    
    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }
    
    public String getEmployerName() {
        return employerName;
    }
    
    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }
    
    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }
    
    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }
    
    public String getEmergencyContactName() {
        return emergencyContactName;
    }
    
    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }
    
    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }
    
    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }
    
    public String getEmergencyContactRelation() {
        return emergencyContactRelation;
    }
    
    public void setEmergencyContactRelation(String emergencyContactRelation) {
        this.emergencyContactRelation = emergencyContactRelation;
    }
    
    public String getIdProofUrl() {
        return idProofUrl;
    }
    
    public void setIdProofUrl(String idProofUrl) {
        this.idProofUrl = idProofUrl;
    }
    
    public String getIncomeProofUrl() {
        return incomeProofUrl;
    }
    
    public void setIncomeProofUrl(String incomeProofUrl) {
        this.incomeProofUrl = incomeProofUrl;
    }
    
    public BigDecimal getPreferredRentRangeMin() {
        return preferredRentRangeMin;
    }
    
    public void setPreferredRentRangeMin(BigDecimal preferredRentRangeMin) {
        this.preferredRentRangeMin = preferredRentRangeMin;
    }
    
    public BigDecimal getPreferredRentRangeMax() {
        return preferredRentRangeMax;
    }
    
    public void setPreferredRentRangeMax(BigDecimal preferredRentRangeMax) {
        this.preferredRentRangeMax = preferredRentRangeMax;
    }
    
    public List<String> getPreferredLocations() {
        return preferredLocations;
    }
    
    public void setPreferredLocations(List<String> preferredLocations) {
        this.preferredLocations = preferredLocations;
    }
    
    public Boolean getProfileCompleted() {
        return profileCompleted;
    }
    
    public void setProfileCompleted(Boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
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
    
    // Helper methods
    public boolean hasDocuments() {
        return idProofUrl != null && incomeProofUrl != null;
    }
    
    public boolean hasEmergencyContact() {
        return emergencyContactName != null && emergencyContactPhone != null;
    }
    
    public boolean isProfileComplete() {
        return Boolean.TRUE.equals(profileCompleted) && 
               occupation != null && 
               monthlyIncome != null && 
               hasEmergencyContact();
    }
}