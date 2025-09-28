package com.bms.backend.dto;

import com.bms.backend.dto.response.TenantConnectionDto;
import com.bms.backend.entity.TenantPropertyConnection;
import com.bms.backend.entity.User;
import com.bms.backend.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantConnectionDtoTest {

    private User tenant;
    private User manager;
    private TenantPropertyConnection connection;

    @BeforeEach
    void setUp() {
        // Setup tenant
        tenant = new User();
        tenant.setId(UUID.randomUUID());
        tenant.setFirstName("Jane");
        tenant.setLastName("Tenant");
        tenant.setEmail("tenant@test.com");
        tenant.setPhone("0987654321");
        tenant.setRole(UserRole.TENANT);
        tenant.setProfileImageUrl("https://example.com/tenant-profile.jpg");

        // Setup manager
        manager = new User();
        manager.setId(UUID.randomUUID());
        manager.setFirstName("John");
        manager.setLastName("Manager");
        manager.setEmail("manager@test.com");
        manager.setPhone("1234567890");
        manager.setRole(UserRole.PROPERTY_MANAGER);

        // Setup connection
        connection = new TenantPropertyConnection(
                tenant, manager, "Test Property",
                LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(6), 1500.0
        );
        connection.setId(UUID.randomUUID());
        connection.setSecurityDeposit(3000.0);
        connection.setPaymentFrequency("MONTHLY");
        connection.setNotes("Test lease notes");
        connection.setIsActive(true);
    }

    @Test
    void testTenantConnectionDto_IncludesTenantId() {
        // Create DTO from connection
        TenantConnectionDto dto = new TenantConnectionDto(connection);

        // Verify tenantId is set
        assertNotNull(dto.getTenantId(), "TenantId should not be null");
        assertEquals(tenant.getId(), dto.getTenantId(), "TenantId should match the tenant's UUID");

        // Verify tenantImage is set
        assertNotNull(dto.getTenantImage(), "TenantImage should not be null");
        assertEquals("https://example.com/tenant-profile.jpg", dto.getTenantImage(), "TenantImage should match the tenant's profile image URL");
    }

    @Test
    void testTenantConnectionDto_AllFieldsPopulated() {
        // Create DTO from connection
        TenantConnectionDto dto = new TenantConnectionDto(connection);

        // Verify all fields are populated correctly
        assertEquals(tenant.getId(), dto.getTenantId());
        assertEquals("Jane Tenant", dto.getTenantName());
        assertEquals("tenant@test.com", dto.getTenantEmail());
        assertEquals("0987654321", dto.getTenantPhone());
        assertEquals("Test Property", dto.getPropertyName());
        assertEquals(connection.getStartDate(), dto.getRentStart());
        assertEquals(connection.getEndDate(), dto.getRentEnd());
        assertEquals(1500.0, dto.getRentAmount());
        assertEquals(3000.0, dto.getSecurityDeposit());
        assertEquals("MONTHLY", dto.getPaymentFrequency());
        assertEquals("Test lease notes", dto.getNotes());
        assertTrue(dto.getIsActive());
    }

    @Test
    void testTenantConnectionDto_SettersAndGetters() {
        TenantConnectionDto dto = new TenantConnectionDto();
        UUID testTenantId = UUID.randomUUID();
        UUID testApartmentId = UUID.randomUUID();
        UUID testPropertyId = UUID.randomUUID();

        // Test tenantId setter and getter
        dto.setTenantId(testTenantId);
        assertEquals(testTenantId, dto.getTenantId());

        // Test new field setters and getters
        dto.setApartmentId(testApartmentId);
        dto.setPropertyId(testPropertyId);
        dto.setPropertyAddress("123 Main St, City, State");
        dto.setUnitName("Unit 101");

        assertEquals(testApartmentId, dto.getApartmentId());
        assertEquals(testPropertyId, dto.getPropertyId());
        assertEquals("123 Main St, City, State", dto.getPropertyAddress());
        assertEquals("Unit 101", dto.getUnitName());

        // Test other setters
        dto.setTenantName("Test Name");
        dto.setTenantEmail("test@example.com");
        dto.setTenantPhone("1111111111");

        assertEquals("Test Name", dto.getTenantName());
        assertEquals("test@example.com", dto.getTenantEmail());
        assertEquals("1111111111", dto.getTenantPhone());
    }

    @Test
    void testTenantConnectionDto_NullTenant() {
        // Create connection with null tenant
        TenantPropertyConnection connectionWithNullTenant = new TenantPropertyConnection(
                null, manager, "Test Property",
                LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(6), 1500.0
        );

        TenantConnectionDto dto = new TenantConnectionDto(connectionWithNullTenant);

        // When tenant is null, tenantId and related fields should be null
        assertNull(dto.getTenantId());
        assertNull(dto.getTenantName());
        assertNull(dto.getTenantEmail());
        assertNull(dto.getTenantPhone());
    }

    @Test
    void testTenantConnectionDto_NewFieldsNotNull() {
        // Create DTO from connection
        TenantConnectionDto dto = new TenantConnectionDto(connection);

        // The new fields propertyAddress and unitName should be settable
        dto.setPropertyAddress("456 Property Address");
        dto.setUnitName("Unit 202");

        // Verify the new fields are properly set
        assertEquals("456 Property Address", dto.getPropertyAddress());
        assertEquals("Unit 202", dto.getUnitName());

        // Verify these fields are included in the interface consistency
        assertNotNull(dto.getPropertyAddress());
        assertNotNull(dto.getUnitName());
    }

    @Test
    void testAPIFlowUsage() {
        // Simulate API flow
        TenantConnectionDto connectionDto = new TenantConnectionDto(connection);

        // Step 1: Frontend gets connections list (including tenantId)
        UUID tenantIdFromConnections = connectionDto.getTenantId();
        assertNotNull(tenantIdFromConnections, "Frontend should receive tenantId from connections API");

        // Step 2: Frontend uses tenantId to call details API
        // This would be: GET /tenants/details/{tenantId} where tenantId = connectionDto.getTenantId()
        assertEquals(tenant.getId(), tenantIdFromConnections, "TenantId should be usable for details API call");
    }
}