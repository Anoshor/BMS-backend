package com.bms.payment.service;

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

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final CustomerService customerService;

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
     */
    public PaymentIntentResponse createCardPaymentIntent(PaymentIntentRequest request) {
        try {
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(request.getAmount())
                    .setCurrency(request.getCurrency())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    );

            // Handle tenant ID - create/get customer
            if (request.getTenantId() != null) {
                Customer customer = customerService.getOrCreateCustomer(
                        request.getTenantId(),
                        request.getTenantEmail(),
                        request.getTenantName(),
                        request.getTenantPhone()
                );
                paramsBuilder.setCustomer(customer.getStripeCustomerId());

                // Use tenant email for receipt if not provided
                if (request.getReceiptEmail() == null && customer.getEmail() != null) {
                    paramsBuilder.setReceiptEmail(customer.getEmail());
                }
            }

            // Add optional parameters
            if (request.getDescription() != null) {
                paramsBuilder.setDescription(request.getDescription());
            }

            if (request.getReceiptEmail() != null) {
                paramsBuilder.setReceiptEmail(request.getReceiptEmail());
            }

            PaymentIntent intent = PaymentIntent.create(paramsBuilder.build());

            log.info("Created card PaymentIntent: {} for amount: {}", intent.getId(), request.getAmount());

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
     */
    public PaymentIntentResponse createACHPaymentIntent(PaymentIntentRequest request) {
        try {
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(request.getAmount())
                    .setCurrency(request.getCurrency())
                    .addPaymentMethodType("us_bank_account")
                    .setPaymentMethodOptions(
                            PaymentIntentCreateParams.PaymentMethodOptions.builder()
                                    .setUsBankAccount(
                                            PaymentIntentCreateParams.PaymentMethodOptions.UsBankAccount.builder()
                                                    .setVerificationMethod(
                                                            PaymentIntentCreateParams.PaymentMethodOptions.UsBankAccount.VerificationMethod.INSTANT
                                                    )
                                                    .build()
                                    )
                                    .build()
                    );

            // Handle tenant ID - create/get customer
            if (request.getTenantId() != null) {
                Customer customer = customerService.getOrCreateCustomer(
                        request.getTenantId(),
                        request.getTenantEmail(),
                        request.getTenantName(),
                        request.getTenantPhone()
                );
                paramsBuilder.setCustomer(customer.getStripeCustomerId());

                // Use tenant email for receipt if not provided
                if (request.getReceiptEmail() == null && customer.getEmail() != null) {
                    paramsBuilder.setReceiptEmail(customer.getEmail());
                }
            }

            // Add optional parameters
            if (request.getDescription() != null) {
                paramsBuilder.setDescription(request.getDescription());
            }

            if (request.getReceiptEmail() != null) {
                paramsBuilder.setReceiptEmail(request.getReceiptEmail());
            }

            PaymentIntent intent = PaymentIntent.create(paramsBuilder.build());

            log.info("Created ACH PaymentIntent: {} for amount: {}", intent.getId(), request.getAmount());

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
