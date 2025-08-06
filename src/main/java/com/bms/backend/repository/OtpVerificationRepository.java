package com.bms.backend.repository;

import com.bms.backend.entity.OtpVerification;
import com.bms.backend.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, UUID> {
    
    Optional<OtpVerification> findByIdentifierAndOtpType(String identifier, OtpType otpType);
    
    @Query("SELECT ov FROM OtpVerification ov WHERE ov.identifier = :identifier AND ov.otpType = :otpType AND ov.isUsed = false AND ov.expiresAt > :now")
    Optional<OtpVerification> findValidOtpByIdentifierAndType(@Param("identifier") String identifier, 
                                                              @Param("otpType") OtpType otpType, 
                                                              @Param("now") Instant now);
    
    @Query("SELECT ov FROM OtpVerification ov WHERE ov.identifier = :identifier AND ov.otpCode = :otpCode AND ov.otpType = :otpType AND ov.isUsed = false AND ov.expiresAt > :now")
    Optional<OtpVerification> findValidOtpByIdentifierCodeAndType(@Param("identifier") String identifier, 
                                                                  @Param("otpCode") String otpCode,
                                                                  @Param("otpType") OtpType otpType, 
                                                                  @Param("now") Instant now);
    
    List<OtpVerification> findByIdentifier(String identifier);
    
    List<OtpVerification> findByOtpType(OtpType otpType);
    
    @Query("SELECT ov FROM OtpVerification ov WHERE ov.expiresAt < :now")
    List<OtpVerification> findExpiredOtps(@Param("now") Instant now);
    
    @Query("SELECT ov FROM OtpVerification ov WHERE ov.isUsed = true")
    List<OtpVerification> findUsedOtps();
    
    @Query("SELECT ov FROM OtpVerification ov WHERE ov.attempts >= 3")
    List<OtpVerification> findOtpsWithMaxAttempts();
    
    @Modifying
    @Query("DELETE FROM OtpVerification ov WHERE ov.expiresAt < :cutoffDate")
    int deleteExpiredOtpsOlderThan(@Param("cutoffDate") Instant cutoffDate);
    
    @Modifying
    @Query("DELETE FROM OtpVerification ov WHERE ov.isUsed = true AND ov.createdAt < :cutoffDate")
    int deleteUsedOtpsOlderThan(@Param("cutoffDate") Instant cutoffDate);
    
    @Modifying
    @Query("UPDATE OtpVerification ov SET ov.isUsed = true WHERE ov.identifier = :identifier AND ov.otpType = :otpType")
    int markOtpAsUsed(@Param("identifier") String identifier, @Param("otpType") OtpType otpType);
    
    @Query("SELECT COUNT(ov) FROM OtpVerification ov WHERE ov.identifier = :identifier AND ov.otpType = :otpType AND ov.createdAt > :since")
    long countOtpAttemptsSince(@Param("identifier") String identifier, 
                              @Param("otpType") OtpType otpType, 
                              @Param("since") Instant since);
    
    @Query("SELECT COUNT(ov) FROM OtpVerification ov WHERE ov.identifier = :identifier AND ov.createdAt > :since")
    long countAllOtpAttemptsSince(@Param("identifier") String identifier, @Param("since") Instant since);
    
    void deleteByIdentifierAndOtpType(String identifier, OtpType otpType);
}