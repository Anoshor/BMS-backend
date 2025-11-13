package com.bms.payment.repository;

import com.bms.payment.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByTenantId(String tenantId);

    Optional<Customer> findByStripeCustomerId(String stripeCustomerId);

    boolean existsByTenantId(String tenantId);
}
