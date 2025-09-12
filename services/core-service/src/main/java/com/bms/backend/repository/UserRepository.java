package com.bms.backend.repository;

import com.bms.backend.entity.User;
import com.bms.backend.enums.AccountStatus;
import com.bms.backend.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhone(String phone);
    
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.phone = :identifier")
    Optional<User> findByEmailOrPhone(@Param("identifier") String identifier);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
    
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email OR u.phone = :phone")
    boolean existsByEmailOrPhone(@Param("email") String email, @Param("phone") String phone);
    
    List<User> findByRole(UserRole role);
    
    List<User> findByAccountStatus(AccountStatus accountStatus);
    
    List<User> findByRoleAndAccountStatus(UserRole role, AccountStatus accountStatus);
    
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :cutoffDate")
    List<User> findUnverifiedUsersOlderThan(@Param("cutoffDate") Instant cutoffDate);
    
    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil < :now")
    List<User> findUsersWithExpiredLocks(@Param("now") Instant now);
    
    Optional<User> findByEmailVerificationToken(String token);
    
    Optional<User> findByPasswordResetToken(String token);
    
    @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token AND u.passwordResetExpires > :now")
    Optional<User> findByValidPasswordResetToken(@Param("token") String token, @Param("now") Instant now);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.createdAt >= :startDate AND u.createdAt <= :endDate")
    long countByRoleAndCreatedAtBetween(@Param("role") UserRole role, 
                                       @Param("startDate") Instant startDate, 
                                       @Param("endDate") Instant endDate);
    
    @Query("SELECT u FROM User u WHERE u.lastLogin IS NULL AND u.createdAt < :cutoffDate")
    List<User> findNeverLoggedInUsersOlderThan(@Param("cutoffDate") Instant cutoffDate);
    
    @Query("SELECT u FROM User u WHERE u.role = 'TENANT' AND u.accountStatus = 'ACTIVE' AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "u.phone LIKE CONCAT('%', :searchText, '%'))")
    List<User> findTenantsBySearchText(@Param("searchText") String searchText);
}