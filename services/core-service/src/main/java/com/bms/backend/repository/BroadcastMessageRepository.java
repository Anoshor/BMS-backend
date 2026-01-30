package com.bms.backend.repository;

import com.bms.backend.entity.BroadcastMessage;
import com.bms.backend.entity.User;
import com.bms.backend.enums.BroadcastPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface BroadcastMessageRepository extends JpaRepository<BroadcastMessage, UUID> {

    List<BroadcastMessage> findByManagerOrderByCreatedAtDesc(User manager);

    List<BroadcastMessage> findByManagerAndIsActiveOrderByCreatedAtDesc(User manager, Boolean isActive);

    @Query("SELECT bm FROM BroadcastMessage bm WHERE bm.manager = :manager AND bm.isActive = :isActive " +
           "AND bm.expiresAt > :now ORDER BY bm.createdAt DESC")
    List<BroadcastMessage> findActiveNonExpiredByManager(@Param("manager") User manager,
                                                          @Param("isActive") Boolean isActive,
                                                          @Param("now") Instant now);

    @Query("SELECT bm FROM BroadcastMessage bm WHERE bm.manager = :manager AND bm.priority = :priority " +
           "ORDER BY bm.createdAt DESC")
    List<BroadcastMessage> findByManagerAndPriority(@Param("manager") User manager,
                                                     @Param("priority") BroadcastPriority priority);

    @Query("SELECT bm FROM BroadcastMessage bm WHERE bm.manager = :manager " +
           "AND (LOWER(bm.title) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(bm.content) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
           "ORDER BY bm.createdAt DESC")
    List<BroadcastMessage> findByManagerAndSearchText(@Param("manager") User manager,
                                                       @Param("searchText") String searchText);

    @Query("SELECT bm FROM BroadcastMessage bm WHERE bm.isActive = false OR bm.expiresAt <= :now")
    List<BroadcastMessage> findExpiredOrInactive(@Param("now") Instant now);

    @Query("SELECT COUNT(bm) FROM BroadcastMessage bm WHERE bm.manager = :manager AND bm.isActive = true " +
           "AND bm.expiresAt > :now")
    Long countActiveByManager(@Param("manager") User manager, @Param("now") Instant now);
}
