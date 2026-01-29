package com.bms.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${spring.mail.from:noreply@bms.com}")
    private String fromEmail;

    @Value("${app.name:Building Management System}")
    private String appName;

    @Value("${email.enabled:false}")
    private boolean emailEnabled;

    @Value("${brevo.api-key:}")
    private String brevoApiKey;

    @Value("${brevo.sender-email:noreply@bms.com}")
    private String brevoSenderEmail;

    @Value("${brevo.sender-name:Building Management System}")
    private String brevoSenderName;

    public void sendVerificationEmail(String toEmail, String otpCode) {
        String subject = "Email Verification - " + appName;
        String textBody = String.format(
            "Hello,\n\n" +
            "Your email verification code for %s is: %s\n\n" +
            "This code will expire in 10 minutes.\n\n" +
            "If you didn't request this verification, please ignore this email.\n\n" +
            "Best regards,\n" +
            "%s Team",
            appName, otpCode, appName
        );

        String htmlBody = buildOtpEmailHtml("Email Verification", otpCode,
            "Your email verification code for " + appName + " is:",
            "This code will expire in 10 minutes.");

        sendEmail(toEmail, subject, textBody, htmlBody);
    }

    public void sendPasswordResetEmail(String toEmail, String otpCode) {
        String subject = "Password Reset - " + appName;
        String textBody = String.format(
            "Hello,\n\n" +
            "Your password reset code for %s is: %s\n\n" +
            "This code will expire in 10 minutes.\n\n" +
            "If you didn't request a password reset, please ignore this email and ensure your account is secure.\n\n" +
            "Best regards,\n" +
            "%s Team",
            appName, otpCode, appName
        );

        String htmlBody = buildOtpEmailHtml("Password Reset", otpCode,
            "Your password reset code for " + appName + " is:",
            "This code will expire in 10 minutes. If you didn't request this, please secure your account.");

        sendEmail(toEmail, subject, textBody, htmlBody);
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

        sendEmail(toEmail, subject, body, null);
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

        sendEmail(toEmail, subject, body, null);
    }

    private void sendEmail(String toEmail, String subject, String textBody, String htmlBody) {
        // Use Brevo when enabled and API key is configured
        if (emailEnabled && brevoApiKey != null && !brevoApiKey.isEmpty()) {
            sendViaBrevo(toEmail, subject, textBody, htmlBody);
            return;
        }

        // Fallback to Spring Mail if configured
        if (mailSender != null) {
            sendViaSpringMail(toEmail, subject, textBody);
            return;
        }

        // Development mode - log the email
        logEmailToConsole(toEmail, subject, textBody);
    }

    private void sendViaBrevo(String toEmail, String subject, String textBody, String htmlBody) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> sender = new HashMap<>();
            sender.put("email", brevoSenderEmail);
            sender.put("name", brevoSenderName);

            Map<String, Object> recipient = new HashMap<>();
            recipient.put("email", toEmail);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", sender);
            body.put("to", List.of(recipient));
            body.put("subject", subject);
            body.put("textContent", textBody);
            if (htmlBody != null) {
                body.put("htmlContent", htmlBody);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(BREVO_API_URL, request, String.class);

            System.out.println("Email sent successfully via Brevo to: " + toEmail);

        } catch (Exception e) {
            System.err.println("Brevo failed, falling back to console log: " + e.getMessage());
            logEmailToConsole(toEmail, subject, textBody);
        }
    }

    private void logEmailToConsole(String toEmail, String subject, String body) {
        System.out.println("=== EMAIL (Dev/Fallback) ===");
        System.out.println("To: " + toEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        System.out.println("============================");
    }

    private void sendViaSpringMail(String toEmail, String subject, String textBody) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(textBody);

            mailSender.send(message);
            System.out.println("Email sent successfully via Spring Mail to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Spring Mail failed, falling back to console log: " + e.getMessage());
            logEmailToConsole(toEmail, subject, textBody);
        }
    }

    private String buildOtpEmailHtml(String title, String otpCode, String message, String expiry) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: 0 auto; background-color: #ffffff;">
                    <tr>
                        <td style="padding: 40px 30px; text-align: center; background-color: #2F61D5;">
                            <h1 style="color: #ffffff; margin: 0; font-size: 24px;">%s</h1>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 40px 30px;">
                            <p style="color: #333333; font-size: 16px; line-height: 24px; margin-bottom: 20px;">
                                Hello,
                            </p>
                            <p style="color: #333333; font-size: 16px; line-height: 24px; margin-bottom: 30px;">
                                %s
                            </p>
                            <div style="text-align: center; margin: 30px 0;">
                                <div style="display: inline-block; background-color: #f8f9fa; border: 2px dashed #2F61D5; border-radius: 8px; padding: 20px 40px;">
                                    <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #2F61D5;">%s</span>
                                </div>
                            </div>
                            <p style="color: #666666; font-size: 14px; line-height: 22px; margin-top: 30px;">
                                %s
                            </p>
                            <p style="color: #666666; font-size: 14px; line-height: 22px;">
                                If you didn't request this, please ignore this email.
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 20px 30px; text-align: center; background-color: #f8f9fa; border-top: 1px solid #eeeeee;">
                            <p style="color: #999999; font-size: 12px; margin: 0;">
                                %s Team
                            </p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """, title, message, otpCode, expiry, appName);
    }

    private String getRoleDescription(String role) {
        return switch (role.toUpperCase()) {
            case "TENANT" -> "search for properties, apply for rentals, and manage your tenancy";
            case "PROPERTY_MANAGER" -> "manage properties, handle tenant requests, and oversee building operations";
            case "BUILDING_OWNER" -> "manage your properties, oversee managers, and track your investments";
            default -> "use our platform";
        };
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }
}
