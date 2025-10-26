package com.bms.backend.service;

import com.bms.backend.dto.request.PaymentSearchRequest;
import com.bms.backend.dto.request.RecordPaymentRequest;
import com.bms.backend.dto.response.PaymentTransactionDto;
import com.bms.backend.entity.Apartment;
import com.bms.backend.entity.PaymentTransaction;
import com.bms.backend.entity.PropertyImage;
import com.bms.backend.entity.TenantPropertyConnection;
import com.bms.backend.entity.User;
import com.bms.backend.enums.UserRole;
import com.bms.backend.repository.PaymentTransactionRepository;
import com.bms.backend.repository.TenantPropertyConnectionRepository;
import com.bms.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentTransactionService {

    @Autowired
    private PaymentTransactionRepository paymentRepository;

    @Autowired
    private TenantPropertyConnectionRepository connectionRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Auto-generate pending rent payments when lease is created
     * Generates payment records for the duration of the lease based on payment frequency
     */
    public void generateRentPaymentsForLease(TenantPropertyConnection connection) {
        LocalDate startDate = connection.getStartDate();
        LocalDate endDate = connection.getEndDate();
        Double monthlyRent = connection.getMonthlyRent();
        String paymentFrequency = connection.getPaymentFrequency();

        // For now, only support MONTHLY frequency
        if (!"MONTHLY".equalsIgnoreCase(paymentFrequency)) {
            return; // Skip for other frequencies
        }

        // Generate one payment per month
        LocalDate currentDate = startDate.withDayOfMonth(1); // Start from first of the month
        int count = 0;
        int maxPayments = 24; // Safety limit: max 2 years of payments

        while (!currentDate.isAfter(endDate) && count < maxPayments) {
            // Create PENDING payment record
            PaymentTransaction payment = new PaymentTransaction();
            payment.setTenant(connection.getTenant());
            payment.setConnection(connection);
            payment.setAmount(BigDecimal.valueOf(monthlyRent));
            payment.setCurrency("USD");
            payment.setStatus(PaymentTransaction.PaymentStatus.PENDING);
            payment.setDescription("Monthly rent for " + connection.getPropertyName() + " - " +
                currentDate.getMonth() + " " + currentDate.getYear());

            // Set due date to 1st of the month
            Instant dueDate = currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            payment.setDueDate(dueDate);

            paymentRepository.save(payment);

            // Move to next month
            currentDate = currentDate.plusMonths(1);
            count++;
        }
    }

    /**
     * Record a payment transaction from payment service webhook
     */
    public PaymentTransaction recordPayment(RecordPaymentRequest request) {
        User tenant = userRepository.findById(request.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        TenantPropertyConnection connection = connectionRepository.findById(request.getConnectionId())
                .orElseThrow(() -> new IllegalArgumentException("Lease/Connection not found"));

        PaymentTransaction payment;

        // PRIORITY 1: If paymentTransactionId is provided, update that specific record
        if (request.getPaymentTransactionId() != null) {
            payment = paymentRepository.findById(request.getPaymentTransactionId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found with ID: " + request.getPaymentTransactionId()));

            // Update the existing payment record
            payment.setStripePaymentIntentId(request.getStripePaymentIntentId());
            payment.setStatus(request.getStatus());
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setStripePaymentMethodId(request.getStripePaymentMethodId());
            payment.setReceiptUrl(request.getReceiptUrl());
            payment.setReceiptEmail(request.getReceiptEmail());
            payment.setFailureReason(request.getFailureReason());
            payment.setPaymentDate(request.getPaymentDate());

            System.out.println("✅ Updated specific payment record with ID: " + request.getPaymentTransactionId());
        }
        // PRIORITY 2: Check if payment already exists by Stripe Payment Intent ID
        else {
            Optional<PaymentTransaction> existingPayment =
                    paymentRepository.findByStripePaymentIntentId(request.getStripePaymentIntentId());

            if (existingPayment.isPresent()) {
                // Update existing payment
                payment = existingPayment.get();
                payment.setStatus(request.getStatus());
                payment.setPaymentMethod(request.getPaymentMethod());
                payment.setStripePaymentMethodId(request.getStripePaymentMethodId());
                payment.setReceiptUrl(request.getReceiptUrl());
                payment.setReceiptEmail(request.getReceiptEmail());
                payment.setFailureReason(request.getFailureReason());
                payment.setPaymentDate(request.getPaymentDate());
            } else {
                // Create new payment
                payment = new PaymentTransaction();
                payment.setTenant(tenant);
                payment.setConnection(connection);
                payment.setStripePaymentIntentId(request.getStripePaymentIntentId());
                payment.setStripePaymentMethodId(request.getStripePaymentMethodId());
                payment.setAmount(request.getAmount());
                payment.setCurrency(request.getCurrency());
                payment.setStatus(request.getStatus());
                payment.setPaymentMethod(request.getPaymentMethod());
                payment.setDescription(request.getDescription());
                payment.setReceiptEmail(request.getReceiptEmail());
                payment.setReceiptUrl(request.getReceiptUrl());
                payment.setFailureReason(request.getFailureReason());
                payment.setPaymentDate(request.getPaymentDate());

                // Set due date (if not provided, use first of next month)
                if (request.getPaymentDate() != null) {
                    payment.setDueDate(request.getPaymentDate());
                } else {
                    LocalDate nextMonth = LocalDate.now().plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
                    payment.setDueDate(nextMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
                }
            }
        }

        return paymentRepository.save(payment);
    }

    /**
     * Get all payments for a tenant with filters
     */
    public List<PaymentTransactionDto> getTenantPayments(User user, PaymentSearchRequest searchRequest) {
        validateTenantAccess(user);

        List<PaymentTransaction> payments;
        Instant startDate = searchRequest != null ? searchRequest.getStartDate() : null;
        Instant endDate = searchRequest != null ? searchRequest.getEndDate() : null;
        String status = searchRequest != null ? searchRequest.getStatus() : "ALL";

        if (status == null || "ALL".equalsIgnoreCase(status)) {
            payments = paymentRepository.findByTenantWithDateRange(user, startDate, endDate);
        } else if ("PAID".equalsIgnoreCase(status)) {
            payments = paymentRepository.findPaidByTenantWithDateRange(user, startDate, endDate);
        } else if ("PENDING".equalsIgnoreCase(status)) {
            payments = paymentRepository.findPendingByTenantWithDateRange(user, startDate, endDate);
        } else if ("OVERDUE".equalsIgnoreCase(status)) {
            payments = paymentRepository.findOverdueByTenantWithDateRange(user, Instant.now(), startDate, endDate);
        } else {
            payments = paymentRepository.findByTenantWithDateRange(user, startDate, endDate);
        }

        return payments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get paid payments for a tenant
     */
    public List<PaymentTransactionDto> getPaidPayments(User user, Instant startDate, Instant endDate) {
        validateTenantAccess(user);
        List<PaymentTransaction> payments = paymentRepository.findPaidByTenantWithDateRange(user, startDate, endDate);
        return payments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get pending payments for a tenant
     */
    public List<PaymentTransactionDto> getPendingPayments(User user, Instant startDate, Instant endDate) {
        validateTenantAccess(user);
        List<PaymentTransaction> payments = paymentRepository.findPendingByTenantWithDateRange(user, startDate, endDate);
        return payments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get overdue payments for a tenant
     */
    public List<PaymentTransactionDto> getOverduePayments(User user, Instant startDate, Instant endDate) {
        validateTenantAccess(user);
        List<PaymentTransaction> payments = paymentRepository.findOverdueByTenantWithDateRange(
                user, Instant.now(), startDate, endDate);
        return payments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get payment transaction history for a specific lease/connection
     */
    public List<PaymentTransactionDto> getLeasePaymentHistory(User user, UUID connectionId) {
        validateTenantAccess(user);

        TenantPropertyConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Lease/Connection not found"));

        // Verify tenant owns this connection
        if (!connection.getTenant().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to view this lease's payment history");
        }

        List<PaymentTransaction> payments = paymentRepository.findByConnectionOrderByCreatedAtDesc(connection);
        return payments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a single payment transaction
     */
    public PaymentTransactionDto getPaymentById(User user, UUID paymentId) {
        validateTenantAccess(user);

        PaymentTransaction payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        // Verify tenant owns this payment
        if (!payment.getTenant().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to view this payment");
        }

        return convertToDto(payment);
    }

    // Helper Methods

    private void validateTenantAccess(User user) {
        if (user.getRole() != UserRole.TENANT) {
            throw new IllegalArgumentException("Only tenants can access payment transactions");
        }
    }

    private PaymentTransactionDto convertToDto(PaymentTransaction payment) {
        TenantPropertyConnection connection = payment.getConnection();
        Apartment apartment = connection.getApartment();

        // Generate lease ID
        String leaseId = "LEASE-" + LocalDate.now().getYear() + "-" +
                connection.getId().toString().substring(0, 4).toUpperCase();

        // Get property image if available
        String propertyImage = null;
        if (apartment != null && apartment.getProperty() != null) {
            List<PropertyImage> images = apartment.getProperty().getImages();
            if (images != null && !images.isEmpty()) {
                propertyImage = images.get(0).getImageUrl();
            }
        }

        // Format amount
        String amountFormatted = formatCurrency(payment.getAmount(), payment.getCurrency());

        // Determine if overdue
        boolean isOverdue = payment.getDueDate() != null &&
                payment.getDueDate().isBefore(Instant.now()) &&
                (payment.getStatus() == PaymentTransaction.PaymentStatus.PENDING ||
                 payment.getStatus() == PaymentTransaction.PaymentStatus.FAILED);

        // Get status label
        String statusLabel = getStatusLabel(payment.getStatus(), isOverdue);

        PaymentTransactionDto dto = new PaymentTransactionDto();
        dto.setId(payment.getId());
        dto.setTenantId(payment.getTenant().getId());
        dto.setConnectionId(connection.getId());
        dto.setLeaseId(leaseId);
        dto.setPropertyName(connection.getPropertyName());
        dto.setPropertyImage(propertyImage);
        dto.setUnitNumber(apartment != null ? apartment.getUnitNumber() : null);
        dto.setStripePaymentIntentId(payment.getStripePaymentIntentId());
        dto.setStripePaymentMethodId(payment.getStripePaymentMethodId());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setStatus(payment.getStatus());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setDescription(payment.getDescription());
        dto.setReceiptEmail(payment.getReceiptEmail());
        dto.setReceiptUrl(payment.getReceiptUrl());
        dto.setFailureReason(payment.getFailureReason());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setDueDate(payment.getDueDate());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        dto.setStatusLabel(statusLabel);
        dto.setAmountFormatted(amountFormatted);
        dto.setOverdue(isOverdue);
        return dto;
    }

    private String formatCurrency(BigDecimal amount, String currencyCode) {
        try {
            Currency currency = Currency.getInstance(currencyCode.toUpperCase());
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            formatter.setCurrency(currency);
            return formatter.format(amount);
        } catch (Exception e) {
            return "$" + amount.toString();
        }
    }

    private String getStatusLabel(PaymentTransaction.PaymentStatus status, boolean isOverdue) {
        if (isOverdue) {
            return "Overdue";
        }
        switch (status) {
            case PAID:
                return "Paid";
            case PENDING:
                return "Pending";
            case PROCESSING:
                return "Processing";
            case FAILED:
                return "Failed";
            case CANCELED:
                return "Canceled";
            case REFUNDED:
                return "Refunded";
            default:
                return status.toString();
        }
    }
}
