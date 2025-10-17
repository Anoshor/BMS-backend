package com.bms.payment.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO to receive lease payment details from core-service
 */
@Data
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
}
