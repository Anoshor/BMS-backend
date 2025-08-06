package com.bms.backend.service;

import com.bms.backend.entity.OtpVerification;
import com.bms.backend.enums.OtpType;
import com.bms.backend.repository.OtpVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@Transactional
public class OtpService {
    
    @Autowired
    private OtpVerificationRepository otpRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;
    
    private final SecureRandom random = new SecureRandom();
    private final int OTP_LENGTH = 6;
    private final int OTP_EXPIRY_MINUTES = 10;
    private final int MAX_ATTEMPTS = 3;
    
    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
    
    public void generateAndSendEmailVerificationOtp(String email) {
        String otpCode = generateOtp();
        Instant expiresAt = Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES);
        
        // Delete any existing OTP for this email and type
        otpRepository.deleteByIdentifierAndOtpType(email, OtpType.EMAIL_VERIFICATION);
        
        // Create new OTP
        OtpVerification otp = new OtpVerification(email, otpCode, OtpType.EMAIL_VERIFICATION, expiresAt);
        otpRepository.save(otp);
        
        // Send email
        emailService.sendVerificationEmail(email, otpCode);
    }
    
    public void generateAndSendPhoneVerificationOtp(String phone) {
        String otpCode = generateOtp();
        Instant expiresAt = Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES);
        
        // Delete any existing OTP for this phone and type
        otpRepository.deleteByIdentifierAndOtpType(phone, OtpType.PHONE_VERIFICATION);
        
        // Create new OTP
        OtpVerification otp = new OtpVerification(phone, otpCode, OtpType.PHONE_VERIFICATION, expiresAt);
        otpRepository.save(otp);
        
        // Send SMS
        smsService.sendVerificationSms(phone, otpCode);
    }
    
    public void generateAndSendPasswordResetOtp(String email) {
        String otpCode = generateOtp();
        Instant expiresAt = Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES);
        
        // Delete any existing OTP for this email and type
        otpRepository.deleteByIdentifierAndOtpType(email, OtpType.PASSWORD_RESET);
        
        // Create new OTP
        OtpVerification otp = new OtpVerification(email, otpCode, OtpType.PASSWORD_RESET, expiresAt);
        otpRepository.save(otp);
        
        // Send email
        emailService.sendPasswordResetEmail(email, otpCode);
    }
    
    public boolean verifyEmailOtp(String email, String otpCode) {
        return verifyOtp(email, otpCode, OtpType.EMAIL_VERIFICATION);
    }
    
    public boolean verifyPhoneOtp(String phone, String otpCode) {
        return verifyOtp(phone, otpCode, OtpType.PHONE_VERIFICATION);
    }
    
    public boolean verifyPasswordResetOtp(String email, String otpCode) {
        return verifyOtp(email, otpCode, OtpType.PASSWORD_RESET);
    }
    
    private boolean verifyOtp(String identifier, String otpCode, OtpType otpType) {
        Optional<OtpVerification> otpOpt = otpRepository.findByIdentifierAndOtpType(identifier, otpType);
        
        if (otpOpt.isEmpty()) {
            return false;
        }
        
        OtpVerification otp = otpOpt.get();
        
        // Check if OTP can be attempted
        if (!otp.canAttempt()) {
            return false;
        }
        
        // Increment attempts
        otp.incrementAttempts();
        otpRepository.save(otp);
        
        // Verify OTP code
        if (!otp.getOtpCode().equals(otpCode)) {
            return false;
        }
        
        // Mark as used
        otp.markAsUsed();
        otpRepository.save(otp);
        
        return true;
    }
    
    public boolean isOtpValid(String identifier, String otpCode, OtpType otpType) {
        Instant now = Instant.now();
        Optional<OtpVerification> otpOpt = otpRepository.findValidOtpByIdentifierCodeAndType(
            identifier, otpCode, otpType, now);
        
        return otpOpt.isPresent() && otpOpt.get().canAttempt();
    }
    
    public void resendOtp(String identifier, OtpType otpType) {
        // Check rate limiting - max 3 OTP requests per hour
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        long recentAttempts = otpRepository.countOtpAttemptsSince(identifier, otpType, oneHourAgo);
        
        if (recentAttempts >= 3) {
            throw new IllegalStateException("Too many OTP requests. Please try again later.");
        }
        
        switch (otpType) {
            case EMAIL_VERIFICATION -> generateAndSendEmailVerificationOtp(identifier);
            case PHONE_VERIFICATION -> generateAndSendPhoneVerificationOtp(identifier);
            case PASSWORD_RESET -> generateAndSendPasswordResetOtp(identifier);
            default -> throw new IllegalArgumentException("Unsupported OTP type for resend: " + otpType);
        }
    }
    
    public void cleanupExpiredOtps() {
        Instant cutoffDate = Instant.now().minus(1, ChronoUnit.DAYS);
        otpRepository.deleteExpiredOtpsOlderThan(cutoffDate);
        otpRepository.deleteUsedOtpsOlderThan(cutoffDate);
    }
    
    public long getRemainingAttempts(String identifier, OtpType otpType) {
        Optional<OtpVerification> otpOpt = otpRepository.findByIdentifierAndOtpType(identifier, otpType);
        
        if (otpOpt.isEmpty()) {
            return 0;
        }
        
        OtpVerification otp = otpOpt.get();
        return Math.max(0, MAX_ATTEMPTS - otp.getAttempts());
    }
    
    public Instant getOtpExpiry(String identifier, OtpType otpType) {
        Optional<OtpVerification> otpOpt = otpRepository.findByIdentifierAndOtpType(identifier, otpType);
        
        if (otpOpt.isEmpty()) {
            return null;
        }
        
        return otpOpt.get().getExpiresAt();
    }
}