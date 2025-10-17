package com.bms.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Lease payment details for making a payment")
public class LeasePaymentDetailsDto {

    @Schema(description = "Connection/Lease UUID", example = "04fc37d0-e819-4488-9849-4f237f9b45c1")
    private UUID connectionId;

    @Schema(description = "Formatted Lease ID", example = "LEASE-2025-6E99")
    private String leaseId;

    @Schema(description = "Tenant UUID", example = "04fc37d0-e819-4488-9849-4f237f9b45c1")
    private UUID tenantId;

    @Schema(description = "Tenant Name (Paid To)", example = "Sudarshana V Sharma")
    private String tenantName;

    @Schema(description = "Tenant Email", example = "sudarshana@example.com")
    private String tenantEmail;

    @Schema(description = "Tenant Phone", example = "+1234567890")
    private String tenantPhone;

    @Schema(description = "Property Name", example = "Sunset Apartments")
    private String propertyName;

    @Schema(description = "Monthly Rent Amount", example = "600.00")
    private BigDecimal rentAmount;

    @Schema(description = "Late Payment Charges", example = "60.00")
    private BigDecimal latePaymentCharges;

    @Schema(description = "Total Payable Amount", example = "660.00")
    private BigDecimal totalPayableAmount;

    @Schema(description = "Security Deposit", example = "1200.00")
    private BigDecimal securityDeposit;

    @Schema(description = "Payment Frequency", example = "Monthly")
    private String paymentFrequency;

    // Constructors
    public LeasePaymentDetailsDto() {}

    // Getters and Setters
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

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
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
