package com.bms.backend.repository;

import com.bms.backend.entity.PaymentTransaction;
import com.bms.backend.entity.TenantPropertyConnection;
import com.bms.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    // Find by tenant
    List<PaymentTransaction> findByTenantOrderByCreatedAtDesc(User tenant);

    // Find by tenant and status
    List<PaymentTransaction> findByTenantAndStatusOrderByCreatedAtDesc(User tenant, PaymentTransaction.PaymentStatus status);

    // Find by connection (lease)
    List<PaymentTransaction> findByConnectionOrderByCreatedAtDesc(TenantPropertyConnection connection);

    // Find by Stripe Payment Intent ID
    Optional<PaymentTransaction> findByStripePaymentIntentId(String stripePaymentIntentId);

    // Find all payments for tenant with date range filter
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.tenant = :tenant " +
           "AND (CAST(:startDate AS timestamp) IS NULL OR pt.createdAt >= :startDate) " +
           "AND (CAST(:endDate AS timestamp) IS NULL OR pt.createdAt <= :endDate) " +
           "ORDER BY pt.createdAt DESC")
    List<PaymentTransaction> findByTenantWithDateRange(
            @Param("tenant") User tenant,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    // Find paid payments for tenant
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.tenant = :tenant " +
           "AND pt.status = 'PAID' " +
           "AND (CAST(:startDate AS timestamp) IS NULL OR pt.createdAt >= :startDate) " +
           "AND (CAST(:endDate AS timestamp) IS NULL OR pt.createdAt <= :endDate) " +
           "ORDER BY pt.createdAt DESC")
    List<PaymentTransaction> findPaidByTenantWithDateRange(
            @Param("tenant") User tenant,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    // Find pending payments for tenant
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.tenant = :tenant " +
           "AND pt.status = 'PENDING' " +
           "AND (CAST(:startDate AS timestamp) IS NULL OR pt.createdAt >= :startDate) " +
           "AND (CAST(:endDate AS timestamp) IS NULL OR pt.createdAt <= :endDate) " +
           "ORDER BY pt.createdAt DESC")
    List<PaymentTransaction> findPendingByTenantWithDateRange(
            @Param("tenant") User tenant,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    // Find overdue payments for tenant (pending/failed and past due date)
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.tenant = :tenant " +
           "AND pt.status IN ('PENDING', 'FAILED') " +
           "AND pt.dueDate < :currentDate " +
           "AND (CAST(:startDate AS timestamp) IS NULL OR pt.createdAt >= :startDate) " +
           "AND (CAST(:endDate AS timestamp) IS NULL OR pt.createdAt <= :endDate) " +
           "ORDER BY pt.dueDate ASC")
    List<PaymentTransaction> findOverdueByTenantWithDateRange(
            @Param("tenant") User tenant,
            @Param("currentDate") Instant currentDate,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    // Count overdue payments
    @Query("SELECT COUNT(pt) FROM PaymentTransaction pt WHERE pt.tenant = :tenant " +
           "AND pt.status IN ('PENDING', 'FAILED') " +
           "AND pt.dueDate < :currentDate")
    long countOverdueByTenant(
            @Param("tenant") User tenant,
            @Param("currentDate") Instant currentDate
    );
}
