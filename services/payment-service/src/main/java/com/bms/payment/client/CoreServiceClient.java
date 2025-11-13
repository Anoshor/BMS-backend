package com.bms.payment.client;

import com.bms.payment.dto.LeasePaymentDetailsDto;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Client to communicate with Core Service
 */
@Component
@RequiredArgsConstructor

public class CoreServiceClient {

    private final RestTemplate restTemplate;

    @Value("${core.service.url:http://localhost:8080}")
    private String coreServiceUrl;

    /**
     * Fetch lease payment details from core-service
     * This ensures the payment amount is fetched server-side and cannot be tampered by the client
     */
    public LeasePaymentDetailsDto getLeasePaymentDetails(String leaseId, String authToken) {
        try {
            String url = coreServiceUrl + "/api/v1/leases/" + leaseId + "/payment-details";


            // Make HTTP call with authorization header
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", authToken);

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<ApiResponse<LeasePaymentDetailsDto>> response =
                restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    new org.springframework.core.ParameterizedTypeReference<ApiResponse<LeasePaymentDetailsDto>>() {}
                );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getData();
            }

            throw new RuntimeException("Failed to fetch lease payment details: " +
                (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"));

        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Failed to fetch lease payment details: " + e.getMessage());
        }
    }

    // DTO to match core-service ApiResponse structure
    @lombok.Data
    public static class ApiResponse<T> {
        private boolean success;
        private T data;
        private String message;
    }
}
