package com.bms.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Monthly payment schedule item for a lease")
public class LeasePaymentScheduleDto {

    @Schema(description = "Payment month (YYYY-MM)", example = "2024-10")
    private String month;

    @Schema(description = "Payment due date", example = "2024-10-01")
    private LocalDate dueDate;

    @Schema(description = "Monthly rent amount", example = "600.00")
    private BigDecimal rentAmount;

    @Schema(description = "Late charges (if applicable)", example = "0.00")
    private BigDecimal lateCharges;

    @Schema(description = "Total amount due for this month", example = "600.00")
    private BigDecimal totalAmount;

    @Schema(description = "Payment status", example = "PENDING", allowableValues = {"PENDING", "PAID", "OVERDUE"})
    private String status;

    // Constructors
    public LeasePaymentScheduleDto() {}

    public LeasePaymentScheduleDto(String month, LocalDate dueDate, BigDecimal rentAmount,
                                   BigDecimal lateCharges, BigDecimal totalAmount, String status) {
        this.month = month;
        this.dueDate = dueDate;
        this.rentAmount = rentAmount;
        this.lateCharges = lateCharges;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // Getters and Setters
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
}
