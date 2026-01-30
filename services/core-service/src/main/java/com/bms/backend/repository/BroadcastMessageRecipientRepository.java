package com.bms.backend.repository;

import com.bms.backend.entity.BroadcastMessage;
import com.bms.backend.entity.BroadcastMessageRecipient;
import com.bms.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BroadcastMessageRecipientRepository extends JpaRepository<BroadcastMessageRecipient, UUID> {

    List<BroadcastMessageRecipient> findByTenantOrderByCreatedAtDesc(User tenant);

    Optional<BroadcastMessageRecipient> findByBroadcastMessageAndTenant(BroadcastMessage broadcastMessage, User tenant);

    @Query("SELECT bmr FROM BroadcastMessageRecipient bmr " +
           "JOIN bmr.broadcastMessage bm " +
           "WHERE bmr.tenant = :tenant " +
           "AND bm.isActive = true " +
           "AND bm.expiresAt > :now " +
           "AND bmr.isDismissed = false " +
           "ORDER BY bm.createdAt DESC")
    List<BroadcastMessageRecipient> findActiveMessagesForTenant(@Param("tenant") User tenant,
                                                                 @Param("now") Instant now);

    @Query("SELECT bmr FROM BroadcastMessageRecipient bmr " +
           "JOIN bmr.broadcastMessage bm " +
           "WHERE bmr.tenant = :tenant " +
           "ORDER BY bm.createdAt DESC")
    List<BroadcastMessageRecipient> findAllMessagesForTenant(@Param("tenant") User tenant);

    @Query("SELECT COUNT(bmr) FROM BroadcastMessageRecipient bmr " +
           "JOIN bmr.broadcastMessage bm " +
           "WHERE bmr.tenant = :tenant " +
           "AND bm.isActive = true " +
           "AND bm.expiresAt > :now " +
           "AND bmr.isRead = false " +
           "AND bmr.isDismissed = false")
    Long countUnreadForTenant(@Param("tenant") User tenant, @Param("now") Instant now);

    List<BroadcastMessageRecipient> findByBroadcastMessage(BroadcastMessage broadcastMessage);

    @Query("SELECT COUNT(bmr) FROM BroadcastMessageRecipient bmr WHERE bmr.broadcastMessage = :message")
    Long countByBroadcastMessage(@Param("message") BroadcastMessage message);

    @Query("SELECT COUNT(bmr) FROM BroadcastMessageRecipient bmr WHERE bmr.broadcastMessage = :message AND bmr.isRead = true")
    Long countReadByBroadcastMessage(@Param("message") BroadcastMessage message);

    @Query("SELECT COUNT(bmr) FROM BroadcastMessageRecipient bmr WHERE bmr.broadcastMessage = :message AND bmr.isDismissed = true")
    Long countDismissedByBroadcastMessage(@Param("message") BroadcastMessage message);

    boolean existsByBroadcastMessageAndTenant(BroadcastMessage broadcastMessage, User tenant);

    void deleteByBroadcastMessage(BroadcastMessage broadcastMessage);
}
