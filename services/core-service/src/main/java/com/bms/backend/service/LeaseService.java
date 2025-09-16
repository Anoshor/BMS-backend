package com.bms.backend.service;

import com.bms.backend.dto.request.LeaseUpdateRequest;
import com.bms.backend.dto.response.LeaseDetailsDto;
import com.bms.backend.dto.response.LeaseListingDto;
import com.bms.backend.entity.TenantPropertyConnection;
import com.bms.backend.entity.User;
import com.bms.backend.enums.UserRole;
import com.bms.backend.repository.TenantPropertyConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

        // Reactivate lease
        connection.setIsActive(true);
        return connectionRepository.save(connection);
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