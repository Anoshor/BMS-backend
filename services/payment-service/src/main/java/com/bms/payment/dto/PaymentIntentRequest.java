package com.bms.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "Payment Intent Request")
public class PaymentIntentRequest {

    @Schema(description = "Lease ID - when provided, amount is fetched from core-service (SECURE)", example = "04fc37d0-e819-4488-9849-4f237f9b45c1")
    private String leaseId;

    @Schema(description = "Payment Transaction ID - specific payment record to update", example = "550e8400-e29b-41d4-a716-446655440000")
    private String paymentTransactionId;

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

    // Constructors
    public PaymentIntentRequest() {}

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String leaseId;
        private String paymentTransactionId;
        private Long amount;
        private String currency = "usd";
        private String paymentMethodId;
        private boolean savePaymentMethod;
        private String[] paymentMethodTypes;
        private String tenantId;
        private String tenantName;
        private String tenantEmail;
        private String tenantPhone;
        private String description;
        private String receiptEmail;

        public Builder leaseId(String leaseId) {
            this.leaseId = leaseId;
            return this;
        }

        public Builder paymentTransactionId(String paymentTransactionId) {
            this.paymentTransactionId = paymentTransactionId;
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

        public Builder paymentMethodId(String paymentMethodId) {
            this.paymentMethodId = paymentMethodId;
            return this;
        }

        public Builder savePaymentMethod(boolean savePaymentMethod) {
            this.savePaymentMethod = savePaymentMethod;
            return this;
        }

        public Builder paymentMethodTypes(String[] paymentMethodTypes) {
            this.paymentMethodTypes = paymentMethodTypes;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder tenantName(String tenantName) {
            this.tenantName = tenantName;
            return this;
        }

        public Builder tenantEmail(String tenantEmail) {
            this.tenantEmail = tenantEmail;
            return this;
        }

        public Builder tenantPhone(String tenantPhone) {
            this.tenantPhone = tenantPhone;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder receiptEmail(String receiptEmail) {
            this.receiptEmail = receiptEmail;
            return this;
        }

        public PaymentIntentRequest build() {
            PaymentIntentRequest request = new PaymentIntentRequest();
            request.leaseId = this.leaseId;
            request.paymentTransactionId = this.paymentTransactionId;
            request.amount = this.amount;
            request.currency = this.currency;
            request.paymentMethodId = this.paymentMethodId;
            request.savePaymentMethod = this.savePaymentMethod;
            request.paymentMethodTypes = this.paymentMethodTypes;
            request.tenantId = this.tenantId;
            request.tenantName = this.tenantName;
            request.tenantEmail = this.tenantEmail;
            request.tenantPhone = this.tenantPhone;
            request.description = this.description;
            request.receiptEmail = this.receiptEmail;
            return request;
        }
    }

    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    // Getters and Setters
    public String getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(String leaseId) {
        this.leaseId = leaseId;
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

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public boolean isSavePaymentMethod() {
        return savePaymentMethod;
    }

    public void setSavePaymentMethod(boolean savePaymentMethod) {
        this.savePaymentMethod = savePaymentMethod;
    }

    public String[] getPaymentMethodTypes() {
        return paymentMethodTypes;
    }

    public void setPaymentMethodTypes(String[] paymentMethodTypes) {
        this.paymentMethodTypes = paymentMethodTypes;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantEmail() {
        return tenantEmail;
    }

    public void setTenantEmail(String tenantEmail) {
        this.tenantEmail = tenantEmail;
    }

    public String getTenantPhone() {
        return tenantPhone;
    }

    public void setTenantPhone(String tenantPhone) {
        this.tenantPhone = tenantPhone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReceiptEmail() {
        return receiptEmail;
    }

    public void setReceiptEmail(String receiptEmail) {
        this.receiptEmail = receiptEmail;
    }
}
