package com.bms.backend.repository;

import com.bms.backend.entity.TenantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantProfileRepository extends JpaRepository<TenantProfile, UUID> {
    
    Optional<TenantProfile> findByUserId(UUID userId);
    
    @Query("SELECT tp FROM TenantProfile tp WHERE tp.user.email = :email")
    Optional<TenantProfile> findByUserEmail(@Param("email") String email);
    
    @Query("SELECT tp FROM TenantProfile tp WHERE tp.user.phone = :phone")
    Optional<TenantProfile> findByUserPhone(@Param("phone") String phone);
    
    List<TenantProfile> findByProfileCompleted(Boolean profileCompleted);
    
    @Query("SELECT tp FROM TenantProfile tp WHERE tp.profileCompleted = false AND tp.createdAt < :cutoffDate")
    List<TenantProfile> findIncompleteProfilesOlderThan(@Param("cutoffDate") java.time.Instant cutoffDate);
    
    @Query("SELECT tp FROM TenantProfile tp WHERE tp.monthlyIncome >= :minIncome AND tp.monthlyIncome <= :maxIncome")
    List<TenantProfile> findByIncomeRange(@Param("minIncome") BigDecimal minIncome, 
                                         @Param("maxIncome") BigDecimal maxIncome);
    
    @Query("SELECT tp FROM TenantProfile tp WHERE tp.preferredRentRangeMin <= :rent AND tp.preferredRentRangeMax >= :rent")
    List<TenantProfile> findByRentPreference(@Param("rent") BigDecimal rent);

    @Query("SELECT tp FROM TenantProfile tp WHERE :location MEMBER OF tp.preferredLocations")
    List<TenantProfile> findByPreferredLocationsContaining(@Param("location") String location);
    
    @Query("SELECT tp FROM TenantProfile tp WHERE tp.occupation = :occupation")
    List<TenantProfile> findByOccupation(@Param("occupation") String occupation);
    
    @Query("SELECT tp FROM TenantProfile tp WHERE tp.idProofUrl IS NOT NULL AND tp.incomeProofUrl IS NOT NULL")
    List<TenantProfile> findProfilesWithAllDocuments();
    
    @Query("SELECT tp FROM TenantProfile tp WHERE tp.emergencyContactName IS NOT NULL AND tp.emergencyContactPhone IS NOT NULL")
    List<TenantProfile> findProfilesWithEmergencyContact();
    
    @Query("SELECT COUNT(tp) FROM TenantProfile tp WHERE tp.profileCompleted = true")
    long countCompletedProfiles();
    
    void deleteByUserId(UUID userId);
}