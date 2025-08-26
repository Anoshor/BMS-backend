package com.bms.backend.repository;

import com.bms.backend.entity.MaintenanceRequest;
import com.bms.backend.entity.MaintenanceRequestPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceRequestPhotoRepository extends JpaRepository<MaintenanceRequestPhoto, UUID> {
    
    List<MaintenanceRequestPhoto> findByMaintenanceRequest(MaintenanceRequest maintenanceRequest);
    
    List<MaintenanceRequestPhoto> findByMaintenanceRequestId(UUID maintenanceRequestId);
    
    List<MaintenanceRequestPhoto> findByMaintenanceRequestOrderByCreatedAtAsc(MaintenanceRequest maintenanceRequest);
}