package com.bms.payment.service;

import com.stripe.model.PaymentIntent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service

public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${core.service.url:http://localhost:8080}")
    private String coreServiceUrl;

    /**
     * Record payment transaction in core-service
     */
    public void recordPaymentTransaction(PaymentIntent paymentIntent, String status) {
        try {
            log.info("Recording payment transaction - PaymentIntent: {}, Status: {}", paymentIntent.getId(), status);

            // Extract metadata from payment intent
            Map<String, String> metadata = paymentIntent.getMetadata();
            if (metadata == null || metadata.isEmpty()) {
                log.warn("PaymentIntent {} has no metadata - skipping recording", paymentIntent.getId());
                return;
            }

            String tenantId = metadata.get("tenantId");
            String connectionId = metadata.get("connectionId");

            if (tenantId == null || connectionId == null) {
                log.warn("PaymentIntent {} missing required metadata - tenantId: {}, connectionId: {}",
                    paymentIntent.getId(), tenantId, connectionId);
                return;
            }

            // Convert amount from cents to dollars
            BigDecimal amount = BigDecimal.valueOf(paymentIntent.getAmount())
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

            // Get payment method type - we'll fetch from payment method ID
            String paymentMethodType = "CARD"; // Default to card

            // Get receipt URL - will be null for now, Stripe provides this after charge completes
            String receiptUrl = null;

            // Get failure reason if failed
            String failureReason = null;
            if ("FAILED".equals(status) && paymentIntent.getLastPaymentError() != null) {
                failureReason = paymentIntent.getLastPaymentError().getMessage();
            }

            // Build request payload
            Map<String, Object> recordPaymentRequest = new HashMap<>();
            recordPaymentRequest.put("tenantId", UUID.fromString(tenantId));
            recordPaymentRequest.put("connectionId", UUID.fromString(connectionId));
            recordPaymentRequest.put("stripePaymentIntentId", paymentIntent.getId());
            recordPaymentRequest.put("stripePaymentMethodId", paymentIntent.getPaymentMethod());
            recordPaymentRequest.put("amount", amount);
            recordPaymentRequest.put("currency", paymentIntent.getCurrency());
            recordPaymentRequest.put("status", status);
            recordPaymentRequest.put("paymentMethod", mapPaymentMethodType(paymentMethodType));
            recordPaymentRequest.put("description", paymentIntent.getDescription());
            recordPaymentRequest.put("receiptEmail", paymentIntent.getReceiptEmail());
            recordPaymentRequest.put("receiptUrl", receiptUrl);
            recordPaymentRequest.put("failureReason", failureReason);

            // Set payment date if succeeded
            if ("PAID".equals(status)) {
                recordPaymentRequest.put("paymentDate", Instant.now());
            }

            // Make request to core-service
            String url = coreServiceUrl + "/api/v1/payments/record";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(recordPaymentRequest, headers);

            log.info("Sending payment transaction to core-service: {}", url);
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            log.info("Successfully recorded payment transaction for PaymentIntent: {} - Response: {}",
                paymentIntent.getId(), response);

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("HTTP Client Error while recording payment transaction for PaymentIntent: {} - Status: {}, Response: {}",
                paymentIntent.getId(), e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("HTTP Server Error while recording payment transaction for PaymentIntent: {} - Status: {}, Response: {}",
                paymentIntent.getId(), e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("Network Error - Cannot reach core-service at {} for PaymentIntent: {} - Error: {}",
                coreServiceUrl, paymentIntent.getId(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while recording payment transaction for PaymentIntent: {} - Error: {}",
                paymentIntent.getId(), e.getMessage(), e);
        }
    }

    /**
     * Map Stripe payment method type to our enum
     */
    private String mapPaymentMethodType(String stripeType) {
        if (stripeType == null) {
            return "CARD"; // default
        }

        switch (stripeType.toLowerCase()) {
            case "card":
                return "CARD";
            case "us_bank_account":
            case "ach_debit":
                return "ACH";
            case "bank_transfer":
                return "BANK_TRANSFER";
            default:
                return "CARD";
        }
    }
}
