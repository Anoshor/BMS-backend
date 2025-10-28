package com.bms.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Most urgent payment across all tenant's leases")
public class UrgentPaymentDto {

    @Schema(description = "Payment transaction ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID paymentTransactionId;

    @Schema(description = "Lease/Connection ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID leaseId;

    @Schema(description = "Connection ID (same as leaseId, for backward compatibility)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID connectionId;

    @Schema(description = "Property name", example = "Sunset Apartments")
    private String propertyName;

    @Schema(description = "Unit number", example = "Unit Lease-001 • Unit 3B")
    private String unitDescription;

    @Schema(description = "Payment month (YYYY-MM)", example = "2025-01")
    private String month;

    @Schema(description = "Due date", example = "2025-01-01")
    private LocalDate dueDate;

    @Schema(description = "Monthly rent amount", example = "1200.00")
    private BigDecimal rentAmount;

    @Schema(description = "Late charges if applicable", example = "120.00")
    private BigDecimal lateCharges;

    @Schema(description = "Total amount due", example = "1320.00")
    private BigDecimal totalAmount;

    @Schema(description = "Payment status", example = "OVERDUE", allowableValues = {"PENDING", "OVERDUE", "PAID"})
    private String status;

    @Schema(description = "Total active leases count", example = "2")
    private int totalActiveLeases;

    // Constructors
    public UrgentPaymentDto() {}

    // Getters and Setters
    public UUID getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(UUID paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    public UUID getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(UUID leaseId) {
        this.leaseId = leaseId;
    }

    public UUID getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(UUID connectionId) {
        this.connectionId = connectionId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getUnitDescription() {
        return unitDescription;
    }

    public void setUnitDescription(String unitDescription) {
        this.unitDescription = unitDescription;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getRentAmount() {
        return rentAmount;
    }

    public void setRentAmount(BigDecimal rentAmount) {
        this.rentAmount = rentAmount;
    }

    public BigDecimal getLateCharges() {
        return lateCharges;
    }

    public void setLateCharges(BigDecimal lateCharges) {
        this.lateCharges = lateCharges;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalActiveLeases() {
        return totalActiveLeases;
    }

    public void setTotalActiveLeases(int totalActiveLeases) {
        this.totalActiveLeases = totalActiveLeases;
    }
}
