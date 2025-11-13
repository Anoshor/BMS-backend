package com.bms.payment.integration;

import com.bms.payment.dto.PaymentIntentRequest;
import com.bms.payment.dto.PaymentIntentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Payment Service
 * Tests the complete payment flow including Stripe integration
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/payments";

    @BeforeEach
    void setUp() {
        // Setup test data if needed
    }

    /**
     * Test 1: Get Stripe Publishable Key
     */
    @Test
    void testGetPublishableKey() throws Exception {
        mockMvc.perform(get(BASE_URL + "/stripe/publishable-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publishableKey").exists())
                .andExpect(jsonPath("$.publishableKey").isNotEmpty());
    }

    /**
     * Test 2: Create Card Payment Intent with Manual Amount
     * This tests the basic payment flow without core-service dependency
     */
    @Test
    void testCreateCardPaymentIntent_ManualAmount() throws Exception {
        PaymentIntentRequest request = PaymentIntentRequest.builder()
                .amount(5000L) // $50.00
                .currency("usd")
                .description("Test payment")
                .build();

        MvcResult result = mockMvc.perform(post(BASE_URL + "/create-card-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        PaymentIntentResponse response = objectMapper.readValue(responseJson, PaymentIntentResponse.class);

        // Verify response
        assertNotNull(response.getClientSecret(), "Client secret should not be null");
        assertNotNull(response.getPaymentIntentId(), "Payment intent ID should not be null");
        assertEquals(5000L, response.getAmount(), "Amount should be $50.00");
        assertEquals("usd", response.getCurrency());
        assertTrue(response.getClientSecret().startsWith("pi_"), "Client secret should start with 'pi_'");
    }

    /**
     * Test 3: Create Card Payment Intent with Minimum Amount (Should Fail)
     */
    @Test
    void testCreateCardPaymentIntent_BelowMinimum() throws Exception {
        PaymentIntentRequest request = PaymentIntentRequest.builder()
                .amount(40L) // Below 50 cents minimum
                .currency("usd")
                .build();

        mockMvc.perform(post(BASE_URL + "/create-card-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test 4: Create ACH Payment Intent with Manual Amount
     */
    @Test
    void testCreateACHPaymentIntent_ManualAmount() throws Exception {
        PaymentIntentRequest request = PaymentIntentRequest.builder()
                .amount(10000L) // $100.00
                .currency("usd")
                .description("Test ACH payment")
                .build();

        MvcResult result = mockMvc.perform(post(BASE_URL + "/create-ach-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        PaymentIntentResponse response = objectMapper.readValue(responseJson, PaymentIntentResponse.class);

        assertNotNull(response.getClientSecret());
        assertNotNull(response.getPaymentIntentId());
        assertEquals(10000L, response.getAmount());
    }

    /**
     * Test 5: Get Payment Intent by ID
     * Note: This test requires a valid payment intent ID from Stripe
     */
    @Test
    void testGetPaymentIntent_NotFound() throws Exception {
        String invalidPaymentIntentId = "pi_invalid_id_12345";

        mockMvc.perform(get(BASE_URL + "/" + invalidPaymentIntentId))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test 6: Cancel Payment Intent
     * Note: This test requires a valid payment intent ID
     */
    @Test
    void testCancelPaymentIntent_NotFound() throws Exception {
        String invalidPaymentIntentId = "pi_invalid_id_12345";

        mockMvc.perform(post(BASE_URL + "/" + invalidPaymentIntentId + "/cancel"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test 7: Create Payment Intent with Missing Required Fields
     */
    @Test
    void testCreatePaymentIntent_MissingAmount_WithoutLeaseId() throws Exception {
        PaymentIntentRequest request = PaymentIntentRequest.builder()
                .currency("usd")
                // Missing amount and leaseId
                .build();

        mockMvc.perform(post(BASE_URL + "/create-card-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test 8: Create Payment Intent with Tenant Details
     */
    @Test
    void testCreatePaymentIntent_WithTenantDetails() throws Exception {
        PaymentIntentRequest request = PaymentIntentRequest.builder()
                .amount(15000L)
                .currency("usd")
                .tenantId("04fc37d0-e819-4488-9849-4f237f9b45c1")
                .tenantEmail("test@example.com")
                .tenantName("Test Tenant")
                .tenantPhone("+1234567890")
                .description("Rent payment")
                .build();

        MvcResult result = mockMvc.perform(post(BASE_URL + "/create-card-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        PaymentIntentResponse response = objectMapper.readValue(responseJson, PaymentIntentResponse.class);

        assertNotNull(response.getClientSecret());
        assertEquals(15000L, response.getAmount());
    }
}
