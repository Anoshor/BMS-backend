package com.bms.backend.service;

import com.bms.backend.dto.request.ManagerRegistrationRequest;
import com.bms.backend.dto.request.TenantRegistrationRequest;
import com.bms.backend.dto.response.UserDto;
import com.bms.backend.entity.ManagerProfile;
import com.bms.backend.entity.TenantProfile;
import com.bms.backend.entity.User;
import com.bms.backend.enums.AccountStatus;
import com.bms.backend.enums.UserRole;
import com.bms.backend.repository.ManagerProfileRepository;
import com.bms.backend.repository.TenantProfileRepository;
import com.bms.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TenantProfileRepository tenantProfileRepository;
    
    @Autowired
    private ManagerProfileRepository managerProfileRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private OtpService otpService;
    
    public User createTenantUser(TenantRegistrationRequest request) {
        validateTenantRegistration(request);
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhone(request.getContactNum());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.TENANT);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(request.getDob().toLocalDate());
        user.setAccountStatus(AccountStatus.PENDING);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        
        User savedUser = userRepository.save(user);
        
        // Create tenant profile
        TenantProfile profile = new TenantProfile(savedUser);
        tenantProfileRepository.save(profile);
        
        // Send verification OTPs
        otpService.generateAndSendEmailVerificationOtp(savedUser.getEmail());
        otpService.generateAndSendPhoneVerificationOtp(savedUser.getPhone());
        
        return savedUser;
    }
    
    public User createManagerUser(ManagerRegistrationRequest request) {
        validateManagerRegistration(request);
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhone(request.getContactNum());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.PROPERTY_MANAGER);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(request.getDob().toLocalDate());
        user.setAccountStatus(AccountStatus.PENDING);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        
        User savedUser = userRepository.save(user);
        
        // Create manager profile with property details
        ManagerProfile profile = new ManagerProfile(savedUser);
        profile.setCompanyName(request.getPropertyName());
        profile.setBusinessLicenseNumber(request.getPropertyManagerName());
        profile.setBusinessAddress(request.getPropertyAddress());
        profile.setBusinessPhone(request.getContactNum());
        profile.setBusinessEmail(request.getEmail());
        managerProfileRepository.save(profile);
        
        // Send verification OTPs
        otpService.generateAndSendEmailVerificationOtp(savedUser.getEmail());
        otpService.generateAndSendPhoneVerificationOtp(savedUser.getPhone());
        
        return savedUser;
    }
    
    public Optional<User> findByEmailOrPhone(String identifier) {
        return userRepository.findByEmailOrPhone(identifier);
    }
    
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }
    
    public boolean existsByEmailOrPhone(String email, String phone) {
        return userRepository.existsByEmailOrPhone(email, phone);
    }
    
    public User verifyEmail(String email, String otpCode) {
        // Verify OTP
        if (!otpService.verifyEmailOtp(email, otpCode)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        
        // Activate account if both email and phone are verified
        if (Boolean.TRUE.equals(user.getPhoneVerified())) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }
        
        return userRepository.save(user);
    }
    
    public User verifyPhone(String phone, String otpCode) {
        // Verify OTP
        if (!otpService.verifyPhoneOtp(phone, otpCode)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setPhoneVerified(true);
        user.setPhoneVerificationCode(null);
        
        // Activate account if both email and phone are verified
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }
        
        return userRepository.save(user);
    }
    
    public boolean validatePassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPasswordHash());
    }
    
    public User updateLastLogin(User user) {
        user.setLastLogin(Instant.now());
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        return userRepository.save(user);
    }
    
    public User incrementFailedLoginAttempts(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        
        // Lock account after 5 failed attempts for 30 minutes
        if (attempts >= 5) {
            user.setAccountLockedUntil(Instant.now().plus(30, ChronoUnit.MINUTES));
        }
        
        return userRepository.save(user);
    }
    
    public boolean isAccountLocked(User user) {
        return user.getAccountLockedUntil() != null && 
               user.getAccountLockedUntil().isAfter(Instant.now());
    }
    
    public UserDto getUserDto(User user) {
        return UserDto.from(user);
    }
    
    public void resendEmailVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalStateException("Email is already verified");
        }
        
        otpService.generateAndSendEmailVerificationOtp(email);
    }
    
    public void resendPhoneVerification(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (Boolean.TRUE.equals(user.getPhoneVerified())) {
            throw new IllegalStateException("Phone is already verified");
        }
        
        otpService.generateAndSendPhoneVerificationOtp(phone);
    }
    
    private void validateTenantRegistration(TenantRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        
        if (userRepository.existsByPhone(request.getContactNum())) {
            throw new IllegalArgumentException("Phone number is already registered");
        }
    }
    
    private void validateManagerRegistration(ManagerRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        
        if (userRepository.existsByPhone(request.getContactNum())) {
            throw new IllegalArgumentException("Phone number is already registered");
        }
        
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
    }
}