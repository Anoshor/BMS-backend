package com.bms.backend.service;

import com.bms.backend.dto.request.LeaseUpdateRequest;
import com.bms.backend.dto.response.LeaseDetailsDto;
import com.bms.backend.dto.response.LeaseListingDto;
import com.bms.backend.dto.response.LeasePaymentDetailsDto;
import com.bms.backend.dto.response.LeasePaymentScheduleDto;
import com.bms.backend.dto.response.LeasePaymentScheduleResponse;
import com.bms.backend.dto.response.LeasePaymentSummaryDto;
import com.bms.backend.entity.TenantPropertyConnection;
import com.bms.backend.entity.User;
import com.bms.backend.enums.UserRole;
import com.bms.backend.repository.TenantPropertyConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaseService {

    @Autowired
    private TenantPropertyConnectionRepository connectionRepository;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private com.bms.backend.repository.ApartmentRepository apartmentRepository;

    @Autowired
    private com.bms.backend.repository.PaymentTransactionRepository paymentTransactionRepository;

    public List<LeaseListingDto> getAllLeases(User user, String status, String propertyName, String tenantName) {
        validateManagerAccess(user);

        List<TenantPropertyConnection> connections = connectionRepository.findByManagerOrderByCreatedAtDesc(user);

        return connections.stream()
                .filter(connection -> filterByStatus(connection, status))
                .filter(connection -> filterByPropertyName(connection, propertyName))
                .filter(connection -> filterByTenantName(connection, tenantName))
                .map(LeaseListingDto::new)
                .collect(Collectors.toList());
    }

    public LeaseDetailsDto getLeaseById(User user, UUID id) {
        return tenantService.getLeaseDetails(user, id);
    }

    public TenantPropertyConnection updateLease(User user, UUID id, LeaseUpdateRequest request) {
        validateManagerAccess(user);

        TenantPropertyConnection connection = connectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lease not found"));

        // Verify manager owns this lease
        if (!connection.getManager().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to update this lease");
        }

        // Validate business rules
        validateLeaseUpdate(request);

        // Update lease details
        connection.setStartDate(request.getStartDate());
        connection.setEndDate(request.getEndDate());
        connection.setMonthlyRent(request.getMonthlyRent());
        connection.setSecurityDeposit(request.getSecurityDeposit());
        connection.setPaymentFrequency(request.getPaymentFrequency());
        connection.setNotes(request.getNotes());

        return connectionRepository.save(connection);
    }

    public void terminateLease(User user, UUID id) {
        validateManagerAccess(user);

        TenantPropertyConnection connection = connectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lease not found"));

        // Verify manager owns this lease
        if (!connection.getManager().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to terminate this lease");
        }

        // Update apartment to vacant and clear tenant info
        if (connection.getApartment() != null) {
            com.bms.backend.entity.Apartment apartment = connection.getApartment();
            apartment.setOccupancyStatus("VACANT");
            apartment.setTenantName(null);
            apartment.setTenantEmail(null);
            apartment.setTenantPhone(null);
            apartmentRepository.save(apartment);
        }

        // Soft delete - set isActive to false
        connection.setIsActive(false);
        connectionRepository.save(connection);
    }

    public List<LeaseListingDto> searchLeases(User user, String searchText) {
        validateManagerAccess(user);

        List<TenantPropertyConnection> connections = connectionRepository.findByManagerAndSearchText(user, searchText);

        return connections.stream()
                .map(LeaseListingDto::new)
                .collect(Collectors.toList());
    }

    public List<LeaseListingDto> getActiveLeases(User user) {
        validateManagerAccess(user);

        LocalDate now = LocalDate.now();
        List<TenantPropertyConnection> connections = connectionRepository.findByManagerAndIsActiveOrderByCreatedAtDesc(user, true);

        return connections.stream()
                .filter(connection -> {
                    LocalDate startDate = connection.getStartDate();
                    LocalDate endDate = connection.getEndDate();
                    return !now.isBefore(startDate) && !now.isAfter(endDate);
                })
                .map(LeaseListingDto::new)
                .collect(Collectors.toList());
    }

    public List<LeaseListingDto> getExpiredLeases(User user) {
        validateManagerAccess(user);

        LocalDate now = LocalDate.now();
        List<TenantPropertyConnection> connections = connectionRepository.findByManagerOrderByCreatedAtDesc(user);

        return connections.stream()
                .filter(connection -> {
                    LocalDate endDate = connection.getEndDate();
                    return now.isAfter(endDate);
                })
                .map(LeaseListingDto::new)
                .collect(Collectors.toList());
    }

    public List<LeaseListingDto> getUpcomingLeases(User user) {
        validateManagerAccess(user);

        LocalDate now = LocalDate.now();
        List<TenantPropertyConnection> connections = connectionRepository.findByManagerAndIsActiveOrderByCreatedAtDesc(user, true);

        return connections.stream()
                .filter(connection -> {
                    LocalDate startDate = connection.getStartDate();
                    return now.isBefore(startDate);
                })
                .map(LeaseListingDto::new)
                .collect(Collectors.toList());
    }

    public List<LeaseListingDto> getUpcomingExpirations(User user, int months) {
        validateManagerAccess(user);

        // Get current date at start of day for consistent comparison
        LocalDate today = LocalDate.now();
        LocalDate expirationThreshold = today.plusMonths(months);

        // Get all active leases for this manager
        List<TenantPropertyConnection> connections = connectionRepository.findByManagerAndIsActiveOrderByCreatedAtDesc(user, true);

        return connections.stream()
                .filter(connection -> {
                    LocalDate endDate = connection.getEndDate();
                    if (endDate == null) {
                        return false;
                    }

                    // Lease is expiring if end date is:
                    // 1. Today or in the future (not already expired)
                    // 2. Within the next N months (on or before threshold)
                    boolean isNotExpired = endDate.isEqual(today) || endDate.isAfter(today);
                    boolean isWithinThreshold = endDate.isBefore(expirationThreshold) || endDate.isEqual(expirationThreshold);

                    return isNotExpired && isWithinThreshold;
                })
                .sorted((c1, c2) -> c1.getEndDate().compareTo(c2.getEndDate())) // Sort by end date (soonest first)
                .map(LeaseListingDto::new)
                .collect(Collectors.toList());
    }

    public TenantPropertyConnection reactivateLease(User user, UUID id) {
        validateManagerAccess(user);

        TenantPropertyConnection connection = connectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lease not found"));

        // Verify manager owns this lease
        if (!connection.getManager().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to reactivate this lease");
        }

        if (connection.getIsActive()) {
            throw new IllegalArgumentException("Lease is already active");
        }

        // Update apartment to occupied and restore tenant info
        if (connection.getApartment() != null && connection.getTenant() != null) {
            com.bms.backend.entity.Apartment apartment = connection.getApartment();

            // Check if apartment is vacant before reactivating
            if (!"VACANT".equalsIgnoreCase(apartment.getOccupancyStatus())) {
                throw new IllegalArgumentException("Cannot reactivate lease - apartment is not vacant");
            }

            User tenant = connection.getTenant();
            apartment.setOccupancyStatus("OCCUPIED");
            apartment.setTenantName(tenant.getFirstName() + " " + tenant.getLastName());
            apartment.setTenantEmail(tenant.getEmail());
            apartment.setTenantPhone(tenant.getPhone());
            apartmentRepository.save(apartment);
        }

        // Reactivate lease
        connection.setIsActive(true);
        return connectionRepository.save(connection);
    }

    /**
     * Get payment details for a lease by its ID
     * This method is accessible to both tenants and managers
     */
    public LeasePaymentDetailsDto getLeasePaymentDetails(User user, UUID leaseId) {
        TenantPropertyConnection connection = connectionRepository.findById(leaseId)
                .orElseThrow(() -> new IllegalArgumentException("Lease not found"));

        // Verify user has access to this lease (either tenant or manager)
        boolean isTenant = connection.getTenant().getId().equals(user.getId());
        boolean isManager = connection.getManager().getId().equals(user.getId());

        if (!isTenant && !isManager) {
            throw new IllegalArgumentException("You don't have permission to view this lease");
        }

        User tenant = connection.getTenant();
        BigDecimal rentAmount = BigDecimal.valueOf(connection.getMonthlyRent());

        // Calculate late payment charges (example: 10% if payment is overdue)
        // This is a simplified calculation - you may want to implement more complex logic
        BigDecimal latePaymentCharges = calculateLatePaymentCharges(connection);

        BigDecimal totalPayableAmount = rentAmount.add(latePaymentCharges);

        // Generate formatted lease ID (e.g., LEASE-2025-6E99)
        String formattedLeaseId = generateFormattedLeaseId(connection);

        LeasePaymentDetailsDto dto = new LeasePaymentDetailsDto();
        dto.setConnectionId(connection.getId());
        dto.setLeaseId(formattedLeaseId);
        dto.setTenantId(tenant.getId());
        dto.setTenantName(tenant.getFirstName() + " " + tenant.getLastName());
        dto.setTenantEmail(tenant.getEmail());
        dto.setTenantPhone(tenant.getPhone());
        dto.setPropertyName(connection.getPropertyName());
        dto.setRentAmount(rentAmount);
        dto.setLatePaymentCharges(latePaymentCharges);
        dto.setTotalPayableAmount(totalPayableAmount);
        dto.setSecurityDeposit(connection.getSecurityDeposit() != null ?
                BigDecimal.valueOf(connection.getSecurityDeposit()) : BigDecimal.ZERO);
        dto.setPaymentFrequency(connection.getPaymentFrequency());

        return dto;
    }

    /**
     * Calculate late payment charges based on lease terms
     * This is a simplified implementation - customize based on your business rules
     */
    private BigDecimal calculateLatePaymentCharges(TenantPropertyConnection connection) {
        // TODO: Implement actual late payment calculation logic
        // For now, we'll calculate 10% late fee if current date is past the payment due date
        // You should replace this with your actual business logic

        LocalDate today = LocalDate.now();
        LocalDate leaseStart = connection.getStartDate();

        // Simplified logic: Calculate if payment is late (assuming payment due on 1st of each month)
        // This is just an example - you'll want to implement proper payment tracking
        long monthsSinceStart = ChronoUnit.MONTHS.between(leaseStart, today);

        // For demo purposes, if we're past the 5th of the month, add 10% late fee
        if (today.getDayOfMonth() > 5 && monthsSinceStart >= 0) {
            return BigDecimal.valueOf(connection.getMonthlyRent())
                    .multiply(BigDecimal.valueOf(0.10))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    /**
     * Generate formatted lease ID from connection
     */
    private String generateFormattedLeaseId(TenantPropertyConnection connection) {
        // Extract last 4 characters of UUID
        String uuidStr = connection.getId().toString().replace("-", "").toUpperCase();
        String suffix = uuidStr.substring(uuidStr.length() - 4);

        // Get year from start date
        int year = connection.getStartDate().getYear();

        return String.format("LEASE-%d-%s", year, suffix);
    }

    /**
     * Get paginated payment schedule for a lease
     * Returns default of current month + next 2 months if no parameters provided
     */
    public LeasePaymentScheduleResponse getLeasePaymentSchedule(User user, UUID leaseId,
                                                                 String startMonth, String endMonth, Integer limit) {
        TenantPropertyConnection connection = connectionRepository.findById(leaseId)
                .orElseThrow(() -> new IllegalArgumentException("Lease not found"));

        // Verify user has access to this lease
        validateLeaseAccess(user, connection);

        // Parse dates or use defaults
        YearMonth start;
        YearMonth end;

        if (startMonth != null) {
            start = YearMonth.parse(startMonth);
        } else {
            // Default: current month
            start = YearMonth.now();
        }

        if (endMonth != null) {
            end = YearMonth.parse(endMonth);
        } else if (limit != null && limit > 0) {
            // Use limit to determine end month
            end = start.plusMonths(limit - 1);
        } else {
            // Default: current month + next 2 months (3 months total)
            end = start.plusMonths(2);
        }

        // Calculate total months in lease
        LocalDate leaseStart = connection.getStartDate();
        LocalDate leaseEnd = connection.getEndDate();
        int totalMonths = (int) ChronoUnit.MONTHS.between(
            YearMonth.from(leaseStart),
            YearMonth.from(leaseEnd)
        ) + 1;

        // Generate schedule for the requested period
        List<LeasePaymentScheduleDto> schedule = generatePaymentSchedule(connection, start, end);

        // Build response
        LeasePaymentScheduleResponse response = new LeasePaymentScheduleResponse();
        response.setLeaseId(connection.getId());
        response.setPropertyName(connection.getPropertyName());
        response.setTenantName(connection.getTenant().getFirstName() + " " + connection.getTenant().getLastName());
        response.setSchedule(schedule);
        response.setTotalMonths(totalMonths);
        response.setCurrentPage(0); // Can be enhanced for actual pagination
        response.setItemsReturned(schedule.size());

        return response;
    }

    /**
     * Get payment summary for a lease
     */
    public LeasePaymentSummaryDto getLeasePaymentSummary(User user, UUID leaseId) {
        TenantPropertyConnection connection = connectionRepository.findById(leaseId)
                .orElseThrow(() -> new IllegalArgumentException("Lease not found"));

        // Verify user has access to this lease
        validateLeaseAccess(user, connection);

        LocalDate today = LocalDate.now();
        LocalDate leaseStart = connection.getStartDate();
        LocalDate leaseEnd = connection.getEndDate();

        BigDecimal monthlyRent = BigDecimal.valueOf(connection.getMonthlyRent());

        // Calculate total months in lease
        int totalMonths = (int) ChronoUnit.MONTHS.between(
            YearMonth.from(leaseStart),
            YearMonth.from(leaseEnd)
        ) + 1;

        // Fetch ALL payment transactions for this connection to calculate real counts
        var allPayments = paymentTransactionRepository.findByConnectionOrderByCreatedAtDesc(connection);

        YearMonth leaseStartMonth = YearMonth.from(leaseStart);
        YearMonth currentYearMonth = YearMonth.from(today);
        YearMonth endPeriod = currentYearMonth.plusMonths(2);

        // If no payment records exist, create payment records from lease start to current + 2 months
        // This ensures ALL payments (including overdue ones) are created
        if (allPayments.isEmpty() || allPayments.stream().noneMatch(p -> p.getDueDate() != null)) {
            System.out.println("🔄 No payment records found for lease " + leaseId + " - auto-creating from lease start");

            // Generate payment schedule which will auto-create records from lease start
            generatePaymentSchedule(connection, leaseStartMonth, endPeriod);

            // Fetch again after creating
            allPayments = paymentTransactionRepository.findByConnectionOrderByCreatedAtDesc(connection);
        } else {
            // Check if we have payment records for all months from lease start to current month
            // If any are missing (especially past months), create them
            boolean hasMissingPastMonths = false;
            YearMonth checkMonth = leaseStartMonth;

            while (!checkMonth.isAfter(currentYearMonth)) {
                final YearMonth monthToCheck = checkMonth;
                boolean hasRecordForMonth = allPayments.stream()
                    .anyMatch(p -> {
                        if (p.getDueDate() != null) {
                            LocalDate dueDate = LocalDate.ofInstant(p.getDueDate(), java.time.ZoneId.of("UTC"));
                            return YearMonth.from(dueDate).equals(monthToCheck);
                        }
                        return false;
                    });

                if (!hasRecordForMonth) {
                    hasMissingPastMonths = true;
                    break;
                }
                checkMonth = checkMonth.plusMonths(1);
            }

            if (hasMissingPastMonths) {
                System.out.println("🔄 Missing payment records detected for lease " + leaseId + " - creating missing months from lease start");

                // Generate payment schedule which will auto-create missing records
                generatePaymentSchedule(connection, leaseStartMonth, endPeriod);

                // Fetch again after creating
                allPayments = paymentTransactionRepository.findByConnectionOrderByCreatedAtDesc(connection);
            }
        }

        // Count ACTUAL overdue and pending payments from database
        int overdueCount = 0;
        int upcomingCount = 0;
        BigDecimal totalPending = BigDecimal.ZERO;
        BigDecimal overdueAmount = BigDecimal.ZERO;
        LocalDate nextDueDate = null;

        YearMonth currentMonth = YearMonth.from(today);

        for (var payment : allPayments) {
            if (payment.getDueDate() == null) continue;

            LocalDate paymentDueDate = LocalDate.ofInstant(payment.getDueDate(), java.time.ZoneId.of("UTC"));
            YearMonth paymentMonth = YearMonth.from(paymentDueDate);

            // Skip PAID payments
            if (payment.getStatus() == com.bms.backend.entity.PaymentTransaction.PaymentStatus.PAID) {
                continue;
            }

            // Determine if payment is overdue
            boolean isOverdue = false;
            if (paymentMonth.isBefore(currentMonth)) {
                isOverdue = true;
            } else if (paymentMonth.equals(currentMonth) && today.getDayOfMonth() > 5) {
                isOverdue = true;
            }

            if (isOverdue) {
                overdueCount++;
                BigDecimal lateCharges = monthlyRent.multiply(BigDecimal.valueOf(0.10))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
                BigDecimal paymentWithLateCharges = payment.getAmount().add(lateCharges);
                totalPending = totalPending.add(paymentWithLateCharges);
                overdueAmount = overdueAmount.add(paymentWithLateCharges);
            } else if (!paymentMonth.isBefore(currentMonth)) {
                // Future or current month not yet overdue
                upcomingCount++;
                if (nextDueDate == null || paymentDueDate.isBefore(nextDueDate)) {
                    nextDueDate = paymentDueDate;
                }
                // Only add current month to total pending if not overdue
                if (paymentMonth.equals(currentMonth)) {
                    totalPending = totalPending.add(payment.getAmount());
                }
            }
        }

        // Build summary
        LeasePaymentSummaryDto summary = new LeasePaymentSummaryDto();
        summary.setLeaseId(connection.getId());
        summary.setFormattedLeaseId(generateFormattedLeaseId(connection));
        summary.setPropertyName(connection.getPropertyName());
        summary.setTenantName(connection.getTenant().getFirstName() + " " + connection.getTenant().getLastName());
        summary.setMonthlyRent(monthlyRent);
        summary.setTotalPending(totalPending);
        summary.setOverdueAmount(overdueAmount);
        summary.setNextDueDate(nextDueDate);
        summary.setUpcomingPaymentsCount(upcomingCount);
        summary.setOverduePaymentsCount(overdueCount);
        summary.setLeaseStartDate(leaseStart);
        summary.setLeaseEndDate(leaseEnd);
        summary.setTotalMonths(totalMonths);

        // Set unit information
        if (connection.getApartment() != null) {
            summary.setUnitId(connection.getApartment().getId());
            summary.setUnitNumber(connection.getApartment().getUnitNumber());
        }

        return summary;
    }

    /**
     * Generate payment schedule items for a given date range
     * NOW CHECKS ACTUAL PAYMENT RECORDS to determine status
     */
    public List<LeasePaymentScheduleDto> generatePaymentSchedule(TenantPropertyConnection connection,
                                                                   YearMonth start, YearMonth end) {
        List<LeasePaymentScheduleDto> schedule = new ArrayList<>();

        YearMonth leaseStartMonth = YearMonth.from(connection.getStartDate());
        YearMonth leaseEndMonth = YearMonth.from(connection.getEndDate());
        BigDecimal monthlyRent = BigDecimal.valueOf(connection.getMonthlyRent());
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);

        // Ensure start and end are within lease period
        if (start.isBefore(leaseStartMonth)) {
            start = leaseStartMonth;
        }
        if (end.isAfter(leaseEndMonth)) {
            end = leaseEndMonth;
        }

        // Fetch all payment transactions for this connection
        var paymentTransactions = paymentTransactionRepository.findByConnectionOrderByCreatedAtDesc(connection);

        YearMonth current = start;
        while (!current.isAfter(end)) {
            LeasePaymentScheduleDto item = new LeasePaymentScheduleDto();

            // Format month as YYYY-MM
            String monthStr = current.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            item.setMonth(monthStr);

            // Due date is 1st of the month
            LocalDate dueDate = current.atDay(1);
            item.setDueDate(dueDate);

            item.setRentAmount(monthlyRent);

            // Find payment record for this month (to get ID and status)
            final YearMonth monthToCheck = current; // Capture for lambda
            var matchingPayment = paymentTransactions.stream()
                .filter(payment -> {
                    // Check if payment's due date matches this month
                    Instant dueDateInstant = payment.getDueDate();
                    if (dueDateInstant != null) {
                        LocalDate paymentDueDate = LocalDate.ofInstant(dueDateInstant, java.time.ZoneId.of("UTC"));
                        YearMonth paymentMonth = YearMonth.from(paymentDueDate);
                        return paymentMonth.equals(monthToCheck);
                    }
                    return false;
                })
                .findFirst();

            // If no payment record exists for this month, create one
            if (matchingPayment.isEmpty()) {
                com.bms.backend.entity.PaymentTransaction newPayment = new com.bms.backend.entity.PaymentTransaction();
                newPayment.setTenant(connection.getTenant());
                newPayment.setConnection(connection);
                newPayment.setAmount(monthlyRent);
                newPayment.setCurrency("USD");
                newPayment.setStatus(com.bms.backend.entity.PaymentTransaction.PaymentStatus.PENDING);
                newPayment.setDescription("Monthly rent for " + connection.getPropertyName() + " - " +
                    current.getMonth() + " " + current.getYear());

                // Set due date to 1st of the month in UTC
                Instant dueDateInstant = dueDate.atStartOfDay(java.time.ZoneId.of("UTC")).toInstant();
                newPayment.setDueDate(dueDateInstant);

                // Save the new payment record
                newPayment = paymentTransactionRepository.save(newPayment);
                matchingPayment = java.util.Optional.of(newPayment);

                System.out.println("✅ Created new payment record for month: " + monthStr + ", ID: " + newPayment.getId());
            }

            // Set payment transaction ID
            item.setPaymentTransactionId(matchingPayment.get().getId());

            // Check if paid
            boolean isPaid = matchingPayment.isPresent() &&
                matchingPayment.get().getStatus() == com.bms.backend.entity.PaymentTransaction.PaymentStatus.PAID;

            // Determine status and calculate late charges
            String status;
            BigDecimal lateCharges = BigDecimal.ZERO;

            if (isPaid) {
                // Payment has been made
                status = "PAID";
            } else if (current.isBefore(currentMonth)) {
                // Past month - mark as overdue
                status = "OVERDUE";
                lateCharges = monthlyRent.multiply(BigDecimal.valueOf(0.10))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            } else if (current.equals(currentMonth)) {
                // Current month
                if (today.getDayOfMonth() > 5) {
                    status = "OVERDUE";
                    lateCharges = monthlyRent.multiply(BigDecimal.valueOf(0.10))
                        .setScale(2, BigDecimal.ROUND_HALF_UP);
                } else {
                    status = "PENDING";
                }
            } else {
                // Future month
                status = "PENDING";
            }

            item.setLateCharges(lateCharges);
            item.setTotalAmount(monthlyRent.add(lateCharges));
            item.setStatus(status);

            schedule.add(item);
            current = current.plusMonths(1);
        }

        return schedule;
    }

    /**
     * Validate that user has access to view this lease
     */
    private void validateLeaseAccess(User user, TenantPropertyConnection connection) {
        boolean isTenant = connection.getTenant().getId().equals(user.getId());
        boolean isManager = connection.getManager().getId().equals(user.getId());

        if (!isTenant && !isManager) {
            throw new IllegalArgumentException("You don't have permission to view this lease");
        }
    }

    // Helper methods
    private void validateManagerAccess(User user) {
        if (user.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only property managers can perform lease operations");
        }
    }

    private void validateLeaseUpdate(LeaseUpdateRequest request) {
        // Validate dates
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        // Validate rent amount
        if (request.getMonthlyRent() <= 0) {
            throw new IllegalArgumentException("Monthly rent must be positive");
        }

        // Validate security deposit
        if (request.getSecurityDeposit() != null && request.getSecurityDeposit() < 0) {
            throw new IllegalArgumentException("Security deposit cannot be negative");
        }
    }

    private boolean filterByStatus(TenantPropertyConnection connection, String status) {
        if (status == null || status.trim().isEmpty()) {
            return true;
        }

        String leaseStatus = determineLeaseStatus(connection);
        return leaseStatus.equalsIgnoreCase(status);
    }

    private boolean filterByPropertyName(TenantPropertyConnection connection, String propertyName) {
        if (propertyName == null || propertyName.trim().isEmpty()) {
            return true;
        }

        return connection.getPropertyName().toLowerCase().contains(propertyName.toLowerCase());
    }

    private boolean filterByTenantName(TenantPropertyConnection connection, String tenantName) {
        if (tenantName == null || tenantName.trim().isEmpty()) {
            return true;
        }

        if (connection.getTenant() == null) {
            return false;
        }

        String fullName = connection.getTenant().getFirstName() + " " + connection.getTenant().getLastName();
        return fullName.toLowerCase().contains(tenantName.toLowerCase());
    }

    private String determineLeaseStatus(TenantPropertyConnection connection) {
        if (!connection.getIsActive()) {
            return "TERMINATED";
        }

        LocalDate now = LocalDate.now();
        LocalDate startDate = connection.getStartDate();
        LocalDate endDate = connection.getEndDate();

        if (now.isBefore(startDate)) {
            return "UPCOMING";
        } else if (now.isAfter(endDate)) {
            return "EXPIRED";
        } else {
            return "ACTIVE";
        }
    }
}