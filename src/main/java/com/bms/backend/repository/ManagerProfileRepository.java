package com.bms.backend.repository;

import com.bms.backend.entity.ManagerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ManagerProfileRepository extends JpaRepository<ManagerProfile, UUID> {
    
    Optional<ManagerProfile> findByUserId(UUID userId);
    
    @Query("SELECT mp FROM ManagerProfile mp WHERE mp.user.email = :email")
    Optional<ManagerProfile> findByUserEmail(@Param("email") String email);
    
    @Query("SELECT mp FROM ManagerProfile mp WHERE mp.user.phone = :phone")
    Optional<ManagerProfile> findByUserPhone(@Param("phone") String phone);
    
    Optional<ManagerProfile> findByBusinessLicenseNumber(String businessLicenseNumber);
    
    Optional<ManagerProfile> findByTaxId(String taxId);
    
    List<ManagerProfile> findByBusinessVerified(Boolean businessVerified);
    
    List<ManagerProfile> findByDocumentsVerified(Boolean documentsVerified);
    
    List<ManagerProfile> findByProfileCompleted(Boolean profileCompleted);
    
    @Query("SELECT mp FROM ManagerProfile mp WHERE mp.businessVerified = true AND mp.documentsVerified = true")
    List<ManagerProfile> findFullyVerifiedProfiles();
    
    @Query("SELECT mp FROM ManagerProfile mp WHERE mp.profileCompleted = false AND mp.createdAt < :cutoffDate")
    List<ManagerProfile> findIncompleteProfilesOlderThan(@Param("cutoffDate") java.time.Instant cutoffDate);
    
    @Query("SELECT mp FROM ManagerProfile mp WHERE mp.businessVerified = false AND mp.createdAt < :cutoffDate")
    List<ManagerProfile> findUnverifiedBusinessesOlderThan(@Param("cutoffDate") java.time.Instant cutoffDate);
    
    @Query("SELECT mp FROM ManagerProfile mp WHERE mp.documentsVerified = false AND mp.businessLicenseUrl IS NOT NULL AND mp.taxCertificateUrl IS NOT NULL AND mp.identityProofUrl IS NOT NULL")
    List<ManagerProfile> findPendingDocumentVerification();
    
    List<ManagerProfile> findByCompanyNameContainingIgnoreCase(String companyName);
    
    @Query("SELECT mp FROM ManagerProfile mp WHERE mp.businessLicenseUrl IS NOT NULL AND mp.taxCertificateUrl IS NOT NULL AND mp.identityProofUrl IS NOT NULL")
    List<ManagerProfile> findProfilesWithAllDocuments();
    
    @Query("SELECT mp FROM ManagerProfile mp WHERE mp.bankAccountNumber IS NOT NULL AND mp.bankName IS NOT NULL")
    List<ManagerProfile> findProfilesWithBankingDetails();
    
    @Query("SELECT COUNT(mp) FROM ManagerProfile mp WHERE mp.businessVerified = true")
    long countVerifiedBusinesses();
    
    @Query("SELECT COUNT(mp) FROM ManagerProfile mp WHERE mp.documentsVerified = true")
    long countVerifiedDocuments();
    
    @Query("SELECT COUNT(mp) FROM ManagerProfile mp WHERE mp.profileCompleted = true")
    long countCompletedProfiles();
    
    void deleteByUserId(UUID userId);
}