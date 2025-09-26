package com.bms.backend.controller;

import com.bms.backend.dto.response.TenantConnectionDto;
import com.bms.backend.dto.response.TenantDetailsDto;
import com.bms.backend.entity.User;
import com.bms.backend.enums.UserRole;
import com.bms.backend.service.TenantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TenantController.class)
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TenantService tenantService;

    @Autowired
    private ObjectMapper objectMapper;

    private User manager;
    private UUID tenantId;
    private TenantConnectionDto tenantConnection;
    private TenantDetailsDto tenantDetails;

    @BeforeEach
    void setUp() {
        manager = new User();
        manager.setId(UUID.randomUUID());
        manager.setRole(UserRole.PROPERTY_MANAGER);

        tenantId = UUID.randomUUID();

        // Setup TenantConnectionDto with tenantId
        tenantConnection = new TenantConnectionDto();
        tenantConnection.setTenantId(tenantId);
        tenantConnection.setTenantName("Jane Tenant");
        tenantConnection.setTenantEmail("tenant@test.com");
        tenantConnection.setTenantPhone("0987654321");
        tenantConnection.setPropertyName("Test Property");
        tenantConnection.setRentStart(LocalDate.now().minusMonths(6));
        tenantConnection.setRentEnd(LocalDate.now().plusMonths(6));
        tenantConnection.setRentAmount(1500.0);
        tenantConnection.setSecurityDeposit(3000.0);
        tenantConnection.setPaymentFrequency("MONTHLY");
        tenantConnection.setIsActive(true);

        // Setup TenantDetailsDto
        tenantDetails = new TenantDetailsDto();
        tenantDetails.setTenantId(tenantId);
        tenantDetails.setFirstName("Jane");
        tenantDetails.setLastName("Tenant");
        tenantDetails.setTenantName("Jane Tenant");
        tenantDetails.setEmail("tenant@test.com");
        tenantDetails.setPhone("0987654321");
        tenantDetails.setAccountStatus("ACTIVE");
        tenantDetails.setCreatedAt(Instant.now());
        tenantDetails.setTotalActiveLeases(1);
        tenantDetails.setTotalProperties(1);
        tenantDetails.setTotalMonthlyRent(1500.0);

        // Setup property info
        TenantDetailsDto.TenantPropertyInfo propertyInfo = new TenantDetailsDto.TenantPropertyInfo();
        propertyInfo.setConnectionId(UUID.randomUUID());
        propertyInfo.setPropertyName("Test Property");
        propertyInfo.setPropertyType("Apartment");
        propertyInfo.setPropertyAddress("123 Test St");
        propertyInfo.setUnitNumber("A101");
        propertyInfo.setFloor(1);
        propertyInfo.setBedrooms(2);
        propertyInfo.setMonthlyRent(1500.0);
        propertyInfo.setSecurityDeposit(3000.0);
        propertyInfo.setPaymentFrequency("MONTHLY");
        propertyInfo.setManagerName("John Manager");
        propertyInfo.setIsActive(true);

        tenantDetails.setProperties(List.of(propertyInfo));
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = "PROPERTY_MANAGER")
    void testGetTenantConnections_IncludesTenantId() throws Exception {
        when(tenantService.getManagerTenantConnections(any(User.class), eq(null)))
                .thenReturn(List.of(tenantConnection));

        mockMvc.perform(get("/tenants/connections")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].tenantId").value(tenantId.toString()))
                .andExpect(jsonPath("$.data[0].tenantName").value("Jane Tenant"))
                .andExpect(jsonPath("$.data[0].tenantEmail").value("tenant@test.com"))
                .andExpect(jsonPath("$.data[0].propertyName").value("Test Property"))
                .andExpect(jsonPath("$.data[0].rentAmount").value(1500.0))
                .andExpect(jsonPath("$.data[0].securityDeposit").value(3000.0))
                .andExpect(jsonPath("$.data[0].paymentFrequency").value("MONTHLY"));
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = "PROPERTY_MANAGER")
    void testGetTenantDetails_Success() throws Exception {
        when(tenantService.getTenantDetails(any(User.class), eq(tenantId)))
                .thenReturn(tenantDetails);

        mockMvc.perform(get("/tenants/details/{tenantId}", tenantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tenantId").value(tenantId.toString()))
                .andExpect(jsonPath("$.data.tenantName").value("Jane Tenant"))
                .andExpect(jsonPath("$.data.firstName").value("Jane"))
                .andExpect(jsonPath("$.data.lastName").value("Tenant"))
                .andExpect(jsonPath("$.data.email").value("tenant@test.com"))
                .andExpect(jsonPath("$.data.phone").value("0987654321"))
                .andExpect(jsonPath("$.data.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.totalActiveLeases").value(1))
                .andExpect(jsonPath("$.data.totalProperties").value(1))
                .andExpect(jsonPath("$.data.totalMonthlyRent").value(1500.0))
                .andExpect(jsonPath("$.data.properties").isArray())
                .andExpect(jsonPath("$.data.properties[0].propertyName").value("Test Property"))
                .andExpect(jsonPath("$.data.properties[0].propertyAddress").value("123 Test St"))
                .andExpect(jsonPath("$.data.properties[0].unitNumber").value("A101"))
                .andExpect(jsonPath("$.data.properties[0].managerName").value("John Manager"));
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = "PROPERTY_MANAGER")
    void testGetTenantDetails_TenantNotFound() throws Exception {
        UUID nonExistentTenantId = UUID.randomUUID();
        when(tenantService.getTenantDetails(any(User.class), eq(nonExistentTenantId)))
                .thenThrow(new IllegalArgumentException("Tenant not found with ID: " + nonExistentTenantId));

        mockMvc.perform(get("/tenants/details/{tenantId}", nonExistentTenantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tenant not found with ID: " + nonExistentTenantId));
    }

    @Test
    @WithMockUser(username = "tenant@test.com", roles = "TENANT")
    void testGetTenantDetails_UnauthorizedRole() throws Exception {
        when(tenantService.getTenantDetails(any(User.class), eq(tenantId)))
                .thenThrow(new IllegalArgumentException("Only property managers can view tenant details"));

        mockMvc.perform(get("/tenants/details/{tenantId}", tenantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Only property managers can view tenant details"));
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = "PROPERTY_MANAGER")
    void testAPIFlowConsistency() throws Exception {
        // Test that tenantId from connections can be used in details API
        when(tenantService.getManagerTenantConnections(any(User.class), eq(null)))
                .thenReturn(List.of(tenantConnection));
        when(tenantService.getTenantDetails(any(User.class), eq(tenantId)))
                .thenReturn(tenantDetails);

        // Step 1: Get connections and verify tenantId is present
        mockMvc.perform(get("/tenants/connections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tenantId").value(tenantId.toString()));

        // Step 2: Use the tenantId to get details
        mockMvc.perform(get("/tenants/details/{tenantId}", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantId").value(tenantId.toString()))
                .andExpect(jsonPath("$.data.tenantName").value(tenantConnection.getTenantName()))
                .andExpect(jsonPath("$.data.email").value(tenantConnection.getTenantEmail()));
    }
}