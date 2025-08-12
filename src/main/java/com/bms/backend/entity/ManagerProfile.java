package com.bms.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "manager_profiles")
public class ManagerProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    
    @Column(name = "company_name")
    private String companyName;
    
    @Column(name = "business_license_number")
    private String businessLicenseNumber;
    
    @Column(name = "tax_id")
    private String taxId;
    
    @Column(name = "business_address", columnDefinition = "TEXT")
    private String businessAddress;
    
    @Column(name = "business_phone")
    private String businessPhone;
    
    @Column(name = "business_email")
    private String businessEmail;
    
    @Column(name = "business_license_url", columnDefinition = "TEXT")
    private String businessLicenseUrl;
    
    @Column(name = "tax_certificate_url", columnDefinition = "TEXT")
    private String taxCertificateUrl;
    
    @Column(name = "identity_proof_url", columnDefinition = "TEXT")
    private String identityProofUrl;
    
    @Column(name = "bank_account_number")
    private String bankAccountNumber;
    
    @Column(name = "bank_name")
    private String bankName;
    
    @Column(name = "bank_routing_number")
    private String bankRoutingNumber;
    
    @Column(name = "account_holder_name")
    private String accountHolderName;
    
    @Column(name = "business_verified")
    private Boolean businessVerified = false;
    
    @Column(name = "documents_verified")
    private Boolean documentsVerified = false;
    
    @Column(name = "profile_completed")
    private Boolean profileCompleted = false;
    
    @Column(name = "admin_approved")
    private Boolean adminApproved = false;
    
    @Column(name = "approval_status")
    private String approvalStatus = "PENDING"; // PENDING, APPROVED, REJECTED
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "approval_date")
    private Instant approvalDate;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    // Constructors
    public ManagerProfile() {}
    
    public ManagerProfile(User user) {
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
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getBusinessLicenseNumber() {
        return businessLicenseNumber;
    }
    
    public void setBusinessLicenseNumber(String businessLicenseNumber) {
        this.businessLicenseNumber = businessLicenseNumber;
    }
    
    public String getTaxId() {
        return taxId;
    }
    
    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }
    
    public String getBusinessAddress() {
        return businessAddress;
    }
    
    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }
    
    public String getBusinessPhone() {
        return businessPhone;
    }
    
    public void setBusinessPhone(String businessPhone) {
        this.businessPhone = businessPhone;
    }
    
    public String getBusinessEmail() {
        return businessEmail;
    }
    
    public void setBusinessEmail(String businessEmail) {
        this.businessEmail = businessEmail;
    }
    
    public String getBusinessLicenseUrl() {
        return businessLicenseUrl;
    }
    
    public void setBusinessLicenseUrl(String businessLicenseUrl) {
        this.businessLicenseUrl = businessLicenseUrl;
    }
    
    public String getTaxCertificateUrl() {
        return taxCertificateUrl;
    }
    
    public void setTaxCertificateUrl(String taxCertificateUrl) {
        this.taxCertificateUrl = taxCertificateUrl;
    }
    
    public String getIdentityProofUrl() {
        return identityProofUrl;
    }
    
    public void setIdentityProofUrl(String identityProofUrl) {
        this.identityProofUrl = identityProofUrl;
    }
    
    public String getBankAccountNumber() {
        return bankAccountNumber;
    }
    
    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public String getBankRoutingNumber() {
        return bankRoutingNumber;
    }
    
    public void setBankRoutingNumber(String bankRoutingNumber) {
        this.bankRoutingNumber = bankRoutingNumber;
    }
    
    public String getAccountHolderName() {
        return accountHolderName;
    }
    
    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }
    
    public Boolean getBusinessVerified() {
        return businessVerified;
    }
    
    public void setBusinessVerified(Boolean businessVerified) {
        this.businessVerified = businessVerified;
    }
    
    public Boolean getDocumentsVerified() {
        return documentsVerified;
    }
    
    public void setDocumentsVerified(Boolean documentsVerified) {
        this.documentsVerified = documentsVerified;
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
    public boolean hasRequiredDocuments() {
        return businessLicenseUrl != null && 
               taxCertificateUrl != null && 
               identityProofUrl != null;
    }
    
    public boolean hasBusinessDetails() {
        return companyName != null && 
               businessLicenseNumber != null && 
               businessAddress != null;
    }
    
    public boolean hasBankingDetails() {
        return bankAccountNumber != null && 
               bankName != null && 
               accountHolderName != null;
    }
    
    public boolean isFullyVerified() {
        return Boolean.TRUE.equals(businessVerified) && 
               Boolean.TRUE.equals(documentsVerified);
    }
    
    public boolean isProfileComplete() {
        return Boolean.TRUE.equals(profileCompleted) && 
               hasBusinessDetails() && 
               hasRequiredDocuments();
    }
    
    public Boolean getAdminApproved() {
        return adminApproved;
    }
    
    public void setAdminApproved(Boolean adminApproved) {
        this.adminApproved = adminApproved;
    }
    
    public String getApprovalStatus() {
        return approvalStatus;
    }
    
    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
    
    public String getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public Instant getApprovalDate() {
        return approvalDate;
    }
    
    public void setApprovalDate(Instant approvalDate) {
        this.approvalDate = approvalDate;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public boolean isApproved() {
        return Boolean.TRUE.equals(adminApproved) && "APPROVED".equals(approvalStatus);
    }
    
    public boolean isPending() {
        return "PENDING".equals(approvalStatus);
    }
    
    public boolean isRejected() {
        return "REJECTED".equals(approvalStatus);
    }
}