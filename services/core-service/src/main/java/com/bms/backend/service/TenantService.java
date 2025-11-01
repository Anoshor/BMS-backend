package com.bms.backend.service;

import com.bms.backend.dto.request.ConnectTenantRequest;
import com.bms.backend.dto.response.LeaseDetailsDto;
import com.bms.backend.dto.response.TenantConnectionDto;
import com.bms.backend.dto.response.TenantDetailsDto;
import com.bms.backend.dto.response.TenantPropertyDto;
import com.bms.backend.entity.*;
import com.bms.backend.enums.UserRole;
import com.bms.backend.repository.*;

import java.time.Period;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TenantService {

    @Autowired
    private TenantPropertyConnectionRepository connectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;
    
    @Autowired
    private ApartmentRepository apartmentRepository;

    @Autowired
    private MaintenanceRequestRepository maintenanceRequestRepository;

    @Autowired
    private PaymentTransactionService paymentTransactionService;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    public TenantPropertyConnection connectTenantToProperty(User manager, ConnectTenantRequest request) {
        // Validate manager
        if (manager.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only managers can connect tenants to properties");
        }
        
        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Find tenant by email
        User tenant = userRepository.findByEmail(request.getTenantEmail())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with email: " + request.getTenantEmail()));

        if (tenant.getRole() != UserRole.TENANT) {
            throw new IllegalArgumentException("User is not a tenant");
        }

        // Find and validate apartment
        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found with ID: " + request.getApartmentId()));

        // Verify apartment belongs to this manager
        if (!apartment.getProperty().getManager().getId().equals(manager.getId())) {
            throw new IllegalArgumentException("You don't have permission to manage this apartment");
        }

        // Check if apartment is already occupied
        if ("OCCUPIED".equalsIgnoreCase(apartment.getOccupancyStatus())) {
            throw new IllegalArgumentException("Apartment is already occupied");
        }

        // Note: We allow a tenant to have multiple units in the same property
        // The validation above ensures each specific apartment/unit can only have one tenant

        // Create connection
        String propertyName = apartment.getProperty().getName();
        TenantPropertyConnection connection = new TenantPropertyConnection(
                tenant, manager, propertyName,
                request.getStartDate(), request.getEndDate(), request.getMonthlyRent()
        );
        connection.setApartment(apartment);  // Link to specific apartment
        connection.setSecurityDeposit(request.getSecurityDeposit());
        connection.setPaymentFrequency(request.getPaymentFrequency());
        connection.setNotes(request.getNotes());

        // Update apartment occupancy and tenant info
        apartment.setOccupancyStatus("OCCUPIED");
        apartment.setTenantName(tenant.getFirstName() + " " + tenant.getLastName());
        apartment.setTenantEmail(tenant.getEmail());
        apartment.setTenantPhone(tenant.getPhone());
        apartmentRepository.save(apartment);

        // Save the connection
        TenantPropertyConnection savedConnection = connectionRepository.save(connection);

        // Auto-generate pending rent payments for the lease duration
        paymentTransactionService.generateRentPaymentsForLease(savedConnection);

        return savedConnection;
    }

    public List<TenantPropertyConnection> searchTenants(User manager, String searchText) {
        if (manager.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only managers can search tenants");
        }

        if (searchText == null || searchText.trim().isEmpty()) {
            return connectionRepository.findByManagerAndIsActive(manager, true);
        }

        return connectionRepository.findByManagerAndSearchText(manager, searchText.trim());
    }

    public List<TenantPropertyConnection> getTenantProperties(User tenant) {
        if (tenant.getRole() != UserRole.TENANT) {
            throw new IllegalArgumentException("Only tenants can view their properties");
        }

        return connectionRepository.findByTenantAndIsActive(tenant, true);
    }

    public List<TenantPropertyDto> getTenantPropertiesEnhanced(User tenant) {
        if (tenant.getRole() != UserRole.TENANT) {
            throw new IllegalArgumentException("Only tenants can view their properties");
        }

        List<TenantPropertyConnection> connections = connectionRepository.findByTenantAndIsActive(tenant, true);
        
        return connections.stream()
                .map(connection -> {
                    TenantPropertyDto dto = new TenantPropertyDto(connection);

                    // Get apartment details directly from the connection
                    if (connection.getApartment() != null) {
                        Apartment apartment = connection.getApartment();
                        dto.setApartmentId(apartment.getId());
                        dto.setUnitId(apartment.getUnitNumber());
                        dto.setUnitNumber(apartment.getUnitNumber());

                        if (apartment.getProperty() != null) {
                            dto.setPropertyId(apartment.getProperty().getId());
                            dto.setPropertyAddress(apartment.getProperty().getAddress());
                        }
                    } else {
                        // Fallback for old connections without apartment reference
                        // Find apartment details based on tenant email and property name
                        List<Apartment> apartments = apartmentRepository.findByTenantEmail(tenant.getEmail());
                        for (Apartment apartment : apartments) {
                            if (apartment.getProperty().getName().equals(connection.getPropertyName())) {
                                dto.setPropertyId(apartment.getProperty().getId());
                                dto.setApartmentId(apartment.getId());
                                dto.setUnitId(apartment.getUnitNumber());
                                dto.setUnitNumber(apartment.getUnitNumber());
                                dto.setPropertyAddress(apartment.getProperty().getAddress());
                                break;
                            }
                        }
                    }

                    return dto;
                })
                .toList();
    }

    public List<User> searchTenantsGlobal(User manager, String searchText) {
        if (manager.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only managers can search tenants globally");
        }

        if (searchText == null || searchText.trim().isEmpty()) {
            // Return all active tenants
            return userRepository.findByRoleAndAccountStatus(UserRole.TENANT, com.bms.backend.enums.AccountStatus.ACTIVE);
        }

        // Search tenants by name, email, or phone
        return userRepository.findTenantsBySearchText(searchText.trim());
    }

    public List<TenantConnectionDto> getManagerTenantConnections(User manager, String searchText) {
        if (manager.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only managers can view their tenant connections");
        }

        List<TenantPropertyConnection> connections;
        if (searchText == null || searchText.trim().isEmpty()) {
            connections = connectionRepository.findByManagerAndIsActive(manager, true);
        } else {
            connections = connectionRepository.findByManagerAndSearchText(manager, searchText.trim());
        }

        return connections.stream()
                .map(connection -> {
                    TenantConnectionDto dto = new TenantConnectionDto(connection);

                    // Get apartment details directly from the connection
                    if (connection.getApartment() != null) {
                        Apartment apartment = connection.getApartment();
                        dto.setApartmentId(apartment.getId());
                        dto.setUnitName(apartment.getUnitNumber());

                        if (apartment.getProperty() != null) {
                            dto.setPropertyId(apartment.getProperty().getId());
                            dto.setPropertyAddress(apartment.getProperty().getAddress());
                        }
                    } else {
                        // Fallback for old connections without apartment reference
                        // Find apartment based on tenant email and property name
                        if (connection.getTenant() != null && connection.getTenant().getEmail() != null) {
                            List<Apartment> apartments = apartmentRepository.findByTenantEmail(connection.getTenant().getEmail());
                            for (Apartment apartment : apartments) {
                                if (apartment.getProperty() != null &&
                                    connection.getPropertyName() != null &&
                                    apartment.getProperty().getName() != null &&
                                    apartment.getProperty().getName().trim().equalsIgnoreCase(connection.getPropertyName().trim())) {
                                    dto.setApartmentId(apartment.getId());
                                    dto.setPropertyId(apartment.getProperty().getId());
                                    dto.setPropertyAddress(apartment.getProperty().getAddress());
                                    dto.setUnitName(apartment.getUnitNumber());
                                    break;
                                }
                            }
                        }
                    }

                    return dto;
                })
                .toList();
    }

    public LeaseDetailsDto getLeaseDetails(User user, UUID connectionId) {
        // Find the tenant property connection
        TenantPropertyConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Lease connection not found"));
        
        // Validate access - either tenant or manager can view
        if (user.getRole() == UserRole.TENANT) {
            if (!connection.getTenant().getId().equals(user.getId())) {
                throw new IllegalArgumentException("You don't have permission to view this lease");
            }
        } else if (user.getRole() == UserRole.PROPERTY_MANAGER) {
            if (!connection.getManager().getId().equals(user.getId())) {
                throw new IllegalArgumentException("You don't have permission to view this lease");
            }
        } else {
            throw new IllegalArgumentException("Only tenants and managers can view lease details");
        }
        
        // Find the apartment for this connection
        List<Apartment> apartments = apartmentRepository.findByTenantEmail(connection.getTenant().getEmail());
        Apartment apartment = null;
        for (Apartment apt : apartments) {
            if (apt.getProperty().getName().equals(connection.getPropertyName())) {
                apartment = apt;
                break;
            }
        }
        
        if (apartment == null) {
            throw new IllegalArgumentException("Apartment not found for this lease connection");
        }
        
        PropertyBuilding property = apartment.getProperty();
        User tenant = connection.getTenant();
        
        // Build the response DTO
        LeaseDetailsDto dto = new LeaseDetailsDto();
        
        // Connection/Lease Information
        dto.setConnectionId(connection.getId());
        dto.setLeaseId(generateLeaseId(connection.getId()));
        
        // Property Information
        dto.setPropertyId(property.getId());
        dto.setPropertyName(property.getName());
        dto.setPropertyType(property.getPropertyType());
        dto.setPropertyAddress(property.getAddress());
        dto.setRentAmount(connection.getMonthlyRent());
        
        // Property Images (sorted by display order)
        if (property.getImages() != null && !property.getImages().isEmpty()) {
            List<LeaseDetailsDto.PropertyImageDto> imagesDtos = property.getImages()
                .stream()
                .sorted((a, b) -> {
                    // Primary image first, then by display order
                    if (Boolean.TRUE.equals(a.getIsPrimary()) && !Boolean.TRUE.equals(b.getIsPrimary())) return -1;
                    if (!Boolean.TRUE.equals(a.getIsPrimary()) && Boolean.TRUE.equals(b.getIsPrimary())) return 1;
                    return Integer.compare(a.getDisplayOrder() != null ? a.getDisplayOrder() : 999, 
                                         b.getDisplayOrder() != null ? b.getDisplayOrder() : 999);
                })
                .map(img -> new LeaseDetailsDto.PropertyImageDto(
                    img.getId(),
                    img.getImageUrl(),
                    img.getImageData(),
                    img.getImageName(),
                    img.getDescription(),
                    Boolean.TRUE.equals(img.getIsPrimary()),
                    img.getDisplayOrder()
                ))
                .collect(Collectors.toList());
            dto.setPropertyImages(imagesDtos);
        }
        
        // Property Features (from apartment)
        dto.setBedrooms(apartment.getBedrooms());
        dto.setBathrooms(apartment.getBathrooms());
        dto.setSquareFootage(apartment.getSquareFootage());
        
        // Tenant Information
        dto.setTenantId(tenant.getId());
        dto.setTenantName(tenant.getFirstName() + " " + tenant.getLastName());
        dto.setTenantEmail(tenant.getEmail());
        dto.setTenantPhone(tenant.getPhone());

        // Manager Information
        User manager = connection.getManager();
        if (manager != null) {
            dto.setManagerId(manager.getId());
            dto.setManagerName(manager.getFirstName() + " " + manager.getLastName());
            dto.setManagerEmail(manager.getEmail());
            dto.setManagerPhone(manager.getPhone());
        }

        // Lease Details
        dto.setLeaseStartDate(connection.getStartDate());
        dto.setLeaseEndDate(connection.getEndDate());
        dto.setLeaseDuration(calculateLeaseDuration(connection.getStartDate(), connection.getEndDate()));
        dto.setPaymentFrequency(connection.getPaymentFrequency());

        // Deposit & Charges
        dto.setSecurityDeposit(connection.getSecurityDeposit());
        dto.setMaintenanceCharges(apartment.getMaintenanceCharges());
        dto.setUtilityMeterNumbers(apartment.getUtilityMeterNumbers());
        
        // Unit Details
        dto.setApartmentId(apartment.getId());
        dto.setUnitId(apartment.getUnitNumber());
        dto.setFloor(apartment.getFloor());
        dto.setOccupancyStatus(apartment.getOccupancyStatus());
        dto.setFurnished(apartment.getFurnished());
        
        // Navigation Actions
        dto.setHasMaintenanceRequests(hasMaintenanceRequests(apartment.getId(), tenant.getId()));
        dto.setHasDocuments(hasDocuments(apartment));
        
        return dto;
    }
    
    private String generateLeaseId(UUID connectionId) {
        // Extract some digits from UUID and format as LEASE-YYYY-XXXX
        String uuidStr = connectionId.toString().replace("-", "");
        String shortId = uuidStr.substring(uuidStr.length() - 4).toUpperCase();
        return "LEASE-2025-" + shortId;
    }
    
    private String calculateLeaseDuration(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return "Unknown";
        }
        
        Period period = Period.between(startDate, endDate);
        
        if (period.getYears() > 0) {
            return period.getYears() + " Year" + (period.getYears() > 1 ? "s" : "");
        } else if (period.getMonths() > 0) {
            return period.getMonths() + " Month" + (period.getMonths() > 1 ? "s" : "");
        } else {
            return period.getDays() + " Day" + (period.getDays() > 1 ? "s" : "");
        }
    }
    
    private boolean hasMaintenanceRequests(UUID apartmentId, UUID tenantId) {
        return maintenanceRequestRepository.existsByApartmentIdAndTenantId(apartmentId, tenantId);
    }
    
    private boolean hasDocuments(Apartment apartment) {
        return apartment.getDocuments() != null && !apartment.getDocuments().trim().isEmpty() ||
               (apartment.getApartmentDocuments() != null && !apartment.getApartmentDocuments().isEmpty());
    }

    public TenantDetailsDto getTenantDetails(User manager, UUID tenantId) {
        // Validate that the user is a manager
        if (manager.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only property managers can view tenant details");
        }

        // Find the tenant by UUID
        User tenant = userRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + tenantId));

        // Verify the user is actually a tenant
        if (tenant.getRole() != UserRole.TENANT) {
            throw new IllegalArgumentException("User is not a tenant");
        }

        // Get all active connections for this tenant under the manager
        List<TenantPropertyConnection> connections = connectionRepository.findByTenantAndManagerAndIsActive(tenant, manager, true);

        if (connections.isEmpty()) {
            throw new IllegalArgumentException("No active connections found for this tenant under your management");
        }

        // Build the tenant details DTO
        TenantDetailsDto dto = new TenantDetailsDto();

        // Set tenant basic information
        dto.setTenantId(tenant.getId());
        dto.setFirstName(tenant.getFirstName());
        dto.setLastName(tenant.getLastName());
        dto.setTenantName(tenant.getFirstName() + " " + tenant.getLastName());
        dto.setEmail(tenant.getEmail());
        dto.setPhone(tenant.getPhone());
        dto.setPhoto(tenant.getProfileImageUrl());
        dto.setAccountStatus(tenant.getAccountStatus() != null ? tenant.getAccountStatus().name() : "UNKNOWN");
        dto.setCreatedAt(tenant.getCreatedAt());

        // Build property information list
        List<TenantDetailsDto.TenantPropertyInfo> propertyInfoList = new ArrayList<>();
        double totalMonthlyRent = 0.0;
        int activeLeaseCount = 0;

        for (TenantPropertyConnection connection : connections) {
            TenantDetailsDto.TenantPropertyInfo propertyInfo = new TenantDetailsDto.TenantPropertyInfo();

            // Set connection details
            propertyInfo.setConnectionId(connection.getId());
            propertyInfo.setPropertyName(connection.getPropertyName());
            propertyInfo.setLeaseStartDate(connection.getStartDate());
            propertyInfo.setLeaseEndDate(connection.getEndDate());
            propertyInfo.setLeaseDuration(calculateLeaseDuration(connection.getStartDate(), connection.getEndDate()));
            propertyInfo.setMonthlyRent(connection.getMonthlyRent());
            propertyInfo.setSecurityDeposit(connection.getSecurityDeposit());
            propertyInfo.setPaymentFrequency(connection.getPaymentFrequency());
            propertyInfo.setNotes(connection.getNotes());
            propertyInfo.setIsActive(connection.getIsActive());

            // Set manager information
            User connectionManager = connection.getManager();
            if (connectionManager != null) {
                propertyInfo.setManagerId(connectionManager.getId());
                propertyInfo.setManagerName(connectionManager.getFirstName() + " " + connectionManager.getLastName());
                propertyInfo.setManagerEmail(connectionManager.getEmail());
                propertyInfo.setManagerPhone(connectionManager.getPhone());
            }

            // Get apartment details directly from the connection
            if (connection.getApartment() != null) {
                Apartment apartment = connection.getApartment();
                PropertyBuilding property = apartment.getProperty();

                // Set property details
                propertyInfo.setPropertyId(property.getId());
                propertyInfo.setPropertyType(property.getPropertyType());
                propertyInfo.setPropertyAddress(property.getAddress());

                // Set apartment/unit details
                propertyInfo.setApartmentId(apartment.getId());
                propertyInfo.setUnitNumber(apartment.getUnitNumber());
                propertyInfo.setUnitType(apartment.getUnitType());
                propertyInfo.setFloor(apartment.getFloor());
                propertyInfo.setBedrooms(apartment.getBedrooms());
                propertyInfo.setBathrooms(apartment.getBathrooms());
                propertyInfo.setSquareFootage(apartment.getSquareFootage());
                propertyInfo.setOccupancyStatus(apartment.getOccupancyStatus());
                propertyInfo.setFurnished(apartment.getFurnished());
                propertyInfo.setBalcony(apartment.getBalcony());
                propertyInfo.setImages(apartment.getImages());
                propertyInfo.setMaintenanceCharges(apartment.getMaintenanceCharges());
                propertyInfo.setUtilityMeterNumbers(apartment.getUtilityMeterNumbers());

                // Set additional flags
                propertyInfo.setHasMaintenanceRequests(hasMaintenanceRequests(apartment.getId(), tenant.getId()));
                propertyInfo.setHasDocuments(hasDocuments(apartment));
            } else {
                // Fallback for old connections without apartment reference
                List<Apartment> apartments = apartmentRepository.findByTenantEmail(tenant.getEmail());
                for (Apartment apartment : apartments) {
                    if (apartment.getProperty().getName().equals(connection.getPropertyName())) {
                        PropertyBuilding property = apartment.getProperty();

                        // Set property details
                        propertyInfo.setPropertyId(property.getId());
                        propertyInfo.setPropertyType(property.getPropertyType());
                        propertyInfo.setPropertyAddress(property.getAddress());

                        // Set apartment/unit details
                        propertyInfo.setApartmentId(apartment.getId());
                        propertyInfo.setUnitNumber(apartment.getUnitNumber());
                        propertyInfo.setUnitType(apartment.getUnitType());
                        propertyInfo.setFloor(apartment.getFloor());
                        propertyInfo.setBedrooms(apartment.getBedrooms());
                        propertyInfo.setBathrooms(apartment.getBathrooms());
                        propertyInfo.setSquareFootage(apartment.getSquareFootage());
                        propertyInfo.setOccupancyStatus(apartment.getOccupancyStatus());
                        propertyInfo.setFurnished(apartment.getFurnished());
                        propertyInfo.setBalcony(apartment.getBalcony());
                        propertyInfo.setImages(apartment.getImages());
                        propertyInfo.setMaintenanceCharges(apartment.getMaintenanceCharges());
                        propertyInfo.setUtilityMeterNumbers(apartment.getUtilityMeterNumbers());

                        // Set additional flags
                        propertyInfo.setHasMaintenanceRequests(hasMaintenanceRequests(apartment.getId(), tenant.getId()));
                        propertyInfo.setHasDocuments(hasDocuments(apartment));

                        break;
                    }
                }
            }

            propertyInfoList.add(propertyInfo);

            // Calculate totals
            if (connection.getIsActive() && connection.getMonthlyRent() != null) {
                totalMonthlyRent += connection.getMonthlyRent();
                activeLeaseCount++;
            }
        }

        // Set summary information
        dto.setProperties(propertyInfoList);
        dto.setTotalActiveLeases(activeLeaseCount);
        dto.setTotalProperties(propertyInfoList.size());
        dto.setTotalMonthlyRent(totalMonthlyRent);

        return dto;
    }

    /**
     * Get most urgent payment across all tenant's active leases
     * Returns the payment that needs most immediate attention:
     * 1. Overdue payments first
     * 2. Then soonest due date
     * 3. Then highest amount
     */
    public com.bms.backend.dto.response.UrgentPaymentDto getMostUrgentPayment(User tenant) {
        // Get all active leases for the tenant
        List<TenantPropertyConnection> activeLeases = connectionRepository.findByTenantAndIsActive(tenant, true);

        if (activeLeases.isEmpty()) {
            return null; // No active leases
        }

        // For each lease, find the most urgent unpaid payment
        java.time.LocalDate today = java.time.LocalDate.now();

        com.bms.backend.dto.response.LeasePaymentScheduleDto mostUrgentPayment = null;
        TenantPropertyConnection mostUrgentLease = null;
        int urgencyScore = Integer.MAX_VALUE; // Lower is more urgent

        for (TenantPropertyConnection lease : activeLeases) {
            // Find the most urgent unpaid payment for this lease
            var schedule = findMostUrgentUnpaidPayment(lease, today);

            if (schedule != null && !"PAID".equals(schedule.getStatus())) {
                // Calculate urgency score
                int score = calculateUrgencyScore(schedule, today);

                // Select most urgent payment with proper tie-breaking
                boolean shouldReplace = false;

                if (score < urgencyScore) {
                    // This payment is more urgent by score
                    shouldReplace = true;
                } else if (score == urgencyScore && mostUrgentPayment != null) {
                    // Tie in urgency score - use amount as tie-breaker (higher amount = more urgent)
                    if (schedule.getTotalAmount().compareTo(mostUrgentPayment.getTotalAmount()) > 0) {
                        shouldReplace = true;
                    }
                }

                if (shouldReplace) {
                    urgencyScore = score;
                    mostUrgentPayment = schedule;
                    mostUrgentLease = lease;
                }
            }
        }

        if (mostUrgentPayment == null) {
            return null;
        }

        // Build the response DTO
        com.bms.backend.dto.response.UrgentPaymentDto response = new com.bms.backend.dto.response.UrgentPaymentDto();
        response.setPaymentTransactionId(mostUrgentPayment.getPaymentTransactionId());
        response.setLeaseId(mostUrgentLease.getId());
        response.setConnectionId(mostUrgentLease.getId()); // Same as leaseId
        response.setPropertyName(mostUrgentLease.getPropertyName());

        // Build unit description
        String unitDesc = "To Lease-" + mostUrgentLease.getId().toString().substring(0, 3);
        if (mostUrgentLease.getApartment() != null) {
            unitDesc += " • Unit " + mostUrgentLease.getApartment().getUnitNumber();
        }
        response.setUnitDescription(unitDesc);

        response.setMonth(mostUrgentPayment.getMonth());
        response.setDueDate(mostUrgentPayment.getDueDate());
        response.setRentAmount(mostUrgentPayment.getRentAmount());
        response.setLateCharges(mostUrgentPayment.getLateCharges());
        response.setTotalAmount(mostUrgentPayment.getTotalAmount());
        response.setStatus(mostUrgentPayment.getStatus());
        response.setTotalActiveLeases(activeLeases.size());

        return response;
    }

    /**
     * Find the most urgent unpaid payment for a lease
     * Priority: OVERDUE > PENDING (sorted by due date)
     */
    private com.bms.backend.dto.response.LeasePaymentScheduleDto findMostUrgentUnpaidPayment(
            TenantPropertyConnection connection, java.time.LocalDate today) {

        // Get all payment transactions for this connection
        var allPayments = paymentTransactionRepository.findByConnectionOrderByCreatedAtDesc(connection);

        // Find the most urgent unpaid payment
        com.bms.backend.entity.PaymentTransaction mostUrgentPayment = null;
        boolean foundOverdue = false;

        for (var payment : allPayments) {
            if (payment.getDueDate() == null) continue;

            // Skip PAID payments
            if (payment.getStatus() == com.bms.backend.entity.PaymentTransaction.PaymentStatus.PAID) {
                continue;
            }

            java.time.LocalDate paymentDueDate = java.time.LocalDate.ofInstant(
                payment.getDueDate(), java.time.ZoneId.of("UTC"));
            java.time.YearMonth paymentMonth = java.time.YearMonth.from(paymentDueDate);
            java.time.YearMonth currentMonth = java.time.YearMonth.from(today);

            // Determine if overdue
            boolean isOverdue = false;
            if (paymentMonth.isBefore(currentMonth)) {
                isOverdue = true;
            } else if (paymentMonth.equals(currentMonth) && today.getDayOfMonth() > 5) {
                isOverdue = true;
            }

            // Priority: Overdue first, then earliest pending
            if (isOverdue) {
                if (!foundOverdue || paymentDueDate.isBefore(
                        java.time.LocalDate.ofInstant(mostUrgentPayment.getDueDate(), java.time.ZoneId.of("UTC")))) {
                    mostUrgentPayment = payment;
                    foundOverdue = true;
                }
            } else if (!foundOverdue) {
                // Only consider pending if no overdue found
                if (mostUrgentPayment == null || paymentDueDate.isBefore(
                        java.time.LocalDate.ofInstant(mostUrgentPayment.getDueDate(), java.time.ZoneId.of("UTC")))) {
                    mostUrgentPayment = payment;
                }
            }
        }

        if (mostUrgentPayment == null) {
            return null;
        }

        // Convert to DTO
        java.time.LocalDate dueDate = java.time.LocalDate.ofInstant(
            mostUrgentPayment.getDueDate(), java.time.ZoneId.of("UTC"));
        java.time.YearMonth paymentMonth = java.time.YearMonth.from(dueDate);
        java.time.YearMonth currentMonth = java.time.YearMonth.from(today);

        // Determine status
        String status;
        java.math.BigDecimal lateCharges = java.math.BigDecimal.ZERO;

        if (paymentMonth.isBefore(currentMonth)) {
            status = "OVERDUE";
            lateCharges = mostUrgentPayment.getAmount().multiply(java.math.BigDecimal.valueOf(0.10))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        } else if (paymentMonth.equals(currentMonth) && today.getDayOfMonth() > 5) {
            status = "OVERDUE";
            lateCharges = mostUrgentPayment.getAmount().multiply(java.math.BigDecimal.valueOf(0.10))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        } else {
            status = "PENDING";
        }

        // Build DTO
        com.bms.backend.dto.response.LeasePaymentScheduleDto schedule =
            new com.bms.backend.dto.response.LeasePaymentScheduleDto();
        schedule.setPaymentTransactionId(mostUrgentPayment.getId());
        schedule.setMonth(paymentMonth.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")));
        schedule.setDueDate(dueDate);
        schedule.setRentAmount(mostUrgentPayment.getAmount());
        schedule.setLateCharges(lateCharges);
        schedule.setTotalAmount(mostUrgentPayment.getAmount().add(lateCharges));
        schedule.setStatus(status);

        return schedule;
    }

    /**
     * Generate schedule for a single month (simplified version for urgent payment)
     */
    private com.bms.backend.dto.response.LeasePaymentScheduleDto generateSingleMonthSchedule(
            TenantPropertyConnection connection, java.time.YearMonth month) {

        java.time.LocalDate startDate = connection.getStartDate();
        java.time.LocalDate endDate = connection.getEndDate();
        java.time.YearMonth leaseStart = java.time.YearMonth.from(startDate);
        java.time.YearMonth leaseEnd = java.time.YearMonth.from(endDate);

        // Check if month is within lease period
        if (month.isBefore(leaseStart) || month.isAfter(leaseEnd)) {
            return null;
        }

        // Get payment transactions for this connection
        var paymentTransactions = paymentTransactionRepository.findByConnectionOrderByCreatedAtDesc(connection);

        // Find or create payment record for this month
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate dueDate = month.atDay(1);
        java.math.BigDecimal monthlyRent = java.math.BigDecimal.valueOf(connection.getMonthlyRent());

        // Check if payment exists for this month
        final java.time.YearMonth monthToCheck = month;
        var matchingPayment = paymentTransactions.stream()
            .filter(payment -> {
                java.time.Instant dueDateInstant = payment.getDueDate();
                if (dueDateInstant != null) {
                    java.time.LocalDate paymentDueDate = java.time.LocalDate.ofInstant(dueDateInstant, java.time.ZoneId.of("UTC"));
                    java.time.YearMonth paymentMonth = java.time.YearMonth.from(paymentDueDate);
                    return paymentMonth.equals(monthToCheck);
                }
                return false;
            })
            .findFirst();

        // Create payment record if doesn't exist
        if (matchingPayment.isEmpty()) {
            com.bms.backend.entity.PaymentTransaction newPayment = new com.bms.backend.entity.PaymentTransaction();
            newPayment.setTenant(connection.getTenant());
            newPayment.setConnection(connection);
            newPayment.setAmount(monthlyRent);
            newPayment.setCurrency("USD");
            newPayment.setStatus(com.bms.backend.entity.PaymentTransaction.PaymentStatus.PENDING);
            newPayment.setDescription("Monthly rent for " + connection.getPropertyName() + " - " + month);
            newPayment.setDueDate(dueDate.atStartOfDay(java.time.ZoneId.of("UTC")).toInstant());
            newPayment = paymentTransactionRepository.save(newPayment);
            matchingPayment = java.util.Optional.of(newPayment);
        }

        // Check if paid
        boolean isPaid = matchingPayment.isPresent() &&
            matchingPayment.get().getStatus() == com.bms.backend.entity.PaymentTransaction.PaymentStatus.PAID;

        // Determine status
        String status;
        java.math.BigDecimal lateCharges = java.math.BigDecimal.ZERO;

        if (isPaid) {
            status = "PAID";
        } else if (month.isBefore(java.time.YearMonth.now())) {
            status = "OVERDUE";
            lateCharges = monthlyRent.multiply(java.math.BigDecimal.valueOf(0.10))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        } else if (month.equals(java.time.YearMonth.now())) {
            if (today.getDayOfMonth() > 5) {
                status = "OVERDUE";
                lateCharges = monthlyRent.multiply(java.math.BigDecimal.valueOf(0.10))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            } else {
                status = "PENDING";
            }
        } else {
            status = "PENDING";
        }

        // Build DTO
        com.bms.backend.dto.response.LeasePaymentScheduleDto schedule =
            new com.bms.backend.dto.response.LeasePaymentScheduleDto();
        schedule.setPaymentTransactionId(matchingPayment.get().getId());
        schedule.setMonth(month.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")));
        schedule.setDueDate(dueDate);
        schedule.setRentAmount(monthlyRent);
        schedule.setLateCharges(lateCharges);
        schedule.setTotalAmount(monthlyRent.add(lateCharges));
        schedule.setStatus(status);

        return schedule;
    }

    /**
     * Calculate urgency score (lower = more urgent)
     * Score = (status_weight * 1000) + days_until_due
     */
    private int calculateUrgencyScore(com.bms.backend.dto.response.LeasePaymentScheduleDto payment,
                                      java.time.LocalDate today) {
        int statusWeight = 0;

        switch (payment.getStatus()) {
            case "OVERDUE":
                statusWeight = 0; // Most urgent
                break;
            case "PENDING":
                statusWeight = 1;
                break;
            case "PAID":
                statusWeight = 10; // Least urgent
                break;
            default:
                statusWeight = 5;
        }

        // Days until due (negative if overdue)
        long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(today, payment.getDueDate());

        return (statusWeight * 1000) + (int) daysUntilDue;
    }
}