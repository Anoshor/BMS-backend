package com.bms.backend.repository;

import com.bms.backend.entity.TenantPropertyConnection;
import com.bms.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TenantPropertyConnectionRepository extends JpaRepository<TenantPropertyConnection, UUID> {

    List<TenantPropertyConnection> findByManagerAndIsActive(User manager, Boolean isActive);

    List<TenantPropertyConnection> findByManagerOrderByCreatedAtDesc(User manager);

    List<TenantPropertyConnection> findByManagerAndIsActiveOrderByCreatedAtDesc(User manager, Boolean isActive);

    List<TenantPropertyConnection> findByTenantAndIsActive(User tenant, Boolean isActive);

    @Query("SELECT tpc FROM TenantPropertyConnection tpc WHERE tpc.manager = :manager AND tpc.isActive = true AND " +
           "(LOWER(tpc.tenant.firstName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(tpc.tenant.lastName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(tpc.tenant.email) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(tpc.propertyName) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    List<TenantPropertyConnection> findByManagerAndSearchText(@Param("manager") User manager, 
                                                              @Param("searchText") String searchText);

    boolean existsByTenantAndPropertyNameAndIsActive(User tenant, String propertyName, Boolean isActive);

    List<TenantPropertyConnection> findByTenantAndManagerAndIsActive(User tenant, User manager, Boolean isActive);

    List<TenantPropertyConnection> findByApartment(com.bms.backend.entity.Apartment apartment);
}