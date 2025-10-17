package com.bms.payment.service;

import com.bms.payment.entity.Customer;
import com.bms.payment.repository.CustomerRepository;
import com.stripe.exception.StripeException;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public Customer getOrCreateCustomer(String tenantId, String email, String name, String phone) {
        log.info("Getting or creating customer for tenant: {}", tenantId);

        // Check if customer already exists
        return customerRepository.findByTenantId(tenantId)
                .orElseGet(() -> {
                    try {
                        // Create Stripe customer
                        CustomerCreateParams params = CustomerCreateParams.builder()
                                .setEmail(email)
                                .setName(name)
                                .setPhone(phone)
                                .putMetadata("tenant_id", tenantId)
                                .build();

                        com.stripe.model.Customer stripeCustomer = com.stripe.model.Customer.create(params);
                        log.info("Created Stripe customer: {}", stripeCustomer.getId());

                        // Save to database
                        Customer customer = Customer.builder()
                                .tenantId(tenantId)
                                .stripeCustomerId(stripeCustomer.getId())
                                .email(email)
                                .name(name)
                                .phone(phone)
                                .build();

                        return customerRepository.save(customer);
                    } catch (StripeException e) {
                        log.error("Error creating Stripe customer", e);
                        throw new RuntimeException("Failed to create customer: " + e.getMessage());
                    }
                });
    }

    @Transactional
    public Customer updateCustomer(String tenantId, String email, String name, String phone) {
        Customer customer = customerRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Customer not found for tenant: " + tenantId));

        try {
            // Update Stripe customer
            com.stripe.model.Customer stripeCustomer = com.stripe.model.Customer.retrieve(customer.getStripeCustomerId());

            CustomerUpdateParams params = CustomerUpdateParams.builder()
                    .setEmail(email)
                    .setName(name)
                    .setPhone(phone)
                    .build();

            stripeCustomer.update(params);
            log.info("Updated Stripe customer: {}", stripeCustomer.getId());

            // Update local database
            customer.setEmail(email);
            customer.setName(name);
            customer.setPhone(phone);

            return customerRepository.save(customer);
        } catch (StripeException e) {
            log.error("Error updating Stripe customer", e);
            throw new RuntimeException("Failed to update customer: " + e.getMessage());
        }
    }

    public Customer getCustomerByTenantId(String tenantId) {
        return customerRepository.findByTenantId(tenantId)
                .orElse(null);
    }
}
