package com.bms.backend.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${app.name:Building Management System}")
    private String appName;

    @Value("${sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${twilio.phone-number:}")
    private String twilioPhoneNumber;

    public void sendVerificationSms(String phoneNumber, String otpCode) {
        String message = String.format(
            "Your %s verification code is: %s. Valid for 10 minutes.",
            appName, otpCode
        );

        sendSms(phoneNumber, message);
    }

    public void sendPasswordResetSms(String phoneNumber, String otpCode) {
        String message = String.format(
            "Your %s password reset code is: %s. Valid for 10 minutes.",
            appName, otpCode
        );

        sendSms(phoneNumber, message);
    }

    public void sendLoginAlertSms(String phoneNumber, String deviceInfo) {
        String message = String.format(
            "New login to your %s account from %s. If this wasn't you, please secure your account immediately.",
            appName, deviceInfo
        );

        sendSms(phoneNumber, message);
    }

    private void sendSms(String phoneNumber, String message) {
        if (!smsEnabled) {
            // For development/testing - just log the SMS
            System.out.println("=== SMS (Dev Mode) ===");
            System.out.println("To: " + phoneNumber);
            System.out.println("Message: " + message);
            System.out.println("======================");
            return;
        }

        try {
            // Send SMS via Twilio
            Message twilioMessage = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    message
            ).create();

            System.out.println("SMS sent successfully. SID: " + twilioMessage.getSid());

        } catch (Exception e) {
            // Fallback to console log if Twilio fails
            System.err.println("Twilio failed, falling back to console log: " + e.getMessage());
            logSmsToConsole(phoneNumber, message);
        }
    }

    private void logSmsToConsole(String phoneNumber, String message) {
        System.out.println("=== SMS (Fallback Log) ===");
        System.out.println("To: " + phoneNumber);
        System.out.println("Message: " + message);
        System.out.println("==========================");
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }

    // Method to validate phone number format before sending
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        // Basic phone number validation (E.164 format)
        return phoneNumber.matches("^\\+?[1-9]\\d{1,14}$");
    }
}
