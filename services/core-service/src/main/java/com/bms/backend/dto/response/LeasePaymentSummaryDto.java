package com.bms.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Summary of lease payment information")
public class LeasePaymentSummaryDto {

    @Schema(description = "Lease/Connection ID", example = "04fc37d0-e819-4488-9849-4f237f9b45c1")
    private UUID leaseId;

    @Schema(description = "Formatted Lease ID", example = "LEASE-2025-6E99")
    private String formattedLeaseId;

    @Schema(description = "Property name", example = "Sunset Apartments")
    private String propertyName;

    @Schema(description = "Unit/Apartment ID", example = "04fc37d0-e819-4488-9849-4f237f9b45c1")
    private UUID unitId;

    @Schema(description = "Unit number", example = "3B")
    private String unitNumber;

    @Schema(description = "Tenant name", example = "John Doe")
    private String tenantName;

    @Schema(description = "Monthly rent amount", example = "600.00")
    private BigDecimal monthlyRent;

    @Schema(description = "Total pending amount (current + overdue)", example = "1260.00")
    private BigDecimal totalPending;

    @Schema(description = "Total overdue amount (with late charges)", example = "660.00")
    private BigDecimal overdueAmount;

    @Schema(description = "Next payment due date", example = "2024-11-01")
    private LocalDate nextDueDate;

    @Schema(description = "Number of upcoming payments in the lease", example = "18")
    private int upcomingPaymentsCount;

    @Schema(description = "Number of overdue payments", example = "2")
    private int overduePaymentsCount;

    @Schema(description = "Lease start date", example = "2024-01-01")
    private LocalDate leaseStartDate;

    @Schema(description = "Lease end date", example = "2025-12-31")
    private LocalDate leaseEndDate;

    @Schema(description = "Total months in lease", example = "24")
    private int totalMonths;

    // Constructors
    public LeasePaymentSummaryDto() {}

    // Getters and Setters
    public UUID getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(UUID leaseId) {
        this.leaseId = leaseId;
    }

    public String getFormattedLeaseId() {
        return formattedLeaseId;
    }

    public void setFormattedLeaseId(String formattedLeaseId) {
        this.formattedLeaseId = formattedLeaseId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }

    public void setMonthlyRent(BigDecimal monthlyRent) {
        this.monthlyRent = monthlyRent;
    }

    public BigDecimal getTotalPending() {
        return totalPending;
    }

    public void setTotalPending(BigDecimal totalPending) {
        this.totalPending = totalPending;
    }

    public BigDecimal getOverdueAmount() {
        return overdueAmount;
    }

    public void setOverdueAmount(BigDecimal overdueAmount) {
        this.overdueAmount = overdueAmount;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public int getUpcomingPaymentsCount() {
        return upcomingPaymentsCount;
    }

    public void setUpcomingPaymentsCount(int upcomingPaymentsCount) {
        this.upcomingPaymentsCount = upcomingPaymentsCount;
    }

    public int getOverduePaymentsCount() {
        return overduePaymentsCount;
    }

    public void setOverduePaymentsCount(int overduePaymentsCount) {
        this.overduePaymentsCount = overduePaymentsCount;
    }

    public LocalDate getLeaseStartDate() {
        return leaseStartDate;
    }

    public void setLeaseStartDate(LocalDate leaseStartDate) {
        this.leaseStartDate = leaseStartDate;
    }

    public LocalDate getLeaseEndDate() {
        return leaseEndDate;
    }

    public void setLeaseEndDate(LocalDate leaseEndDate) {
        this.leaseEndDate = leaseEndDate;
    }

    public int getTotalMonths() {
        return totalMonths;
    }

    public void setTotalMonths(int totalMonths) {
        this.totalMonths = totalMonths;
    }

    public UUID getUnitId() {
        return unitId;
    }

    public void setUnitId(UUID unitId) {
        this.unitId = unitId;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }
}
