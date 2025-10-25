package com.bms.payment.service;

import com.bms.payment.client.CoreServiceClient;
import com.bms.payment.dto.LeasePaymentDetailsDto;
import com.bms.payment.dto.PaymentIntentRequest;
import com.bms.payment.dto.PaymentIntentResponse;
import com.bms.payment.entity.Customer;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final CustomerService customerService;
    private final CoreServiceClient coreServiceClient;
    private final WebhookService webhookService;

    @Value("${stripe.publishable.key}")
    private String publishableKey;

    public PaymentService(CustomerService customerService, CoreServiceClient coreServiceClient, WebhookService webhookService) {
        this.customerService = customerService;
        this.coreServiceClient = coreServiceClient;
        this.webhookService = webhookService;
    }

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
            log.info("=== Creating Card Payment Intent ===");
            log.info("Request - LeaseId: {}, TenantId: {}, Amount: {}",
                request.getLeaseId(), request.getTenantId(), request.getAmount());

            Long amount;
            String tenantId = request.getTenantId();
            String tenantEmail = request.getTenantEmail();
            String tenantName = request.getTenantName();
            String tenantPhone = request.getTenantPhone();
            String description = request.getDescription();

            // SECURITY: Fetch amount from core-service if leaseId is provided
            if (request.getLeaseId() != null && !request.getLeaseId().isEmpty()) {
                log.info("Fetching lease payment details from core-service for lease: {}", request.getLeaseId());
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
                    )
                    // Add metadata for webhook processing
                    .putMetadata("tenantId", tenantId)
                    .putMetadata("connectionId", request.getLeaseId()); // leaseId is actually connectionId (UUID)

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
            log.info("✅ PaymentIntent created successfully - ID: {}, Amount: {}, Status: {}",
                intent.getId(), intent.getAmount(), intent.getStatus());
            log.info("PaymentIntent Metadata - TenantId: {}, ConnectionId: {}",
                intent.getMetadata().get("tenantId"), intent.getMetadata().get("connectionId"));

            // Immediately record payment as PENDING in core-service
            // This ensures payment history exists even if webhooks fail
            try {
                log.info("🔄 Attempting to record PENDING payment in core-service...");
                webhookService.recordPaymentTransaction(intent, "PENDING");
                log.info("✅ Successfully recorded PENDING payment transaction for PaymentIntent: {}", intent.getId());
            } catch (Exception e) {
                log.error("❌ Failed to record initial PENDING transaction: {}", e.getMessage(), e);
                // Continue - webhook will retry later
            }

            return PaymentIntentResponse.builder()
                    .clientSecret(intent.getClientSecret())
                    .paymentIntentId(intent.getId())
                    .status(intent.getStatus())
                    .amount(intent.getAmount())
                    .currency(intent.getCurrency())
                    .build();

        } catch (StripeException e) {
            log.error("❌ Stripe error creating card payment intent - Code: {}, Message: {}",
                e.getCode(), e.getMessage(), e);
            return PaymentIntentResponse.builder()
                    .error("PAYMENT_INTENT_CREATION_FAILED")
                    .errorMessage(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error creating card payment intent: {}", e.getMessage(), e);
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
            log.info("=== Creating ACH Payment Intent ===");
            log.info("Request - LeaseId: {}, TenantId: {}, Amount: {}",
                request.getLeaseId(), request.getTenantId(), request.getAmount());

            Long amount;
            String tenantId = request.getTenantId();
            String tenantEmail = request.getTenantEmail();
            String tenantName = request.getTenantName();
            String tenantPhone = request.getTenantPhone();
            String description = request.getDescription();

            // SECURITY: Fetch amount from core-service if leaseId is provided
            if (request.getLeaseId() != null && !request.getLeaseId().isEmpty()) {
                log.info("Fetching lease payment details from core-service for lease: {}", request.getLeaseId());
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
                    )
                    // Add metadata for webhook processing
                    .putMetadata("tenantId", tenantId)
                    .putMetadata("connectionId", request.getLeaseId()); // leaseId is actually connectionId (UUID)

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
            log.info("✅ PaymentIntent created successfully - ID: {}, Amount: {}, Status: {}",
                intent.getId(), intent.getAmount(), intent.getStatus());
            log.info("PaymentIntent Metadata - TenantId: {}, ConnectionId: {}",
                intent.getMetadata().get("tenantId"), intent.getMetadata().get("connectionId"));

            // Immediately record payment as PENDING in core-service
            // This ensures payment history exists even if webhooks fail
            try {
                log.info("🔄 Attempting to record PENDING payment in core-service...");
                webhookService.recordPaymentTransaction(intent, "PENDING");
                log.info("✅ Successfully recorded PENDING payment transaction for PaymentIntent: {}", intent.getId());
            } catch (Exception e) {
                log.error("❌ Failed to record initial PENDING transaction: {}", e.getMessage(), e);
                // Continue - webhook will retry later
            }

            return PaymentIntentResponse.builder()
                    .clientSecret(intent.getClientSecret())
                    .paymentIntentId(intent.getId())
                    .status(intent.getStatus())
                    .amount(intent.getAmount())
                    .currency(intent.getCurrency())
                    .build();

        } catch (StripeException e) {
            log.error("❌ Stripe error creating ACH payment intent - Code: {}, Message: {}",
                e.getCode(), e.getMessage(), e);
            return PaymentIntentResponse.builder()
                    .error("PAYMENT_INTENT_CREATION_FAILED")
                    .errorMessage(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error creating ACH payment intent: {}", e.getMessage(), e);
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
            log.info("Retrieving PaymentIntent: {}", paymentIntentId);
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

            log.info("PaymentIntent retrieved - Status: {}, Amount: {}", intent.getStatus(), intent.getAmount());
            return PaymentIntentResponse.builder()
                    .clientSecret(intent.getClientSecret())
                    .paymentIntentId(intent.getId())
                    .status(intent.getStatus())
                    .amount(intent.getAmount())
                    .currency(intent.getCurrency())
                    .build();

        } catch (StripeException e) {
            log.error("Failed to retrieve PaymentIntent: {} - Code: {}, Message: {}",
                paymentIntentId, e.getCode(), e.getMessage(), e);
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
            log.info("Canceling PaymentIntent: {}", paymentIntentId);
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            PaymentIntent canceledIntent = intent.cancel();

            log.info("PaymentIntent canceled successfully: {}", paymentIntentId);
            return PaymentIntentResponse.builder()
                    .paymentIntentId(canceledIntent.getId())
                    .status(canceledIntent.getStatus())
                    .amount(canceledIntent.getAmount())
                    .currency(canceledIntent.getCurrency())
                    .build();

        } catch (StripeException e) {
            log.error("Failed to cancel PaymentIntent: {} - Code: {}, Message: {}",
                paymentIntentId, e.getCode(), e.getMessage(), e);
            return PaymentIntentResponse.builder()
                    .error("PAYMENT_INTENT_CANCELLATION_FAILED")
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}
