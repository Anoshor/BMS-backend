package com.bms.backend.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {

    @Value("${sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @PostConstruct
    public void initTwilio() {
        if (smsEnabled && accountSid != null && !accountSid.isEmpty()
                && authToken != null && !authToken.isEmpty()) {
            Twilio.init(accountSid, authToken);
            System.out.println("Twilio SDK initialized successfully");
        } else {
            System.out.println("Twilio SMS disabled or credentials not configured - running in development mode");
        }
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }
}
