package com.bms.payment.controller;

import com.bms.payment.dto.PaymentIntentRequest;
import com.bms.payment.dto.PaymentIntentResponse;
import com.bms.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment", description = "Payment processing APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/stripe/publishable-key")
    @Operation(summary = "Get Stripe publishable key", description = "Returns the Stripe publishable key for frontend initialization")
    public ResponseEntity<Map<String, String>> getPublishableKey() {
        Map<String, String> response = new HashMap<>();
        response.put("publishableKey", paymentService.getPublishableKey());
        log.info("Publishable key requested");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-card-intent")
    @Operation(
            summary = "Create card payment intent",
            description = """
                    Creates a Stripe PaymentIntent for card payments.

                    **SECURE FLOW (Recommended for lease payments):**
                    ```json
                    {
                      "leaseId": "04fc37d0-e819-4488-9849-4f237f9b45c1"
                    }
                    ```
                    Amount is fetched server-side from core-service - client cannot tamper!

                    **Test Card Numbers:**
                    - Success: 4242 4242 4242 4242
                    - Decline: 4000 0000 0000 0002
                    - Requires authentication: 4000 0025 0000 3155

                    **Manual payment (non-lease):**
                    ```json
                    {
                      "amount": 5000,
                      "currency": "usd",
                      "description": "Test payment"
                    }
                    ```
                    """
    )
    public ResponseEntity<PaymentIntentResponse> createCardPaymentIntent(
            @Valid @RequestBody PaymentIntentRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("Creating card payment intent for lease: {} or manual amount: {}",
            request.getLeaseId(), request.getAmount());
        PaymentIntentResponse response = paymentService.createCardPaymentIntent(request, authHeader);

        if (response.getError() != null) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-ach-intent")
    @Operation(
            summary = "Create ACH payment intent",
            description = """
                    Creates a Stripe PaymentIntent for ACH/bank account payments.

                    **SECURE FLOW (Recommended for lease payments):**
                    ```json
                    {
                      "leaseId": "04fc37d0-e819-4488-9849-4f237f9b45c1"
                    }
                    ```
                    Amount is fetched server-side from core-service - client cannot tamper!

                    **Test Bank Account:**
                    - Routing: 110000000
                    - Account: 000123456789

                    **Manual payment (non-lease):**
                    ```json
                    {
                      "amount": 5000,
                      "currency": "usd",
                      "description": "Test ACH payment"
                    }
                    ```
                    """
    )
    public ResponseEntity<PaymentIntentResponse> createACHPaymentIntent(
            @Valid @RequestBody PaymentIntentRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("Creating ACH payment intent for lease: {} or manual amount: {}",
            request.getLeaseId(), request.getAmount());
        PaymentIntentResponse response = paymentService.createACHPaymentIntent(request, authHeader);

        if (response.getError() != null) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentIntentId}")
    @Operation(summary = "Get payment intent", description = "Retrieves a PaymentIntent by ID")
    public ResponseEntity<PaymentIntentResponse> getPaymentIntent(
            @PathVariable String paymentIntentId) {
        log.info("Retrieving payment intent: {}", paymentIntentId);
        PaymentIntentResponse response = paymentService.getPaymentIntent(paymentIntentId);

        if (response.getError() != null) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{paymentIntentId}/cancel")
    @Operation(summary = "Cancel payment intent", description = "Cancels a PaymentIntent")
    public ResponseEntity<PaymentIntentResponse> cancelPaymentIntent(
            @PathVariable String paymentIntentId) {
        log.info("Canceling payment intent: {}", paymentIntentId);
        PaymentIntentResponse response = paymentService.cancelPaymentIntent(paymentIntentId);

        if (response.getError() != null) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
