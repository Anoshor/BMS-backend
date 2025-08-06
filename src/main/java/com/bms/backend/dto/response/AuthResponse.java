package com.bms.backend.dto.response;

public class AuthResponse {
    
    private String accessToken;
    private String refreshToken;
    private UserDto user;
    private boolean requiresVerification;
    private boolean requiresDocuments;
    private long expiresIn; // seconds
    private String tokenType = "Bearer";
    
    // Constructors
    public AuthResponse() {}
    
    public AuthResponse(String accessToken, String refreshToken, UserDto user, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.expiresIn = expiresIn;
    }
    
    // Builder pattern
    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }
    
    public static class AuthResponseBuilder {
        private String accessToken;
        private String refreshToken;
        private UserDto user;
        private boolean requiresVerification;
        private boolean requiresDocuments;
        private long expiresIn;
        
        public AuthResponseBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }
        
        public AuthResponseBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }
        
        public AuthResponseBuilder user(UserDto user) {
            this.user = user;
            return this;
        }
        
        public AuthResponseBuilder requiresVerification(boolean requiresVerification) {
            this.requiresVerification = requiresVerification;
            return this;
        }
        
        public AuthResponseBuilder requiresDocuments(boolean requiresDocuments) {
            this.requiresDocuments = requiresDocuments;
            return this;
        }
        
        public AuthResponseBuilder expiresIn(long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }
        
        public AuthResponse build() {
            AuthResponse response = new AuthResponse();
            response.accessToken = this.accessToken;
            response.refreshToken = this.refreshToken;
            response.user = this.user;
            response.requiresVerification = this.requiresVerification;
            response.requiresDocuments = this.requiresDocuments;
            response.expiresIn = this.expiresIn;
            return response;
        }
    }
    
    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public UserDto getUser() {
        return user;
    }
    
    public void setUser(UserDto user) {
        this.user = user;
    }
    
    public boolean isRequiresVerification() {
        return requiresVerification;
    }
    
    public void setRequiresVerification(boolean requiresVerification) {
        this.requiresVerification = requiresVerification;
    }
    
    public boolean isRequiresDocuments() {
        return requiresDocuments;
    }
    
    public void setRequiresDocuments(boolean requiresDocuments) {
        this.requiresDocuments = requiresDocuments;
    }
    
    public long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}