package com.bms.backend.dto;

import com.bms.backend.dto.response.MaintenanceDetailsResponse;
import com.bms.backend.entity.*;
import com.bms.backend.enums.AccountStatus;
import com.bms.backend.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceDetailsResponseTest {

    private User manager;
    private User tenant;
    private PropertyBuilding property;
    private Apartment apartment;
    private ServiceCategory serviceCategory;
    private MaintenanceRequest maintenanceRequest;

    @BeforeEach
    void setUp() {
        // Setup Manager (Landlord)
        manager = new User();
        manager.setId(UUID.randomUUID());
        manager.setFirstName("John");
        manager.setLastName("Manager");
        manager.setEmail("manager@test.com");
        manager.setPhone("1234567890");
        manager.setRole(UserRole.PROPERTY_MANAGER);
        manager.setAccountStatus(AccountStatus.ACTIVE);

        // Setup Tenant
        tenant = new User();
        tenant.setId(UUID.randomUUID());
        tenant.setFirstName("Jane");
        tenant.setLastName("Tenant");
        tenant.setEmail("tenant@test.com");
        tenant.setPhone("0987654321");
        tenant.setRole(UserRole.TENANT);
        tenant.setAccountStatus(AccountStatus.ACTIVE);

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
        apartment.setTenantEmail(tenant.getEmail());
        apartment.setProperty(property);

        // Setup Service Category
        serviceCategory = new ServiceCategory();
        serviceCategory.setId(UUID.randomUUID());
        serviceCategory.setName("Plumbing");
        serviceCategory.setDescription("Plumbing related issues");

        // Setup Maintenance Request
        maintenanceRequest = new MaintenanceRequest();
        maintenanceRequest.setId(UUID.randomUUID());
        maintenanceRequest.setApartment(apartment);
        maintenanceRequest.setServiceCategory(serviceCategory);
        maintenanceRequest.setTitle("Leaky faucet");
        maintenanceRequest.setDescription("Kitchen faucet is leaking");
        maintenanceRequest.setPriority(MaintenanceRequest.Priority.MEDIUM);
        maintenanceRequest.setStatus(MaintenanceRequest.Status.OPEN);
        maintenanceRequest.setRequester(tenant);
        maintenanceRequest.setTenant(tenant);
        maintenanceRequest.setCreatedAt(Instant.now());
        maintenanceRequest.setUpdatedAt(Instant.now());
        maintenanceRequest.setManagerInitiated(false);
    }

    @Test
    void testMaintenanceDetailsResponse_IncludesLandlordDetails() {
        // Create DTO from maintenance request
        MaintenanceDetailsResponse response = new MaintenanceDetailsResponse(maintenanceRequest);

        // Verify landlord details are populated
        assertNotNull(response.getLandlordId(), "Landlord ID should not be null");
        assertNotNull(response.getLandlordName(), "Landlord name should not be null");
        assertNotNull(response.getLandlordEmail(), "Landlord email should not be null");
        assertNotNull(response.getLandlordPhone(), "Landlord phone should not be null");

        // Verify landlord details match the manager
        assertEquals(manager.getId(), response.getLandlordId(), "Landlord ID should match manager ID");
        assertEquals("John Manager", response.getLandlordName(), "Landlord name should be manager's full name");
        assertEquals("manager@test.com", response.getLandlordEmail(), "Landlord email should match manager's email");
        assertEquals("1234567890", response.getLandlordPhone(), "Landlord phone should match manager's phone");
    }

    @Test
    void testMaintenanceDetailsResponse_AllFieldsPopulated() {
        // Create DTO from maintenance request
        MaintenanceDetailsResponse response = new MaintenanceDetailsResponse(maintenanceRequest);

        // Verify basic maintenance request fields
        assertEquals(maintenanceRequest.getId(), response.getId());
        assertEquals("Leaky faucet", response.getTitle());
        assertEquals("Kitchen faucet is leaking", response.getDescription());
        assertEquals("MEDIUM", response.getPriority());
        assertEquals("OPEN", response.getStatus());

        // Verify apartment fields
        assertEquals(apartment.getId(), response.getApartmentId());
        assertEquals("A101", response.getApartmentUnitNumber());

        // Verify service category fields
        assertEquals(serviceCategory.getId(), response.getServiceCategoryId());
        assertEquals("Plumbing", response.getServiceCategoryName());

        // Verify requester fields
        assertEquals(tenant.getId(), response.getRequesterId());
        assertEquals("Jane Tenant", response.getRequesterName());
        assertEquals("tenant@test.com", response.getRequesterEmail());

        // Verify tenant fields
        assertEquals(tenant.getId(), response.getTenantId());
        assertEquals("Jane Tenant", response.getTenantName());
        assertEquals("tenant@test.com", response.getTenantEmail());

        // Verify landlord fields (the main test)
        assertEquals(manager.getId(), response.getLandlordId());
        assertEquals("John Manager", response.getLandlordName());
        assertEquals("manager@test.com", response.getLandlordEmail());
        assertEquals("1234567890", response.getLandlordPhone());

        // Verify timestamps
        assertEquals(maintenanceRequest.getCreatedAt(), response.getCreatedAt());
        assertEquals(maintenanceRequest.getUpdatedAt(), response.getUpdatedAt());
        assertEquals(false, response.getManagerInitiated());
    }

    @Test
    void testMaintenanceDetailsResponse_NullPropertyHandling() {
        // Create maintenance request with null apartment property
        maintenanceRequest.setApartment(null);

        MaintenanceDetailsResponse response = new MaintenanceDetailsResponse(maintenanceRequest);

        // When property is null, landlord fields should be null
        assertNull(response.getLandlordId());
        assertNull(response.getLandlordName());
        assertNull(response.getLandlordEmail());
        assertNull(response.getLandlordPhone());
        assertNull(response.getApartmentId());
        assertNull(response.getApartmentUnitNumber());
    }

    @Test
    void testMaintenanceDetailsResponse_NullManagerHandling() {
        // Create property without manager
        property.setManager(null);

        MaintenanceDetailsResponse response = new MaintenanceDetailsResponse(maintenanceRequest);

        // When manager is null, landlord fields should be null
        assertNull(response.getLandlordId());
        assertNull(response.getLandlordName());
        assertNull(response.getLandlordEmail());
        assertNull(response.getLandlordPhone());

        // But apartment fields should still be populated
        assertEquals(apartment.getId(), response.getApartmentId());
        assertEquals("A101", response.getApartmentUnitNumber());
    }

    @Test
    void testMaintenanceDetailsResponse_ManagerInitiatedRequest() {
        // Create a manager-initiated request
        maintenanceRequest.setRequester(manager);
        maintenanceRequest.setManagerInitiated(true);

        MaintenanceDetailsResponse response = new MaintenanceDetailsResponse(maintenanceRequest);

        // Verify landlord details are still populated
        assertEquals(manager.getId(), response.getLandlordId());
        assertEquals("John Manager", response.getLandlordName());
        assertEquals("manager@test.com", response.getLandlordEmail());
        assertEquals("1234567890", response.getLandlordPhone());

        // Verify requester is now the manager
        assertEquals(manager.getId(), response.getRequesterId());
        assertEquals("John Manager", response.getRequesterName());
        assertEquals("manager@test.com", response.getRequesterEmail());

        // Verify manager initiated flag
        assertEquals(true, response.getManagerInitiated());
    }

    @Test
    void testLandlordFieldSettersAndGetters() {
        MaintenanceDetailsResponse response = new MaintenanceDetailsResponse();
        UUID testLandlordId = UUID.randomUUID();

        // Test landlord field setters and getters
        response.setLandlordId(testLandlordId);
        response.setLandlordName("Test Landlord");
        response.setLandlordEmail("landlord@test.com");
        response.setLandlordPhone("5555555555");

        assertEquals(testLandlordId, response.getLandlordId());
        assertEquals("Test Landlord", response.getLandlordName());
        assertEquals("landlord@test.com", response.getLandlordEmail());
        assertEquals("5555555555", response.getLandlordPhone());
    }
}