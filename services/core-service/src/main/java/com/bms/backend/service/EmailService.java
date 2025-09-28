package com.bms.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.from:noreply@bms.com}")
    private String fromEmail;
    
    @Value("${app.name:Building Management System}")
    private String appName;
    
    public void sendVerificationEmail(String toEmail, String otpCode) {
        String subject = "Email Verification - " + appName;
        String body = String.format(
            "Hello,\n\n" +
            "Your email verification code for %s is: %s\n\n" +
            "This code will expire in 10 minutes.\n\n" +
            "If you didn't request this verification, please ignore this email.\n\n" +
            "Best regards,\n" +
            "%s Team",
            appName, otpCode, appName
        );
        
        sendEmail(toEmail, subject, body);
    }
    
    public void sendPasswordResetEmail(String toEmail, String otpCode) {
        String subject = "Password Reset - " + appName;
        String body = String.format(
            "Hello,\n\n" +
            "Your password reset code for %s is: %s\n\n" +
            "This code will expire in 10 minutes.\n\n" +
            "If you didn't request a password reset, please ignore this email and ensure your account is secure.\n\n" +
            "Best regards,\n" +
            "%s Team",
            appName, otpCode, appName
        );
        
        sendEmail(toEmail, subject, body);
    }
    
    public void sendWelcomeEmail(String toEmail, String userName, String userRole) {
        String subject = "Welcome to " + appName;
        String body = String.format(
            "Hello %s,\n\n" +
            "Welcome to %s!\n\n" +
            "Your %s account has been successfully created and verified.\n\n" +
            "You can now start using the platform to %s.\n\n" +
            "If you have any questions, please don't hesitate to contact our support team.\n\n" +
            "Best regards,\n" +
            "%s Team",
            userName, appName, userRole.toLowerCase(),
            getRoleDescription(userRole), appName
        );
        
        sendEmail(toEmail, subject, body);
    }
    
    public void sendAccountLockedEmail(String toEmail, String userName) {
        String subject = "Account Security Alert - " + appName;
        String body = String.format(
            "Hello %s,\n\n" +
            "Your account has been temporarily locked due to multiple failed login attempts.\n\n" +
            "Your account will be automatically unlocked in 30 minutes.\n\n" +
            "If this wasn't you, please contact our support team immediately.\n\n" +
            "Best regards,\n" +
            "%s Team",
            userName, appName
        );
        
        sendEmail(toEmail, subject, body);
    }
    
    private void sendEmail(String toEmail, String subject, String body) {
        if (mailSender == null) {
            System.out.println("Mail not configured - Email would be sent to: " + toEmail + " with subject: " + subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
        } catch (Exception e) {
            // Log the error but don't throw exception to avoid breaking the registration flow
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
        }
    }
    
    private String getRoleDescription(String role) {
        return switch (role.toUpperCase()) {
            case "TENANT" -> "search for properties, apply for rentals, and manage your tenancy";
            case "PROPERTY_MANAGER" -> "manage properties, handle tenant requests, and oversee building operations";
            case "BUILDING_OWNER" -> "manage your properties, oversee managers, and track your investments";
            default -> "use our platform";
        };
    }
}