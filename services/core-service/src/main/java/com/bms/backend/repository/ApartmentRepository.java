package com.bms.backend.repository;

import com.bms.backend.entity.Apartment;
import com.bms.backend.entity.PropertyBuilding;
import com.bms.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {
    
    List<Apartment> findByProperty(PropertyBuilding property);
    
    @Query("SELECT a FROM Apartment a WHERE a.property.manager = :manager")
    List<Apartment> findByManager(@Param("manager") User manager);
    
    @Query("SELECT a FROM Apartment a WHERE a.property.manager = :manager AND LOWER(a.occupancyStatus) = LOWER(:status)")
    List<Apartment> findByManagerAndOccupancyStatus(@Param("manager") User manager, 
                                                   @Param("status") String status);
    
    @Query("SELECT a FROM Apartment a WHERE a.tenantEmail = :email")
    List<Apartment> findByTenantEmail(@Param("email") String email);
    
    @Query("SELECT a FROM Apartment a WHERE a.property.manager = :manager " +
           "AND (LOWER(a.unitNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(a.tenantName) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(a.tenantEmail) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(a.property.name) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    List<Apartment> findByManagerAndSearchText(@Param("manager") User manager, 
                                              @Param("searchText") String searchText);
    
    @Query("SELECT a FROM Apartment a WHERE " +
           "LOWER(a.unitNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(a.tenantName) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(a.tenantEmail) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(a.property.name) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Apartment> findByGlobalSearch(@Param("searchText") String searchText);
    
    Optional<Apartment> findByPropertyAndUnitNumber(PropertyBuilding property, String unitNumber);
    
    @Query("SELECT COUNT(a) FROM Apartment a WHERE a.property.manager = :manager AND LOWER(a.occupancyStatus) = 'vacant'")
    Long countVacantByManager(@Param("manager") User manager);
    
    @Query("SELECT COUNT(a) FROM Apartment a WHERE a.property.manager = :manager AND LOWER(a.occupancyStatus) = 'occupied'")
    Long countOccupiedByManager(@Param("manager") User manager);
    
    // Additional methods needed by ApartmentService
    @Query("SELECT a FROM Apartment a WHERE a.property.manager = :manager")
    List<Apartment> findByPropertyManager(@Param("manager") User manager);
    
    @Query("SELECT a FROM Apartment a WHERE a.property.manager = :manager " +
           "AND LOWER(a.occupancyStatus) = 'occupied'")
    List<Apartment> findOccupiedByManager(@Param("manager") User manager);
    
    @Query("SELECT a FROM Apartment a WHERE a.property.manager = :manager " +
           "AND LOWER(a.occupancyStatus) = 'vacant'")
    List<Apartment> findUnoccupiedByManager(@Param("manager") User manager);
    
    List<Apartment> findByTenantPhone(String tenantPhone);
    
    List<Apartment> findByTenantNameContaining(String tenantName);
}