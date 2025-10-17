package com.bms.payment.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@Slf4j
@Tag(name = "Webhook", description = "Stripe webhook handling APIs")
public class WebhookController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/stripe")
    @Operation(summary = "Handle Stripe webhook", description = "Processes Stripe webhook events")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook signature verification failed");
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error processing webhook");
        }

        log.info("Received webhook event: {}", event.getType());

        // Handle the event
        switch (event.getType()) {
            case "payment_intent.succeeded":
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (paymentIntent != null) {
                    handlePaymentSuccess(paymentIntent);
                }
                break;

            case "payment_intent.payment_failed":
                PaymentIntent failedIntent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (failedIntent != null) {
                    handlePaymentFailure(failedIntent);
                }
                break;

            case "payment_intent.canceled":
                PaymentIntent canceledIntent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (canceledIntent != null) {
                    handlePaymentCanceled(canceledIntent);
                }
                break;

            case "payment_intent.processing":
                PaymentIntent processingIntent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (processingIntent != null) {
                    handlePaymentProcessing(processingIntent);
                }
                break;

            case "payment_intent.requires_action":
                PaymentIntent requiresActionIntent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (requiresActionIntent != null) {
                    handlePaymentRequiresAction(requiresActionIntent);
                }
                break;

            default:
                log.info("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Success");
    }

    private void handlePaymentSuccess(PaymentIntent paymentIntent) {
        log.info("Payment succeeded for PaymentIntent: {}", paymentIntent.getId());
        log.info("Amount: {} {}", paymentIntent.getAmount(), paymentIntent.getCurrency());
        log.info("Payment Method: {}", paymentIntent.getPaymentMethod());

        // TODO: Implement your business logic here:
        // - Update order status in database
        // - Send confirmation email to customer
        // - Trigger order fulfillment process
        // - Update inventory
        // - Generate invoice
        // - Notify relevant services (e.g., shipping service)
    }

    private void handlePaymentFailure(PaymentIntent paymentIntent) {
        log.error("Payment failed for PaymentIntent: {}", paymentIntent.getId());
        log.error("Amount: {} {}", paymentIntent.getAmount(), paymentIntent.getCurrency());

        String failureReason = paymentIntent.getLastPaymentError() != null
                ? paymentIntent.getLastPaymentError().getMessage()
                : "Unknown reason";
        log.error("Failure reason: {}", failureReason);

        // TODO: Implement your failure handling logic here:
        // - Update order status to 'payment_failed'
        // - Send notification to customer about payment failure
        // - Log failure for analytics
        // - Retry logic if appropriate
        // - Alert admin if needed
    }

    private void handlePaymentCanceled(PaymentIntent paymentIntent) {
        log.info("Payment canceled for PaymentIntent: {}", paymentIntent.getId());
        log.info("Amount: {} {}", paymentIntent.getAmount(), paymentIntent.getCurrency());

        // TODO: Implement your cancellation logic here:
        // - Update order status to 'canceled'
        // - Release reserved inventory
        // - Notify customer about cancellation
        // - Clean up any pending processes
    }

    private void handlePaymentProcessing(PaymentIntent paymentIntent) {
        log.info("Payment processing for PaymentIntent: {}", paymentIntent.getId());
        log.info("Amount: {} {}", paymentIntent.getAmount(), paymentIntent.getCurrency());

        // TODO: Implement processing state logic here:
        // - Update order status to 'processing'
        // - Show customer that payment is being processed
        // - Set timeout for processing (if needed)
    }

    private void handlePaymentRequiresAction(PaymentIntent paymentIntent) {
        log.info("Payment requires action for PaymentIntent: {}", paymentIntent.getId());
        log.info("Amount: {} {}", paymentIntent.getAmount(), paymentIntent.getCurrency());

        // TODO: Implement requires action logic here:
        // - Notify customer that additional action is required
        // - Send email with instructions
        // - Update UI to prompt for action
    }
}
