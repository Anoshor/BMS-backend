package com.bms.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    
    @Value("${app.name:Building Management System}")
    private String appName;
    
    @Value("${sms.enabled:false}")
    private boolean smsEnabled;
    
    // In production, integrate with SMS providers like Twilio, AWS SNS, etc.
    
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
            System.out.println("SMS to " + phoneNumber + ": " + message);
            return;
        }
        
        try {
            // TODO: Integrate with actual SMS service provider
            // Examples:
            // - Twilio
            // - AWS SNS
            // - Google Cloud Messaging
            // - Local SMS gateway
            
            // For now, just simulate sending
            simulateSmsDispatch(phoneNumber, message);
            
        } catch (Exception e) {
            // Log the error but don't throw exception to avoid breaking the flow
            System.err.println("Failed to send SMS to " + phoneNumber + ": " + e.getMessage());
        }
    }
    
    private void simulateSmsDispatch(String phoneNumber, String message) {
        // Simulate SMS sending delay
        try {
            Thread.sleep(100); // 100ms delay to simulate network call
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("SMS sent to " + phoneNumber + ": " + message);
    }
    
    public boolean isSmsEnabled() {
        return smsEnabled;
    }
    
    // Method to validate phone number format before sending
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        // Basic phone number validation
        return phoneNumber.matches("^\\+?[1-9]\\d{1,14}$");
    }
}