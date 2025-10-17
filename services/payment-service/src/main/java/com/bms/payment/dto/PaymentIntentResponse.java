package com.bms.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment Intent Response")
public class PaymentIntentResponse {

    @Schema(description = "Client secret for confirming payment on frontend", example = "pi_123_secret_456")
    private String clientSecret;

    @Schema(description = "Payment Intent ID", example = "pi_3AbCdEfGhIjKlMnO")
    private String paymentIntentId;

    @Schema(description = "Payment status", example = "requires_payment_method", allowableValues = {"requires_payment_method", "requires_confirmation", "requires_action", "processing", "succeeded", "canceled"})
    private String status;

    @Schema(description = "Amount in cents", example = "5000")
    private Long amount;

    @Schema(description = "Currency code", example = "usd")
    private String currency;

    @Schema(description = "Error code if payment failed", example = "card_declined")
    private String error;

    @Schema(description = "Error message if payment failed", example = "Your card was declined")
    private String errorMessage;
}
