package com.bms.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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

    // Constructors
    public PaymentIntentResponse() {}

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String clientSecret;
        private String paymentIntentId;
        private String status;
        private Long amount;
        private String currency;
        private String error;
        private String errorMessage;

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder paymentIntentId(String paymentIntentId) {
            this.paymentIntentId = paymentIntentId;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder amount(Long amount) {
            this.amount = amount;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public PaymentIntentResponse build() {
            PaymentIntentResponse response = new PaymentIntentResponse();
            response.clientSecret = this.clientSecret;
            response.paymentIntentId = this.paymentIntentId;
            response.status = this.status;
            response.amount = this.amount;
            response.currency = this.currency;
            response.error = this.error;
            response.errorMessage = this.errorMessage;
            return response;
        }
    }

    // Getters and Setters
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
