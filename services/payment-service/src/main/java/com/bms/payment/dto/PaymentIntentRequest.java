package com.bms.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment Intent Request")
public class PaymentIntentRequest {

    @Schema(description = "Lease ID - when provided, amount is fetched from core-service (SECURE)", example = "04fc37d0-e819-4488-9849-4f237f9b45c1")
    private String leaseId;

    @Min(value = 50, message = "Minimum amount is 50 cents")
    @Schema(description = "Amount in cents (e.g., 5000 = $50.00) - Only for non-lease payments. Ignored if leaseId is provided.", example = "5000")
    private Long amount;

    @Schema(description = "Currency code", example = "usd", defaultValue = "usd")
    private String currency = "usd";

    @Schema(description = "Payment method ID (optional for initial intent creation)", example = "pm_card_visa")
    private String paymentMethodId;

    @Schema(description = "Whether to save payment method for future use", example = "false", defaultValue = "false")
    private boolean savePaymentMethod;

    @Schema(description = "Allowed payment method types", example = "[\"card\"]")
    private String[] paymentMethodTypes;

    @Schema(description = "Tenant ID from core-service (will auto-create/retrieve Stripe customer)", example = "04fc37d0-e819-4488-9849-4f237f9b45c1")
    private String tenantId;

    @Schema(description = "Tenant name (required if tenantId provided and customer doesn't exist)", example = "John Doe")
    private String tenantName;

    @Schema(description = "Tenant email (required if tenantId provided and customer doesn't exist)", example = "john.doe@example.com")
    private String tenantEmail;

    @Schema(description = "Tenant phone (optional)", example = "+1234567890")
    private String tenantPhone;

    @Schema(description = "Payment description", example = "Monthly rent payment")
    private String description;

    @Schema(description = "Email to send receipt to (optional, uses tenantEmail if not provided)", example = "customer@example.com")
    private String receiptEmail;
}
