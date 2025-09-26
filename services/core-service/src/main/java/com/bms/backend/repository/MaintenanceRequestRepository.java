package com.bms.backend.repository;

import com.bms.backend.entity.Apartment;
import com.bms.backend.entity.MaintenanceRequest;
import com.bms.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, UUID> {
    
    List<MaintenanceRequest> findByTenant(User tenant);
    
    @Query("SELECT mr FROM MaintenanceRequest mr WHERE mr.apartment.property.manager = :manager")
    List<MaintenanceRequest> findByManager(@Param("manager") User manager);
    
    List<MaintenanceRequest> findByAssignedTo(User assignedTo);
    
    List<MaintenanceRequest> findByApartment(Apartment apartment);
    
    List<MaintenanceRequest> findByStatus(MaintenanceRequest.Status status);
    
    List<MaintenanceRequest> findByPriority(MaintenanceRequest.Priority priority);
    
    @Query("SELECT mr FROM MaintenanceRequest mr WHERE mr.tenant = :tenant " +
           "AND mr.status = :status ORDER BY mr.createdAt DESC")
    List<MaintenanceRequest> findByTenantAndStatus(@Param("tenant") User tenant, 
                                                  @Param("status") MaintenanceRequest.Status status);
    
    @Query("SELECT mr FROM MaintenanceRequest mr WHERE mr.apartment.property.manager = :manager " +
           "AND mr.status = :status ORDER BY mr.createdAt DESC")
    List<MaintenanceRequest> findByManagerAndStatus(@Param("manager") User manager, 
                                                   @Param("status") MaintenanceRequest.Status status);
    
    @Query("SELECT mr FROM MaintenanceRequest mr WHERE mr.apartment.property.manager = :manager " +
           "AND mr.priority = :priority ORDER BY mr.createdAt DESC")
    List<MaintenanceRequest> findByManagerAndPriority(@Param("manager") User manager, 
                                                     @Param("priority") MaintenanceRequest.Priority priority);
    
    @Query("SELECT mr FROM MaintenanceRequest mr WHERE mr.apartment.property.manager = :manager " +
           "AND (LOWER(mr.title) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(mr.description) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(mr.apartment.unitNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(mr.serviceCategory.name) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    List<MaintenanceRequest> findByManagerAndSearchText(@Param("manager") User manager, 
                                                       @Param("searchText") String searchText);
    
    @Query("SELECT mr FROM MaintenanceRequest mr WHERE mr.createdAt >= :startDate " +
           "AND mr.createdAt <= :endDate ORDER BY mr.createdAt DESC")
    List<MaintenanceRequest> findByDateRange(@Param("startDate") Instant startDate, 
                                           @Param("endDate") Instant endDate);
    
    // Dashboard queries
    @Query("SELECT COUNT(mr) FROM MaintenanceRequest mr WHERE mr.apartment.property.manager = :manager " +
           "AND mr.status = :status")
    Long countByManagerAndStatus(@Param("manager") User manager, 
                                @Param("status") MaintenanceRequest.Status status);
    
    @Query("SELECT COUNT(mr) FROM MaintenanceRequest mr WHERE mr.tenant = :tenant " +
           "AND mr.status = :status")
    Long countByTenantAndStatus(@Param("tenant") User tenant, 
                               @Param("status") MaintenanceRequest.Status status);
    
    @Query("SELECT mr FROM MaintenanceRequest mr WHERE mr.apartment.property.manager = :manager " +
           "ORDER BY mr.createdAt DESC")
    Page<MaintenanceRequest> findByManagerOrderByCreatedAtDesc(@Param("manager") User manager, 
                                                              Pageable pageable);
    
    @Query("SELECT mr FROM MaintenanceRequest mr WHERE mr.tenant = :tenant " +
           "ORDER BY mr.createdAt DESC")
    Page<MaintenanceRequest> findByTenantOrderByCreatedAtDesc(@Param("tenant") User tenant, 
                                                             Pageable pageable);
    
    // Additional methods needed by MaintenanceRequestService
    @Query("SELECT mr FROM MaintenanceRequest mr " +
           "LEFT JOIN FETCH mr.photos " +
           "LEFT JOIN FETCH mr.serviceCategory " +
           "WHERE mr.apartment.property.manager = :manager")
    List<MaintenanceRequest> findByApartmentPropertyManager(@Param("manager") User manager);
    
    @Query("SELECT mr FROM MaintenanceRequest mr " +
           "LEFT JOIN FETCH mr.photos " +
           "LEFT JOIN FETCH mr.serviceCategory " +
           "WHERE mr.apartment.tenantEmail = :tenantEmail")
    List<MaintenanceRequest> findByApartmentTenantEmail(@Param("tenantEmail") String tenantEmail);
    
    @Query("SELECT mr FROM MaintenanceRequest mr " +
           "LEFT JOIN FETCH mr.photos " +
           "LEFT JOIN FETCH mr.serviceCategory " +
           "WHERE mr.status = :status " +
           "AND mr.apartment.property.manager = :manager")
    List<MaintenanceRequest> findByStatusAndApartmentPropertyManager(@Param("status") MaintenanceRequest.Status status,
                                                                   @Param("manager") User manager);
    
    @Query("SELECT mr FROM MaintenanceRequest mr " +
           "LEFT JOIN FETCH mr.photos " +
           "LEFT JOIN FETCH mr.serviceCategory " +
           "WHERE mr.priority = :priority " +
           "AND mr.apartment.property.manager = :manager")
    List<MaintenanceRequest> findByPriorityAndApartmentPropertyManager(@Param("priority") MaintenanceRequest.Priority priority,
                                                                      @Param("manager") User manager);
    
    @Query("SELECT mr FROM MaintenanceRequest mr " +
           "LEFT JOIN FETCH mr.photos " +
           "LEFT JOIN FETCH mr.serviceCategory " +
           "WHERE mr.serviceCategory.id = :serviceCategoryId " +
           "AND mr.apartment.property.manager = :manager")
    List<MaintenanceRequest> findByServiceCategoryIdAndApartmentPropertyManager(@Param("serviceCategoryId") UUID serviceCategoryId,
                                                                               @Param("manager") User manager);

    @Query("SELECT mr FROM MaintenanceRequest mr " +
           "LEFT JOIN FETCH mr.photos " +
           "LEFT JOIN FETCH mr.serviceCategory " +
           "WHERE mr.apartment.id = :apartmentId " +
           "AND mr.apartment.property.manager = :manager")
    List<MaintenanceRequest> findByApartmentIdAndApartmentPropertyManager(@Param("apartmentId") UUID apartmentId,
                                                                          @Param("manager") User manager);
    
    @Query("SELECT mr FROM MaintenanceRequest mr " +
           "LEFT JOIN FETCH mr.photos " +
           "LEFT JOIN FETCH mr.serviceCategory " +
           "WHERE mr.apartment.property.manager = :manager " +
           "AND (LOWER(mr.title) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(mr.description) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(mr.apartment.unitNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(mr.serviceCategory.name) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    List<MaintenanceRequest> findBySearchTextAndManager(@Param("searchText") String searchText,
                                                       @Param("manager") User manager);

    // Tenant-specific query methods for dashboard
    @Query("SELECT mr FROM MaintenanceRequest mr " +
           "LEFT JOIN FETCH mr.photos " +
           "LEFT JOIN FETCH mr.serviceCategory " +
           "WHERE " +
           "(mr.tenant IS NOT NULL AND mr.tenant.email = :tenantEmail) OR " +
           "mr.requester.email = :tenantEmail")
    List<MaintenanceRequest> findByTenantEmail(@Param("tenantEmail") String tenantEmail);

    @Query("SELECT mr FROM MaintenanceRequest mr " +
           "LEFT JOIN FETCH mr.photos " +
           "LEFT JOIN FETCH mr.serviceCategory " +
           "WHERE " +
           "((mr.tenant IS NOT NULL AND mr.tenant.email = :tenantEmail) OR " +
           "mr.requester.email = :tenantEmail) AND mr.status = :status " +
           "ORDER BY mr.createdAt DESC")
    List<MaintenanceRequest> findByTenantEmailAndStatus(@Param("tenantEmail") String tenantEmail,
                                                       @Param("status") MaintenanceRequest.Status status);

    @Query("SELECT mr FROM MaintenanceRequest mr " +
           "LEFT JOIN FETCH mr.photos " +
           "LEFT JOIN FETCH mr.serviceCategory " +
           "WHERE " +
           "((mr.tenant IS NOT NULL AND mr.tenant.email = :tenantEmail) OR " +
           "mr.requester.email = :tenantEmail) AND mr.priority = :priority " +
           "ORDER BY mr.createdAt DESC")
    List<MaintenanceRequest> findByTenantEmailAndPriority(@Param("tenantEmail") String tenantEmail,
                                                         @Param("priority") MaintenanceRequest.Priority priority);

    // Method for lease details to check if tenant has maintenance requests for apartment
    @Query("SELECT CASE WHEN COUNT(mr) > 0 THEN true ELSE false END FROM MaintenanceRequest mr " +
           "WHERE mr.apartment.id = :apartmentId AND mr.tenant.id = :tenantId")
    boolean existsByApartmentIdAndTenantId(@Param("apartmentId") UUID apartmentId, 
                                          @Param("tenantId") UUID tenantId);
}