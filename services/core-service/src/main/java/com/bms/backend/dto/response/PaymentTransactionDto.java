package com.bms.backend.dto.response;

import com.bms.backend.entity.PaymentTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PaymentTransactionDto {

    private UUID id;
    private UUID tenantId;
    private UUID connectionId;
    private String leaseId;
    private String propertyName;
    private String propertyImage;
    private String unitNumber;
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
    private Instant dueDate;
    private Instant createdAt;
    private Instant updatedAt;

    // UI-friendly fields
    private String statusLabel;
    private String amountFormatted;
    private boolean isOverdue;

    // Constructors
    public PaymentTransactionDto() {}

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(String leaseId) {
        this.leaseId = leaseId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyImage() {
        return propertyImage;
    }

    public void setPropertyImage(String propertyImage) {
        this.propertyImage = propertyImage;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
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

    public Instant getDueDate() {
        return dueDate;
    }

    public void setDueDate(Instant dueDate) {
        this.dueDate = dueDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getAmountFormatted() {
        return amountFormatted;
    }

    public void setAmountFormatted(String amountFormatted) {
        this.amountFormatted = amountFormatted;
    }

    public boolean isOverdue() {
        return isOverdue;
    }

    public void setOverdue(boolean overdue) {
        isOverdue = overdue;
    }
}
