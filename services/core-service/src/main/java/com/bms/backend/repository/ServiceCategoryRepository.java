package com.bms.backend.repository;

import com.bms.backend.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, UUID> {
    
    Optional<ServiceCategory> findByName(String name);
    
    @Query("SELECT sc FROM ServiceCategory sc WHERE " +
           "LOWER(sc.name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(sc.description) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<ServiceCategory> findBySearchText(@Param("searchText") String searchText);
    
    List<ServiceCategory> findAllByOrderByNameAsc();
}