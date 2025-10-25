package com.bms.payment.controller;

import com.bms.payment.service.WebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@Tag(name = "Webhook", description = "Stripe webhook handling APIs")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Autowired
    private WebhookService webhookService;

    @PostMapping("/stripe")
    @Operation(summary = "Handle Stripe webhook", description = "Processes Stripe webhook events")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("========================================");
        log.info("🔔 WEBHOOK RECEIVED from Stripe");
        log.info("========================================");

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            log.info("✅ Webhook signature verified successfully");
            log.info("📋 Event Type: {}", event.getType());
            log.info("📋 Event ID: {}", event.getId());
            log.info("📋 Created: {}", java.time.Instant.ofEpochSecond(event.getCreated()));
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Webhook signature verification failed");
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error processing webhook");
        }


        // Handle the event
        switch (event.getType()) {
            case "payment_intent.succeeded":
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (paymentIntent != null) {
                    log.info("Processing payment_intent.succeeded for PaymentIntent: {}", paymentIntent.getId());
                    handlePaymentSuccess(paymentIntent);
                } else {
                    log.warn("Received payment_intent.succeeded event but PaymentIntent is null");
                }
                break;

            case "payment_intent.payment_failed":
                PaymentIntent failedIntent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (failedIntent != null) {
                    log.info("Processing payment_intent.payment_failed for PaymentIntent: {}", failedIntent.getId());
                    handlePaymentFailure(failedIntent);
                } else {
                    log.warn("Received payment_intent.payment_failed event but PaymentIntent is null");
                }
                break;

            case "payment_intent.canceled":
                PaymentIntent canceledIntent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (canceledIntent != null) {
                    log.info("Processing payment_intent.canceled for PaymentIntent: {}", canceledIntent.getId());
                    handlePaymentCanceled(canceledIntent);
                } else {
                    log.warn("Received payment_intent.canceled event but PaymentIntent is null");
                }
                break;

            case "payment_intent.processing":
                PaymentIntent processingIntent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (processingIntent != null) {
                    log.info("Processing payment_intent.processing for PaymentIntent: {}", processingIntent.getId());
                    handlePaymentProcessing(processingIntent);
                } else {
                    log.warn("Received payment_intent.processing event but PaymentIntent is null");
                }
                break;

            case "payment_intent.requires_action":
                PaymentIntent requiresActionIntent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (requiresActionIntent != null) {
                    log.info("Processing payment_intent.requires_action for PaymentIntent: {}", requiresActionIntent.getId());
                    handlePaymentRequiresAction(requiresActionIntent);
                } else {
                    log.warn("Received payment_intent.requires_action event but PaymentIntent is null");
                }
                break;

            default:
                log.info("Received unhandled webhook event type: {}", event.getType());
        }

        log.info("Webhook processed successfully - Event type: {}", event.getType());
        return ResponseEntity.ok("Success");
    }

    private void handlePaymentSuccess(PaymentIntent paymentIntent) {
        log.info("💰 PAYMENT SUCCEEDED");
        log.info("PaymentIntent ID: {}", paymentIntent.getId());
        log.info("Amount: {} {}", paymentIntent.getAmount(), paymentIntent.getCurrency().toUpperCase());
        log.info("Customer: {}", paymentIntent.getCustomer());
        log.info("Metadata: {}", paymentIntent.getMetadata());

        // Record payment in core-service
        webhookService.recordPaymentTransaction(paymentIntent, "PAID");
    }

    private void handlePaymentFailure(PaymentIntent paymentIntent) {
        String failureReason = paymentIntent.getLastPaymentError() != null
                ? paymentIntent.getLastPaymentError().getMessage()
                : "Unknown reason";

        log.error("❌ PAYMENT FAILED");
        log.error("PaymentIntent ID: {}", paymentIntent.getId());
        log.error("Failure Reason: {}", failureReason);
        log.error("Metadata: {}", paymentIntent.getMetadata());

        // Record failed payment in core-service
        webhookService.recordPaymentTransaction(paymentIntent, "FAILED");
    }

    private void handlePaymentCanceled(PaymentIntent paymentIntent) {
        log.info("🚫 PAYMENT CANCELED");
        log.info("PaymentIntent ID: {}", paymentIntent.getId());
        log.info("Metadata: {}", paymentIntent.getMetadata());

        // Record canceled payment in core-service
        webhookService.recordPaymentTransaction(paymentIntent, "CANCELED");
    }

    private void handlePaymentProcessing(PaymentIntent paymentIntent) {
        log.info("⏳ PAYMENT PROCESSING");
        log.info("PaymentIntent ID: {}", paymentIntent.getId());
        log.info("Amount: {} {}", paymentIntent.getAmount(), paymentIntent.getCurrency().toUpperCase());
        log.info("Metadata: {}", paymentIntent.getMetadata());

        // Record processing payment in core-service
        webhookService.recordPaymentTransaction(paymentIntent, "PROCESSING");
    }

    private void handlePaymentRequiresAction(PaymentIntent paymentIntent) {
        log.warn("Payment requires action for PaymentIntent: {}", paymentIntent.getId());
        // TODO: Implement requires action logic here:
        // - Notify customer that additional action is required
        // - Send email with instructions
        // - Update UI to prompt for action
    }
}
