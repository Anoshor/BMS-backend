package com.bms.backend.dto.request;

import com.bms.backend.entity.PaymentTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class RecordPaymentRequest {

    private UUID paymentTransactionId; // Optional - if provided, update this specific payment record
    private UUID tenantId;
    private UUID connectionId; // lease/connection ID
    private String stripePaymentIntentId;
    private String stripePaymentMethodId;
    private BigDecimal amount;
    private String currency;
    private PaymentTransaction.PaymentStatus status;
    private PaymentTransaction.PaymentMethod paymentMethod;
    private String description;
    private String receiptEmail;
    private String receiptUrl;
    private String failureReason;
    private Instant paymentDate;

    // Constructors
    public RecordPaymentRequest() {}

    // Getters and Setters
    public UUID getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(UUID paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(UUID connectionId) {
        this.connectionId = connectionId;
    }

    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }

    public void setStripePaymentIntentId(String stripePaymentIntentId) {
        this.stripePaymentIntentId = stripePaymentIntentId;
    }

    public String getStripePaymentMethodId() {
        return stripePaymentMethodId;
    }

    public void setStripePaymentMethodId(String stripePaymentMethodId) {
        this.stripePaymentMethodId = stripePaymentMethodId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentTransaction.PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentTransaction.PaymentStatus status) {
        this.status = status;
    }

    public PaymentTransaction.PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentTransaction.PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
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

    public String getReceiptUrl() {
        return receiptUrl;
    }

    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Instant getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Instant paymentDate) {
        this.paymentDate = paymentDate;
    }
}
