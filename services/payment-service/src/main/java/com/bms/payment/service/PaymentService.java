package com.bms.payment.service;

import com.bms.payment.client.CoreServiceClient;
import com.bms.payment.dto.LeasePaymentDetailsDto;
import com.bms.payment.dto.PaymentIntentRequest;
import com.bms.payment.dto.PaymentIntentResponse;
import com.bms.payment.entity.Customer;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final CustomerService customerService;
    private final CoreServiceClient coreServiceClient;

    @Value("${stripe.publishable.key}")
    private String publishableKey;

    /**
     * Get Stripe publishable key for frontend initialization
     */
    public String getPublishableKey() {
        return publishableKey;
    }

    /**
     * Create a PaymentIntent for card payments
     * SECURE: If leaseId is provided, amount is fetched from core-service
     */
    public PaymentIntentResponse createCardPaymentIntent(PaymentIntentRequest request, String authToken) {
        try {
            Long amount;
            String tenantId = request.getTenantId();
            String tenantEmail = request.getTenantEmail();
            String tenantName = request.getTenantName();
            String tenantPhone = request.getTenantPhone();
            String description = request.getDescription();

            // SECURITY: Fetch amount from core-service if leaseId is provided
            if (request.getLeaseId() != null && !request.getLeaseId().isEmpty()) {
                log.info("Fetching lease payment details for lease: {}", request.getLeaseId());
                LeasePaymentDetailsDto leaseDetails = coreServiceClient.getLeasePaymentDetails(
                    request.getLeaseId(),
                    authToken
                );

                // Use server-side verified amount
                amount = leaseDetails.getTotalPayableAmount()
                    .multiply(BigDecimal.valueOf(100)) // Convert to cents
                    .longValue();

                // Use tenant details from lease
                tenantId = leaseDetails.getTenantId();
                tenantEmail = leaseDetails.getTenantEmail();
                tenantName = leaseDetails.getTenantName();
                tenantPhone = leaseDetails.getTenantPhone();
                description = "Rent payment for " + leaseDetails.getPropertyName() +
                    " - Lease " + leaseDetails.getLeaseId();

                log.info("Verified amount from core-service: ${} for lease {}",
                    leaseDetails.getTotalPayableAmount(), request.getLeaseId());
            } else {
                // For non-lease payments, use provided amount
                if (request.getAmount() == null) {
                    throw new IllegalArgumentException("Amount is required when leaseId is not provided");
                }
                amount = request.getAmount();
            }

            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency(request.getCurrency())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    );

            // Handle tenant ID - create/get customer
            if (tenantId != null) {
                Customer customer = customerService.getOrCreateCustomer(
                        tenantId,
                        tenantEmail,
                        tenantName,
                        tenantPhone
                );
                paramsBuilder.setCustomer(customer.getStripeCustomerId());

                // Use tenant email for receipt if not provided
                if (request.getReceiptEmail() == null && customer.getEmail() != null) {
                    paramsBuilder.setReceiptEmail(customer.getEmail());
                }
            }

            // Add optional parameters
            if (description != null) {
                paramsBuilder.setDescription(description);
            }

            if (request.getReceiptEmail() != null) {
                paramsBuilder.setReceiptEmail(request.getReceiptEmail());
            }

            PaymentIntent intent = PaymentIntent.create(paramsBuilder.build());

            log.info("Created card PaymentIntent: {} for amount: ${}", intent.getId(), amount / 100.0);

            return PaymentIntentResponse.builder()
                    .clientSecret(intent.getClientSecret())
                    .paymentIntentId(intent.getId())
                    .status(intent.getStatus())
                    .amount(intent.getAmount())
                    .currency(intent.getCurrency())
                    .build();

        } catch (StripeException e) {
            log.error("Error creating card PaymentIntent: {}", e.getMessage(), e);
            return PaymentIntentResponse.builder()
                    .error("PAYMENT_INTENT_CREATION_FAILED")
                    .errorMessage(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error creating card PaymentIntent: {}", e.getMessage(), e);
            return PaymentIntentResponse.builder()
                    .error("PAYMENT_INTENT_CREATION_FAILED")
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Create a PaymentIntent for ACH/Bank account payments
     * SECURE: If leaseId is provided, amount is fetched from core-service
     */
    public PaymentIntentResponse createACHPaymentIntent(PaymentIntentRequest request, String authToken) {
        try {
            Long amount;
            String tenantId = request.getTenantId();
            String tenantEmail = request.getTenantEmail();
            String tenantName = request.getTenantName();
            String tenantPhone = request.getTenantPhone();
            String description = request.getDescription();

            // SECURITY: Fetch amount from core-service if leaseId is provided
            if (request.getLeaseId() != null && !request.getLeaseId().isEmpty()) {
                log.info("Fetching lease payment details for lease: {}", request.getLeaseId());
                LeasePaymentDetailsDto leaseDetails = coreServiceClient.getLeasePaymentDetails(
                    request.getLeaseId(),
                    authToken
                );

                // Use server-side verified amount
                amount = leaseDetails.getTotalPayableAmount()
                    .multiply(BigDecimal.valueOf(100)) // Convert to cents
                    .longValue();

                // Use tenant details from lease
                tenantId = leaseDetails.getTenantId();
                tenantEmail = leaseDetails.getTenantEmail();
                tenantName = leaseDetails.getTenantName();
                tenantPhone = leaseDetails.getTenantPhone();
                description = "Rent payment for " + leaseDetails.getPropertyName() +
                    " - Lease " + leaseDetails.getLeaseId();

                log.info("Verified amount from core-service: ${} for lease {}",
                    leaseDetails.getTotalPayableAmount(), request.getLeaseId());
            } else {
                // For non-lease payments, use provided amount
                if (request.getAmount() == null) {
                    throw new IllegalArgumentException("Amount is required when leaseId is not provided");
                }
                amount = request.getAmount();
            }

            // Create payment intent with automatic payment method detection
            // Will include all enabled payment methods (Card, ACH, etc.) from Stripe dashboard
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency(request.getCurrency())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    );

            // Handle tenant ID - create/get customer
            if (tenantId != null) {
                Customer customer = customerService.getOrCreateCustomer(
                        tenantId,
                        tenantEmail,
                        tenantName,
                        tenantPhone
                );
                paramsBuilder.setCustomer(customer.getStripeCustomerId());

                // Use tenant email for receipt if not provided
                if (request.getReceiptEmail() == null && customer.getEmail() != null) {
                    paramsBuilder.setReceiptEmail(customer.getEmail());
                }
            }

            // Add optional parameters
            if (description != null) {
                paramsBuilder.setDescription(description);
            }

            if (request.getReceiptEmail() != null) {
                paramsBuilder.setReceiptEmail(request.getReceiptEmail());
            }

            PaymentIntent intent = PaymentIntent.create(paramsBuilder.build());

            log.info("Created ACH PaymentIntent: {} for amount: ${}", intent.getId(), amount / 100.0);

            return PaymentIntentResponse.builder()
                    .clientSecret(intent.getClientSecret())
                    .paymentIntentId(intent.getId())
                    .status(intent.getStatus())
                    .amount(intent.getAmount())
                    .currency(intent.getCurrency())
                    .build();

        } catch (StripeException e) {
            log.error("Error creating ACH PaymentIntent: {}", e.getMessage(), e);
            return PaymentIntentResponse.builder()
                    .error("PAYMENT_INTENT_CREATION_FAILED")
                    .errorMessage(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error creating ACH PaymentIntent: {}", e.getMessage(), e);
            return PaymentIntentResponse.builder()
                    .error("PAYMENT_INTENT_CREATION_FAILED")
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Retrieve a PaymentIntent by ID
     */
    public PaymentIntentResponse getPaymentIntent(String paymentIntentId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

            log.info("Retrieved PaymentIntent: {}", intent.getId());

            return PaymentIntentResponse.builder()
                    .clientSecret(intent.getClientSecret())
                    .paymentIntentId(intent.getId())
                    .status(intent.getStatus())
                    .amount(intent.getAmount())
                    .currency(intent.getCurrency())
                    .build();

        } catch (StripeException e) {
            log.error("Error retrieving PaymentIntent {}: {}", paymentIntentId, e.getMessage(), e);
            return PaymentIntentResponse.builder()
                    .error("PAYMENT_INTENT_RETRIEVAL_FAILED")
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Cancel a PaymentIntent
     */
    public PaymentIntentResponse cancelPaymentIntent(String paymentIntentId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            PaymentIntent canceledIntent = intent.cancel();

            log.info("Canceled PaymentIntent: {}", canceledIntent.getId());

            return PaymentIntentResponse.builder()
                    .paymentIntentId(canceledIntent.getId())
                    .status(canceledIntent.getStatus())
                    .amount(canceledIntent.getAmount())
                    .currency(canceledIntent.getCurrency())
                    .build();

        } catch (StripeException e) {
            log.error("Error canceling PaymentIntent {}: {}", paymentIntentId, e.getMessage(), e);
            return PaymentIntentResponse.builder()
                    .error("PAYMENT_INTENT_CANCELLATION_FAILED")
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}
