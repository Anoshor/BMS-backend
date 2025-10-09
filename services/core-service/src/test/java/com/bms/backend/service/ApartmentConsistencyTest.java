package com.bms.backend.service;

import com.bms.backend.dto.request.ApartmentRequest;
import com.bms.backend.dto.request.UpdateApartmentRequest;
import com.bms.backend.entity.*;
import com.bms.backend.enums.UserRole;
import com.bms.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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

/**
 * Test suite for Apartment data consistency features
 * Tests the new baseRent/currentRent pattern to ensure single source of truth
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Apartment Consistency Tests")
class ApartmentConsistencyTest {

    @Mock
    private ApartmentRepository apartmentRepository;

    @Mock
    private PropertyBuildingRepository propertyBuildingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantPropertyConnectionRepository connectionRepository;

    @InjectMocks
    private ApartmentService apartmentService;

    private User manager;
    private PropertyBuilding property;
    private Apartment vacantApartment;
    private Apartment occupiedApartment;
    private TenantPropertyConnection activeLease;
    private User tenant;

    @BeforeEach
    void setUp() {
        // Create test manager
        manager = new User();
        manager.setId(UUID.randomUUID());
        manager.setEmail("manager@test.com");
        manager.setRole(UserRole.PROPERTY_MANAGER);
        manager.setFirstName("Test");
        manager.setLastName("Manager");

        // Create test tenant
        tenant = new User();
        tenant.setId(UUID.randomUUID());
        tenant.setEmail("tenant@test.com");
        tenant.setRole(UserRole.TENANT);
        tenant.setFirstName("Test");
        tenant.setLastName("Tenant");

        // Create test property
        property = new PropertyBuilding();
        property.setId(UUID.randomUUID());
        property.setName("Test Property");
        property.setManager(manager);

        // Create VACANT apartment with base pricing
        vacantApartment = new Apartment();
        vacantApartment.setId(UUID.randomUUID());
        vacantApartment.setProperty(property);
        vacantApartment.setUnitNumber("A101");
        vacantApartment.setBaseRent(BigDecimal.valueOf(1500.00));
        vacantApartment.setBaseSecurityDeposit(BigDecimal.valueOf(3000.00));
        vacantApartment.setOccupancyStatus("VACANT");

        // Create OCCUPIED apartment with base pricing
        occupiedApartment = new Apartment();
        occupiedApartment.setId(UUID.randomUUID());
        occupiedApartment.setProperty(property);
        occupiedApartment.setUnitNumber("A102");
        occupiedApartment.setBaseRent(BigDecimal.valueOf(1500.00));
        occupiedApartment.setBaseSecurityDeposit(BigDecimal.valueOf(3000.00));
        occupiedApartment.setOccupancyStatus("OCCUPIED");

        // Create active lease with discounted rent (actual rent tenant pays)
        activeLease = new TenantPropertyConnection();
        activeLease.setId(UUID.randomUUID());
        activeLease.setApartment(occupiedApartment);
        activeLease.setTenant(tenant);
        activeLease.setManager(manager);
        activeLease.setMonthlyRent(1350.00);  // Discounted from base 1500
        activeLease.setSecurityDeposit(2700.00);  // Discounted from base 3000
        activeLease.setIsActive(true);
        activeLease.setStartDate(LocalDate.now().minusMonths(6));
        activeLease.setEndDate(LocalDate.now().plusMonths(6));
    }

    @Test
    @DisplayName("Should create apartment with baseRent and baseSecurityDeposit")
    void testCreateApartmentWithBaseRent() {
        // Arrange
        ApartmentRequest request = new ApartmentRequest();
        request.setPropertyId(property.getId());
        request.setUnitNumber("A103");
        request.setBaseRent(BigDecimal.valueOf(1800.00));
        request.setBaseSecurityDeposit(BigDecimal.valueOf(3600.00));
        request.setFloor(1);
        request.setBedrooms(2);
        request.setBathrooms(BigDecimal.valueOf(2.0));

        when(propertyBuildingRepository.findById(property.getId())).thenReturn(Optional.of(property));
        when(apartmentRepository.findByPropertyAndUnitNumber(any(), any())).thenReturn(Optional.empty());
        when(apartmentRepository.save(any(Apartment.class))).thenAnswer(invocation -> {
            Apartment apt = invocation.getArgument(0);
            apt.setId(UUID.randomUUID());
            return apt;
        });

        // Act
        Apartment created = apartmentService.createApartment(request, manager);

        // Assert
        assertNotNull(created);
        assertEquals(BigDecimal.valueOf(1800.00), created.getBaseRent());
        assertEquals(BigDecimal.valueOf(3600.00), created.getBaseSecurityDeposit());
        assertNull(created.getCurrentRent());  // Vacant apartment has no current rent
        assertNull(created.getCurrentSecurityDeposit());
        verify(apartmentRepository).save(any(Apartment.class));
    }

    @Test
    @DisplayName("Should update apartment baseRent without affecting active lease")
    void testUpdateApartmentBaseRentDoesNotAffectLease() {
        // Arrange
        UpdateApartmentRequest request = new UpdateApartmentRequest();
        request.setUnitNumber("A102");
        request.setBaseRent(BigDecimal.valueOf(1600.00));  // Increased base rent
        request.setBaseSecurityDeposit(BigDecimal.valueOf(3200.00));
        request.setFloor(1);
        request.setBedrooms(2);
        request.setBathrooms(BigDecimal.valueOf(2.0));

        when(apartmentRepository.findById(occupiedApartment.getId())).thenReturn(Optional.of(occupiedApartment));
        when(apartmentRepository.save(any(Apartment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Apartment updated = apartmentService.updateApartment(occupiedApartment.getId(), request, manager);

        // Assert
        assertEquals(BigDecimal.valueOf(1600.00), updated.getBaseRent());  // Base rent updated
        assertEquals(BigDecimal.valueOf(3200.00), updated.getBaseSecurityDeposit());

        // Verify: Lease rent is NOT changed by this update
        // (Lease table is the source of truth for actual rent paid)
        assertEquals(1350.00, activeLease.getMonthlyRent());  // Still the original discounted rate
        verify(connectionRepository, never()).save(any());  // Lease not modified
    }

    @Test
    @DisplayName("Vacant apartment should have baseRent but no currentRent")
    void testVacantApartmentHasNoCurrentRent() {
        // Arrange
        when(apartmentRepository.findById(vacantApartment.getId())).thenReturn(Optional.of(vacantApartment));
        when(connectionRepository.findByApartment(vacantApartment)).thenReturn(Collections.emptyList());

        // Act
        Optional<Apartment> result = apartmentService.getApartmentById(vacantApartment.getId());

        // Assert
        assertTrue(result.isPresent());
        Apartment apt = result.get();

        // Base rent is available (for listing/advertising)
        assertNotNull(apt.getBaseRent());
        assertEquals(BigDecimal.valueOf(1500.00), apt.getBaseRent());
        assertNotNull(apt.getBaseSecurityDeposit());

        // No current rent (no active lease)
        assertNull(apt.getCurrentRent());
        assertNull(apt.getCurrentSecurityDeposit());
        assertNull(apt.getCurrentLeaseId());
    }

    @Test
    @DisplayName("Occupied apartment should have both baseRent and currentRent from active lease")
    void testOccupiedApartmentHasCurrentRentFromLease() {
        // Arrange
        when(apartmentRepository.findById(occupiedApartment.getId())).thenReturn(Optional.of(occupiedApartment));
        when(connectionRepository.findByApartment(occupiedApartment)).thenReturn(Arrays.asList(activeLease));

        // Act
        Optional<Apartment> result = apartmentService.getApartmentById(occupiedApartment.getId());

        // Assert
        assertTrue(result.isPresent());
        Apartment apt = result.get();

        // Base rent is still available (original advertised rate)
        assertEquals(BigDecimal.valueOf(1500.00), apt.getBaseRent());
        assertEquals(BigDecimal.valueOf(3000.00), apt.getBaseSecurityDeposit());

        // Current rent populated from ACTIVE LEASE (source of truth!)
        assertNotNull(apt.getCurrentRent());
        assertEquals(BigDecimal.valueOf(1350.00), apt.getCurrentRent());  // Discounted rate
        assertEquals(BigDecimal.valueOf(2700.00), apt.getCurrentSecurityDeposit());
        assertEquals(activeLease.getId(), apt.getCurrentLeaseId());
    }

    @Test
    @DisplayName("Should enforce data consistency - lease is source of truth for occupied units")
    void testLeaseIsSourceOfTruthForActualRent() {
        // Arrange: Create apartment and lease with DIFFERENT rent values
        Apartment apt = new Apartment();
        apt.setId(UUID.randomUUID());
        apt.setProperty(property);
        apt.setUnitNumber("A104");
        apt.setBaseRent(BigDecimal.valueOf(2000.00));  // Base rent
        apt.setBaseSecurityDeposit(BigDecimal.valueOf(4000.00));
        apt.setOccupancyStatus("OCCUPIED");

        TenantPropertyConnection lease = new TenantPropertyConnection();
        lease.setId(UUID.randomUUID());
        lease.setApartment(apt);
        lease.setMonthlyRent(1750.00);  // Actual rent (negotiated/discounted)
        lease.setSecurityDeposit(3500.00);
        lease.setIsActive(true);

        when(apartmentRepository.findById(apt.getId())).thenReturn(Optional.of(apt));
        when(connectionRepository.findByApartment(apt)).thenReturn(Arrays.asList(lease));

        // Act
        Optional<Apartment> result = apartmentService.getApartmentById(apt.getId());

        // Assert
        assertTrue(result.isPresent());
        Apartment retrieved = result.get();

        // Verify: currentRent comes from LEASE, not apartment
        assertEquals(BigDecimal.valueOf(1750.00), retrieved.getCurrentRent());  // From lease
        assertNotEquals(retrieved.getBaseRent(), retrieved.getCurrentRent());  // Different values

        // This proves: Lease table is the SINGLE SOURCE OF TRUTH for actual rent
        assertEquals(lease.getMonthlyRent(), retrieved.getCurrentRent().doubleValue());
    }

    @Test
    @DisplayName("Should handle apartment with no base rent set")
    void testApartmentWithNullBaseRent() {
        // Arrange
        Apartment apt = new Apartment();
        apt.setId(UUID.randomUUID());
        apt.setProperty(property);
        apt.setUnitNumber("A105");
        apt.setBaseRent(null);  // No base rent set
        apt.setBaseSecurityDeposit(null);
        apt.setOccupancyStatus("VACANT");

        when(apartmentRepository.findById(apt.getId())).thenReturn(Optional.of(apt));
        when(connectionRepository.findByApartment(apt)).thenReturn(Collections.emptyList());

        // Act
        Optional<Apartment> result = apartmentService.getApartmentById(apt.getId());

        // Assert
        assertTrue(result.isPresent());
        Apartment retrieved = result.get();
        assertNull(retrieved.getBaseRent());
        assertNull(retrieved.getCurrentRent());
    }

    @Test
    @DisplayName("Should only populate currentRent for OCCUPIED status")
    void testCurrentRentOnlyForOccupiedStatus() {
        // Arrange: Apartment in MAINTENANCE with an active lease
        Apartment apt = new Apartment();
        apt.setId(UUID.randomUUID());
        apt.setProperty(property);
        apt.setBaseRent(BigDecimal.valueOf(1500.00));
        apt.setOccupancyStatus("MAINTENANCE");  // Not OCCUPIED

        TenantPropertyConnection lease = new TenantPropertyConnection();
        lease.setMonthlyRent(1350.00);
        lease.setIsActive(true);

        when(apartmentRepository.findById(apt.getId())).thenReturn(Optional.of(apt));
        when(connectionRepository.findByApartment(apt)).thenReturn(Arrays.asList(lease));

        // Act
        Optional<Apartment> result = apartmentService.getApartmentById(apt.getId());

        // Assert
        assertTrue(result.isPresent());
        Apartment retrieved = result.get();

        // currentRent should NOT be populated for non-OCCUPIED status
        assertNull(retrieved.getCurrentRent());
        assertNull(retrieved.getCurrentLeaseId());
    }

    @Test
    @DisplayName("Should handle multiple inactive leases correctly")
    void testMultipleInactiveLeasesDoNotAffectCurrentRent() {
        // Arrange
        TenantPropertyConnection inactiveLease1 = new TenantPropertyConnection();
        inactiveLease1.setMonthlyRent(1200.00);
        inactiveLease1.setIsActive(false);  // Inactive

        TenantPropertyConnection inactiveLease2 = new TenantPropertyConnection();
        inactiveLease2.setMonthlyRent(1300.00);
        inactiveLease2.setIsActive(false);  // Inactive

        when(apartmentRepository.findById(occupiedApartment.getId())).thenReturn(Optional.of(occupiedApartment));
        when(connectionRepository.findByApartment(occupiedApartment))
            .thenReturn(Arrays.asList(inactiveLease1, inactiveLease2, activeLease));

        // Act
        Optional<Apartment> result = apartmentService.getApartmentById(occupiedApartment.getId());

        // Assert
        assertTrue(result.isPresent());
        Apartment apt = result.get();

        // Only ACTIVE lease should populate currentRent
        assertEquals(BigDecimal.valueOf(1350.00), apt.getCurrentRent());  // From activeLease
        assertEquals(activeLease.getId(), apt.getCurrentLeaseId());
    }
}
