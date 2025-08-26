package com.bms.backend.repository;

import com.bms.backend.entity.PropertyBuilding;
import com.bms.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyBuildingRepository extends JpaRepository<PropertyBuilding, UUID> {
    
    List<PropertyBuilding> findByManager(User manager);
    
    @Query("SELECT pb FROM PropertyBuilding pb WHERE pb.manager = :manager " +
           "AND (LOWER(pb.name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(pb.address) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    List<PropertyBuilding> findByManagerAndSearchText(@Param("manager") User manager, 
                                                     @Param("searchText") String searchText);
    
    @Query("SELECT pb FROM PropertyBuilding pb WHERE " +
           "LOWER(pb.name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(pb.address) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<PropertyBuilding> findByGlobalSearch(@Param("searchText") String searchText);
    
    List<PropertyBuilding> findByPropertyType(String propertyType);
    
    @Query("SELECT COUNT(pb) FROM PropertyBuilding pb WHERE pb.manager = :manager")
    Long countByManager(@Param("manager") User manager);
    
    // Additional method needed by PropertyBuildingService
    @Query("SELECT pb FROM PropertyBuilding pb WHERE " +
           "LOWER(pb.name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(pb.address) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<PropertyBuilding> findBySearchText(@Param("searchText") String searchText);
}