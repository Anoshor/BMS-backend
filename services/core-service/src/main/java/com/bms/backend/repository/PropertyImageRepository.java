package com.bms.backend.repository;

import com.bms.backend.entity.PropertyBuilding;
import com.bms.backend.entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, UUID> {
    
    List<PropertyImage> findByProperty(PropertyBuilding property);
    
    List<PropertyImage> findByPropertyOrderByDisplayOrderAsc(PropertyBuilding property);
    
    @Query("SELECT pi FROM PropertyImage pi WHERE pi.property.id = :propertyId ORDER BY pi.displayOrder ASC")
    List<PropertyImage> findByPropertyIdOrderByDisplayOrderAsc(@Param("propertyId") UUID propertyId);
    
    Optional<PropertyImage> findByPropertyAndIsPrimary(PropertyBuilding property, Boolean isPrimary);
    
    @Query("SELECT pi FROM PropertyImage pi WHERE pi.property.id = :propertyId AND pi.isPrimary = true")
    Optional<PropertyImage> findPrimaryImageByPropertyId(@Param("propertyId") UUID propertyId);
    
    @Query("SELECT COUNT(pi) FROM PropertyImage pi WHERE pi.property.id = :propertyId")
    Long countByPropertyId(@Param("propertyId") UUID propertyId);
}