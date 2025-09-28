package com.bms.backend.service;

import com.bms.backend.dto.response.TenantDetailsDto;
import com.bms.backend.entity.*;
import com.bms.backend.enums.AccountStatus;
import com.bms.backend.enums.UserRole;
import com.bms.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantPropertyConnectionRepository connectionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private ApartmentRepository apartmentRepository;

    @Mock
    private MaintenanceRequestRepository maintenanceRequestRepository;

    @InjectMocks
    private TenantService tenantService;

    private User manager;
    private User tenant;
    private TenantPropertyConnection connection;
    private Apartment apartment;
    private PropertyBuilding property;

    @BeforeEach
    void setUp() {
        // Setup Manager
        manager = new User();
        manager.setId(UUID.randomUUID());
        manager.setFirstName("John");
        manager.setLastName("Manager");
        manager.setEmail("manager@test.com");
        manager.setPhone("1234567890");
        manager.setRole(UserRole.PROPERTY_MANAGER);
        manager.setAccountStatus(AccountStatus.ACTIVE);
        manager.setCreatedAt(Instant.now());

        // Setup Tenant
        tenant = new User();
        tenant.setId(UUID.randomUUID());
        tenant.setFirstName("Jane");
        tenant.setLastName("Tenant");
        tenant.setEmail("tenant@test.com");
        tenant.setPhone("0987654321");
        tenant.setRole(UserRole.TENANT);
        tenant.setAccountStatus(AccountStatus.ACTIVE);
        tenant.setCreatedAt(Instant.now());

        // Setup Property
        property = new PropertyBuilding();
        property.setId(UUID.randomUUID());
        property.setName("Test Property");
        property.setPropertyType("Apartment");
        property.setAddress("123 Test St");
        property.setManager(manager);

        // Setup Apartment
        apartment = new Apartment();
        apartment.setId(UUID.randomUUID());
        apartment.setUnitNumber("A101");
        apartment.setFloor(1);
        apartment.setBedrooms(2);
        apartment.setBathrooms(new BigDecimal("1.5"));
        apartment.setSquareFootage(1200);
        apartment.setOccupancyStatus("OCCUPIED");
        apartment.setFurnished("FURNISHED");
        apartment.setMaintenanceCharges(new BigDecimal("100.00"));
        apartment.setUtilityMeterNumbers("ELEC123,GAS456");
        apartment.setTenantEmail(tenant.getEmail());
        apartment.setProperty(property);

        // Setup Connection
        connection = new TenantPropertyConnection(
                tenant, manager, property.getName(),
                LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(6), 1500.0
        );
        connection.setId(UUID.randomUUID());
        connection.setSecurityDeposit(3000.0);
        connection.setPaymentFrequency("MONTHLY");
        connection.setNotes("Test lease notes");
        connection.setIsActive(true);
    }

    @Test
    void testGetTenantDetails_Success() {
        // Arrange
        when(userRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(connectionRepository.findByTenantAndManagerAndIsActive(tenant, manager, true))
                .thenReturn(List.of(connection));
        when(apartmentRepository.findByTenantEmail(tenant.getEmail()))
                .thenReturn(List.of(apartment));
        when(maintenanceRequestRepository.existsByApartmentIdAndTenantId(apartment.getId(), tenant.getId()))
                .thenReturn(true);

        // Act
        TenantDetailsDto result = tenantService.getTenantDetails(manager, tenant.getId());

        // Assert
        assertNotNull(result);
        assertEquals(tenant.getId(), result.getTenantId());
        assertEquals("Jane Tenant", result.getTenantName());
        assertEquals(tenant.getEmail(), result.getEmail());
        assertEquals(tenant.getPhone(), result.getPhone());
        assertEquals("ACTIVE", result.getAccountStatus());
        assertEquals(tenant.getCreatedAt(), result.getCreatedAt());

        // Check properties list
        assertNotNull(result.getProperties());
        assertEquals(1, result.getProperties().size());

        TenantDetailsDto.TenantPropertyInfo propertyInfo = result.getProperties().get(0);
        assertEquals(connection.getId(), propertyInfo.getConnectionId());
        assertEquals(property.getName(), propertyInfo.getPropertyName());
        assertEquals(property.getPropertyType(), propertyInfo.getPropertyType());
        assertEquals(property.getAddress(), propertyInfo.getPropertyAddress());
        assertEquals(apartment.getId(), propertyInfo.getApartmentId());
        assertEquals(apartment.getUnitNumber(), propertyInfo.getUnitNumber());
        assertEquals(apartment.getFloor(), propertyInfo.getFloor());
        assertEquals(apartment.getBedrooms(), propertyInfo.getBedrooms());
        assertEquals(apartment.getBathrooms(), propertyInfo.getBathrooms());
        assertEquals(apartment.getSquareFootage(), propertyInfo.getSquareFootage());
        assertEquals(apartment.getFurnished(), propertyInfo.getFurnished());
        assertEquals(apartment.getMaintenanceCharges(), propertyInfo.getMaintenanceCharges());
        assertEquals(connection.getMonthlyRent(), propertyInfo.getMonthlyRent());
        assertEquals(connection.getSecurityDeposit(), propertyInfo.getSecurityDeposit());
        assertTrue(propertyInfo.getHasMaintenanceRequests());

        // Check summary
        assertEquals(1, result.getTotalActiveLeases());
        assertEquals(1, result.getTotalProperties());
        assertEquals(1500.0, result.getTotalMonthlyRent());

        // Verify interactions
        verify(userRepository).findById(tenant.getId());
        verify(connectionRepository).findByTenantAndManagerAndIsActive(tenant, manager, true);
        verify(apartmentRepository).findByTenantEmail(tenant.getEmail());
        verify(maintenanceRequestRepository).existsByApartmentIdAndTenantId(apartment.getId(), tenant.getId());
    }

    @Test
    void testGetTenantDetails_NonManagerUser() {
        // Arrange
        User nonManager = new User();
        nonManager.setRole(UserRole.TENANT);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tenantService.getTenantDetails(nonManager, tenant.getId())
        );
        assertEquals("Only property managers can view tenant details", exception.getMessage());
    }

    @Test
    void testGetTenantDetails_TenantNotFound() {
        // Arrange
        UUID nonExistentTenantId = UUID.randomUUID();
        when(userRepository.findById(nonExistentTenantId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tenantService.getTenantDetails(manager, nonExistentTenantId)
        );
        assertEquals("Tenant not found with ID: " + nonExistentTenantId, exception.getMessage());
    }

    @Test
    void testGetTenantDetails_UserIsNotTenant() {
        // Arrange
        User nonTenant = new User();
        nonTenant.setId(UUID.randomUUID());
        nonTenant.setRole(UserRole.PROPERTY_MANAGER);
        when(userRepository.findById(nonTenant.getId())).thenReturn(Optional.of(nonTenant));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tenantService.getTenantDetails(manager, nonTenant.getId())
        );
        assertEquals("User is not a tenant", exception.getMessage());
    }

    @Test
    void testGetTenantDetails_NoConnectionsFound() {
        // Arrange
        when(userRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(connectionRepository.findByTenantAndManagerAndIsActive(tenant, manager, true))
                .thenReturn(List.of());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tenantService.getTenantDetails(manager, tenant.getId())
        );
        assertEquals("No active connections found for this tenant under your management", exception.getMessage());
    }

    @Test
    void testGetTenantDetails_MultipleProperties() {
        // Arrange
        // Create second property and apartment
        PropertyBuilding property2 = new PropertyBuilding();
        property2.setId(UUID.randomUUID());
        property2.setName("Test Property 2");
        property2.setPropertyType("Condo");
        property2.setAddress("456 Test Ave");
        property2.setManager(manager);

        Apartment apartment2 = new Apartment();
        apartment2.setId(UUID.randomUUID());
        apartment2.setUnitNumber("B202");
        apartment2.setFloor(2);
        apartment2.setBedrooms(1);
        apartment2.setBathrooms(new BigDecimal("1.0"));
        apartment2.setSquareFootage(800);
        apartment2.setOccupancyStatus("OCCUPIED");
        apartment2.setFurnished("UNFURNISHED");
        apartment2.setMaintenanceCharges(new BigDecimal("75.00"));
        apartment2.setTenantEmail(tenant.getEmail());
        apartment2.setProperty(property2);

        TenantPropertyConnection connection2 = new TenantPropertyConnection(
                tenant, manager, property2.getName(),
                LocalDate.now().minusMonths(3), LocalDate.now().plusMonths(9), 1200.0
        );
        connection2.setId(UUID.randomUUID());
        connection2.setSecurityDeposit(2400.0);
        connection2.setPaymentFrequency("MONTHLY");
        connection2.setIsActive(true);

        when(userRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(connectionRepository.findByTenantAndManagerAndIsActive(tenant, manager, true))
                .thenReturn(List.of(connection, connection2));
        when(apartmentRepository.findByTenantEmail(tenant.getEmail()))
                .thenReturn(List.of(apartment, apartment2));
        when(maintenanceRequestRepository.existsByApartmentIdAndTenantId(any(), any()))
                .thenReturn(false);

        // Act
        TenantDetailsDto result = tenantService.getTenantDetails(manager, tenant.getId());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getProperties().size());
        assertEquals(2, result.getTotalActiveLeases());
        assertEquals(2, result.getTotalProperties());
        assertEquals(2700.0, result.getTotalMonthlyRent()); // 1500 + 1200
    }

    @Test
    void testGetTenantDetails_WithInactiveConnection() {
        // Arrange
        TenantPropertyConnection inactiveConnection = new TenantPropertyConnection(
                tenant, manager, property.getName(),
                LocalDate.now().minusMonths(12), LocalDate.now().minusMonths(6), 1000.0
        );
        inactiveConnection.setId(UUID.randomUUID());
        inactiveConnection.setIsActive(false);

        when(userRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(connectionRepository.findByTenantAndManagerAndIsActive(tenant, manager, true))
                .thenReturn(List.of(connection)); // Only active connection returned
        when(apartmentRepository.findByTenantEmail(tenant.getEmail()))
                .thenReturn(List.of(apartment));
        when(maintenanceRequestRepository.existsByApartmentIdAndTenantId(apartment.getId(), tenant.getId()))
                .thenReturn(false);

        // Act
        TenantDetailsDto result = tenantService.getTenantDetails(manager, tenant.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProperties().size());
        assertEquals(1, result.getTotalActiveLeases());
        assertEquals(1500.0, result.getTotalMonthlyRent()); // Only active connection rent
    }

}