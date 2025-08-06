package com.bms.backend.repository;

import com.bms.backend.entity.RefreshToken;
import com.bms.backend.enums.DeviceType;
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
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    
    List<RefreshToken> findByUserId(UUID userId);
    
    Optional<RefreshToken> findByUserIdAndDeviceId(UUID userId, String deviceId);
    
    List<RefreshToken> findByUserIdAndDeviceType(UUID userId, DeviceType deviceType);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isRevoked = false")
    List<RefreshToken> findActiveTokensByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < :now")
    List<RefreshToken> findExpiredTokens(@Param("now") Instant now);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.isRevoked = true")
    List<RefreshToken> findRevokedTokens();
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user.id = :userId")
    int revokeAllTokensForUser(@Param("userId") UUID userId);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user.id = :userId AND rt.deviceId != :excludeDeviceId")
    int revokeAllTokensForUserExceptDevice(@Param("userId") UUID userId, @Param("excludeDeviceId") String excludeDeviceId);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.tokenHash = :tokenHash")
    int revokeToken(@Param("tokenHash") String tokenHash);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :cutoffDate")
    int deleteExpiredTokensOlderThan(@Param("cutoffDate") Instant cutoffDate);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.isRevoked = true AND rt.createdAt < :cutoffDate")
    int deleteRevokedTokensOlderThan(@Param("cutoffDate") Instant cutoffDate);
    
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isRevoked = false")
    long countActiveTokensForUser(@Param("userId") UUID userId);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.deviceType = :deviceType AND rt.isRevoked = false")
    List<RefreshToken> findActiveTokensByUserIdAndDeviceType(@Param("userId") UUID userId, @Param("deviceType") DeviceType deviceType);
    
    void deleteByUserId(UUID userId);
    
    void deleteByUserIdAndDeviceId(UUID userId, String deviceId);
}