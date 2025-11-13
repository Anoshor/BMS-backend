package com.bms.payment.integration;

import com.bms.payment.client.CoreServiceClient;
import com.bms.payment.dto.LeasePaymentDetailsDto;
import com.bms.payment.dto.PaymentIntentRequest;
import com.bms.payment.dto.PaymentIntentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for secure lease payment flow
 * Tests server-side amount verification to prevent payment tampering
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SecureLeasePaymentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CoreServiceClient coreServiceClient;

    private static final String BASE_URL = "/api/payments";

    /**
     * Test: Secure Payment Flow - Amount Fetched from Core Service
     * This is the CRITICAL security test - ensures client cannot tamper with amount
     */
    @Test
    void testSecureLeasePayment_ServerSideAmountVerification() throws Exception {
        // Mock lease payment details from core-service
        String leaseId = "04fc37d0-e819-4488-9849-4f237f9b45c1";
        LeasePaymentDetailsDto mockLeaseDetails = new LeasePaymentDetailsDto();
        mockLeaseDetails.setLeaseId("LEASE-2025-6E99");
        mockLeaseDetails.setTenantId("tenant-uuid-123");
        mockLeaseDetails.setTenantName("Sudarshana V Sharma");
        mockLeaseDetails.setTenantEmail("sudarshana@example.com");
        mockLeaseDetails.setTenantPhone("+1234567890");
        mockLeaseDetails.setPropertyName("Sunset Apartments");
        mockLeaseDetails.setRentAmount(new BigDecimal("600.00"));
        mockLeaseDetails.setLatePaymentCharges(new BigDecimal("60.00"));
        mockLeaseDetails.setTotalPayableAmount(new BigDecimal("660.00")); // Server-side calculated!

        // Mock the core service response
        when(coreServiceClient.getLeasePaymentDetails(eq(leaseId), any()))
                .thenReturn(mockLeaseDetails);

        // Client sends only leaseId (NO amount!)
        PaymentIntentRequest request = PaymentIntentRequest.builder()
                .leaseId(leaseId)
                // Client does NOT send amount - server fetches it!
                .build();

        // Create payment intent
        MvcResult result = mockMvc.perform(post(BASE_URL + "/create-card-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer fake-token")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        PaymentIntentResponse response = objectMapper.readValue(responseJson, PaymentIntentResponse.class);

        // CRITICAL: Verify the amount is from server, not client!
        assertEquals(66000L, response.getAmount(),
            "Amount should be $660.00 (66000 cents) from server, NOT from client!");
        assertNotNull(response.getClientSecret());
        assertNotNull(response.getPaymentIntentId());
    }

    /**
     * Test: Client Tampering Prevention
     * Even if client sends wrong amount, server should use correct amount from core-service
     */
    @Test
    void testSecureLeasePayment_IgnoreClientAmount() throws Exception {
        String leaseId = "04fc37d0-e819-4488-9849-4f237f9b45c1";

        // Mock server-side amount
        LeasePaymentDetailsDto mockLeaseDetails = new LeasePaymentDetailsDto();
        mockLeaseDetails.setTenantId("tenant-uuid");
        mockLeaseDetails.setTenantEmail("test@example.com");
        mockLeaseDetails.setTenantName("Test Tenant");
        mockLeaseDetails.setPropertyName("Test Property");
        mockLeaseDetails.setTotalPayableAmount(new BigDecimal("1000.00")); // Server says $1000

        when(coreServiceClient.getLeasePaymentDetails(eq(leaseId), any()))
                .thenReturn(mockLeaseDetails);

        // Client tries to tamper - sends $1 instead of $1000!
        PaymentIntentRequest request = PaymentIntentRequest.builder()
                .leaseId(leaseId)
                .amount(100L) // Client sends $1.00 (trying to cheat!)
                .build();

        MvcResult result = mockMvc.perform(post(BASE_URL + "/create-card-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer fake-token")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        PaymentIntentResponse response = objectMapper.readValue(responseJson, PaymentIntentResponse.class);

        // SECURITY CHECK: Server should charge $1000, NOT $1!
        assertEquals(100000L, response.getAmount(),
            "Server should charge $1000 (from server), NOT $1 (from client)!");
    }

    /**
     * Test: Lease Payment with Late Charges
     */
    @Test
    void testSecureLeasePayment_WithLateCharges() throws Exception {
        String leaseId = "lease-with-late-fee";

        LeasePaymentDetailsDto mockLeaseDetails = new LeasePaymentDetailsDto();
        mockLeaseDetails.setTenantId("tenant-uuid");
        mockLeaseDetails.setTenantEmail("test@example.com");
        mockLeaseDetails.setTenantName("Test Tenant");
        mockLeaseDetails.setPropertyName("Test Property");
        mockLeaseDetails.setRentAmount(new BigDecimal("500.00"));
        mockLeaseDetails.setLatePaymentCharges(new BigDecimal("50.00"));
        mockLeaseDetails.setTotalPayableAmount(new BigDecimal("550.00"));

        when(coreServiceClient.getLeasePaymentDetails(eq(leaseId), any()))
                .thenReturn(mockLeaseDetails);

        PaymentIntentRequest request = PaymentIntentRequest.builder()
                .leaseId(leaseId)
                .build();

        MvcResult result = mockMvc.perform(post(BASE_URL + "/create-card-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer fake-token")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        PaymentIntentResponse response = objectMapper.readValue(responseJson, PaymentIntentResponse.class);

        // Should include late charges
        assertEquals(55000L, response.getAmount(),
            "Amount should include late charges: $500 + $50 = $550");
    }

    /**
     * Test: ACH Payment with Lease ID
     */
    @Test
    void testSecureACHPayment_WithLeaseId() throws Exception {
        String leaseId = "ach-lease-test";

        LeasePaymentDetailsDto mockLeaseDetails = new LeasePaymentDetailsDto();
        mockLeaseDetails.setTenantId("tenant-uuid");
        mockLeaseDetails.setTenantEmail("test@example.com");
        mockLeaseDetails.setTenantName("Test Tenant");
        mockLeaseDetails.setPropertyName("Test Property");
        mockLeaseDetails.setTotalPayableAmount(new BigDecimal("750.00"));

        when(coreServiceClient.getLeasePaymentDetails(eq(leaseId), any()))
                .thenReturn(mockLeaseDetails);

        PaymentIntentRequest request = PaymentIntentRequest.builder()
                .leaseId(leaseId)
                .build();

        MvcResult result = mockMvc.perform(post(BASE_URL + "/create-ach-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer fake-token")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        PaymentIntentResponse response = objectMapper.readValue(responseJson, PaymentIntentResponse.class);

        assertEquals(75000L, response.getAmount(), "ACH payment should use server-side amount");
    }
}
