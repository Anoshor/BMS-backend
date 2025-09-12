package com.bms.backend.dto.response;

import com.bms.backend.entity.User;

import java.time.Instant;
import java.time.LocalDate;

public class UserDto {
    
    private String id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String role;
    private String accountStatus;
    private boolean emailVerified;
    private boolean phoneVerified;
    private String profileImageUrl;
    private LocalDate dateOfBirth;
    private String gender;
    private Instant createdAt;
    private Instant lastLogin;
    
    // Constructors
    public UserDto() {}
    
    // Static factory method
    public static UserDto from(User user) {
        UserDto dto = new UserDto();
        dto.id = user.getId().toString();
        dto.email = user.getEmail();
        dto.phone = user.getPhone();
        dto.firstName = user.getFirstName();
        dto.lastName = user.getLastName();
        dto.role = user.getRole().name().toLowerCase();
        dto.accountStatus = user.getAccountStatus().name().toLowerCase();
        dto.emailVerified = Boolean.TRUE.equals(user.getEmailVerified());
        dto.phoneVerified = Boolean.TRUE.equals(user.getPhoneVerified());
        dto.profileImageUrl = user.getProfileImageUrl();
        dto.dateOfBirth = user.getDateOfBirth();
        dto.gender = user.getGender();
        dto.createdAt = user.getCreatedAt();
        dto.lastLogin = user.getLastLogin();
        return dto;
    }
    
    // Builder pattern
    public static UserDtoBuilder builder() {
        return new UserDtoBuilder();
    }
    
    public static class UserDtoBuilder {
        private String id;
        private String email;
        private String phone;
        private String firstName;
        private String lastName;
        private String role;
        private String accountStatus;
        private boolean emailVerified;
        private boolean phoneVerified;
        private String profileImageUrl;
        private LocalDate dateOfBirth;
        private String gender;
        private Instant createdAt;
        private Instant lastLogin;
        
        public UserDtoBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public UserDtoBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserDtoBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public UserDtoBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        
        public UserDtoBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        public UserDtoBuilder role(String role) {
            this.role = role;
            return this;
        }
        
        public UserDtoBuilder accountStatus(String accountStatus) {
            this.accountStatus = accountStatus;
            return this;
        }
        
        public UserDtoBuilder emailVerified(boolean emailVerified) {
            this.emailVerified = emailVerified;
            return this;
        }
        
        public UserDtoBuilder phoneVerified(boolean phoneVerified) {
            this.phoneVerified = phoneVerified;
            return this;
        }
        
        public UserDtoBuilder profileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
            return this;
        }
        
        public UserDtoBuilder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }
        
        public UserDtoBuilder gender(String gender) {
            this.gender = gender;
            return this;
        }
        
        public UserDtoBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public UserDtoBuilder lastLogin(Instant lastLogin) {
            this.lastLogin = lastLogin;
            return this;
        }
        
        public UserDto build() {
            UserDto dto = new UserDto();
            dto.id = this.id;
            dto.email = this.email;
            dto.phone = this.phone;
            dto.firstName = this.firstName;
            dto.lastName = this.lastName;
            dto.role = this.role;
            dto.accountStatus = this.accountStatus;
            dto.emailVerified = this.emailVerified;
            dto.phoneVerified = this.phoneVerified;
            dto.profileImageUrl = this.profileImageUrl;
            dto.dateOfBirth = this.dateOfBirth;
            dto.gender = this.gender;
            dto.createdAt = this.createdAt;
            dto.lastLogin = this.lastLogin;
            return dto;
        }
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getAccountStatus() {
        return accountStatus;
    }
    
    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
    
    public boolean isEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public boolean isPhoneVerified() {
        return phoneVerified;
    }
    
    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }
    
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(Instant lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isVerificationComplete() {
        return emailVerified && phoneVerified;
    }
}