package com.bms.backend.repository;

import com.bms.backend.entity.Property;
import com.bms.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    
    List<Property> findByManager(User manager);
    
    @Query("SELECT p FROM Property p WHERE p.manager = :manager AND p.occupancy = 'vacant'")
    List<Property> findVacantPropertiesByManager(@Param("manager") User manager);
    
    @Query("SELECT p FROM Property p WHERE p.manager = :manager AND p.occupancy = 'occupied'")
    List<Property> findOccupiedPropertiesByManager(@Param("manager") User manager);
    
    Optional<Property> findByPropertyNameAndManager(String propertyName, User manager);
    
    @Query("SELECT p FROM Property p WHERE p.manager = :manager AND " +
           "(LOWER(p.propertyName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(p.propertyAddress) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(p.unitNumber) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    List<Property> searchPropertiesByManager(@Param("manager") User manager, @Param("searchText") String searchText);
}