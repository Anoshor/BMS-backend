package com.bms.payment.dto;

import java.math.BigDecimal;

/**
 * DTO to receive lease payment details from core-service
 */
public class LeasePaymentDetailsDto {
    private String connectionId;
    private String leaseId;
    private String tenantId;
    private String tenantName;
    private String tenantEmail;
    private String tenantPhone;
    private String propertyName;
    private BigDecimal rentAmount;
    private BigDecimal latePaymentCharges;
    private BigDecimal totalPayableAmount;
    private BigDecimal securityDeposit;
    private String paymentFrequency;

    // Constructors
    public LeasePaymentDetailsDto() {}

    // Getters and Setters
    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(String leaseId) {
        this.leaseId = leaseId;
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

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public BigDecimal getRentAmount() {
        return rentAmount;
    }

    public void setRentAmount(BigDecimal rentAmount) {
        this.rentAmount = rentAmount;
    }

    public BigDecimal getLatePaymentCharges() {
        return latePaymentCharges;
    }

    public void setLatePaymentCharges(BigDecimal latePaymentCharges) {
        this.latePaymentCharges = latePaymentCharges;
    }

    public BigDecimal getTotalPayableAmount() {
        return totalPayableAmount;
    }

    public void setTotalPayableAmount(BigDecimal totalPayableAmount) {
        this.totalPayableAmount = totalPayableAmount;
    }

    public BigDecimal getSecurityDeposit() {
        return securityDeposit;
    }

    public void setSecurityDeposit(BigDecimal securityDeposit) {
        this.securityDeposit = securityDeposit;
    }

    public String getPaymentFrequency() {
        return paymentFrequency;
    }

    public void setPaymentFrequency(String paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
    }
}
