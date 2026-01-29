package com.bms.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BrevoConfig {

    @Value("${email.enabled:false}")
    private boolean emailEnabled;

    @Value("${brevo.api-key:}")
    private String apiKey;

    @Value("${brevo.sender-email:noreply@bms.com}")
    private String senderEmail;

    @Value("${brevo.sender-name:Building Management System}")
    private String senderName;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public String getSenderName() {
        return senderName;
    }
}
