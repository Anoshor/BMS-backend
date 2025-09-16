package com.bms.backend.repository;

import com.bms.backend.entity.MaintenanceRequest;
import com.bms.backend.entity.MaintenanceUpdate;
import com.bms.backend.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceUpdateRepository extends JpaRepository<MaintenanceUpdate, UUID> {
    
    List<MaintenanceUpdate> findByMaintenanceRequest(MaintenanceRequest maintenanceRequest);
    
    List<MaintenanceUpdate> findByMaintenanceRequestOrderByCreatedAtAsc(MaintenanceRequest maintenanceRequest);

    List<MaintenanceUpdate> findByMaintenanceRequestOrderByCreatedAtDesc(MaintenanceRequest maintenanceRequest);

    List<MaintenanceUpdate> findByMaintenanceRequestIdOrderByCreatedAtDesc(UUID maintenanceRequestId);
    
    List<MaintenanceUpdate> findByUpdatedBy(User user);
    
    List<MaintenanceUpdate> findByUpdateType(MaintenanceUpdate.UpdateType updateType);
    
    @Query("SELECT mu FROM MaintenanceUpdate mu WHERE " +
           "mu.maintenanceRequest.tenant IS NOT NULL AND mu.maintenanceRequest.tenant.email = :tenantEmail " +
           "OR mu.maintenanceRequest.requester.email = :tenantEmail " +
           "ORDER BY mu.createdAt DESC")
    List<MaintenanceUpdate> findRecentUpdatesByTenantEmail(@Param("tenantEmail") String tenantEmail, 
                                                          Pageable pageable);
}