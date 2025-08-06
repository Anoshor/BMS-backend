package com.bms.backend.service;

import com.bms.backend.entity.User;
import com.bms.backend.enums.DeviceType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    
    @Value("${jwt.secret:mySecretKey12345678901234567890123456789012345678901234567890}")
    private String jwtSecret;
    
    @Value("${jwt.access-token-expiration:900}") // 15 minutes
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token-expiration:2592000}") // 30 days
    private long refreshTokenExpiration;
    
    @Value("${jwt.issuer:bms-api}")
    private String issuer;
    
    @Value("${jwt.audience:bms-app}")
    private String audience;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getId().toString());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());
        claims.put("status", user.getAccountStatus().name());
        claims.put("verified", Map.of(
            "email", Boolean.TRUE.equals(user.getEmailVerified()),
            "phone", Boolean.TRUE.equals(user.getPhoneVerified())
        ));
        claims.put("permissions", getPermissionsForRole(user.getRole().name()));
        
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenExpiration, ChronoUnit.SECONDS);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .setIssuer(issuer)
                .setAudience(audience)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public String generateRefreshToken(User user, String deviceId, DeviceType deviceType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getId().toString());
        claims.put("type", "refresh");
        claims.put("device_id", deviceId);
        claims.put("device_type", deviceType.name());
        
        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenExpiration, ChronoUnit.SECONDS);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .setIssuer(issuer)
                .setAudience(audience)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }
    
    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }
    
    public String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }
    
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }
    
    public String extractDeviceId(String token) {
        return extractClaims(token).get("device_id", String.class);
    }
    
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }
    
    public boolean isValidToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return !isTokenExpired(token) && 
                   issuer.equals(claims.getIssuer()) &&
                   audience.equals(claims.getAudience());
        } catch (JwtException e) {
            return false;
        }
    }
    
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return "refresh".equals(claims.get("type", String.class));
        } catch (JwtException e) {
            return false;
        }
    }
    
    public boolean isAccessToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.get("type") == null; // Access tokens don't have type claim
        } catch (JwtException e) {
            return false;
        }
    }
    
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
    
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
    
    public String hashToken(String token) {
        // Simple hash for demonstration - in production use proper hashing
        return String.valueOf(token.hashCode());
    }
    
    private String[] getPermissionsForRole(String role) {
        return switch (role) {
            case "TENANT" -> new String[]{"read:profile", "write:profile", "read:properties", "create:rental_application"};
            case "PROPERTY_MANAGER" -> new String[]{"read:profile", "write:profile", "manage:properties", "manage:tenants", "read:analytics"};
            case "BUILDING_OWNER" -> new String[]{"read:profile", "write:profile", "manage:properties", "manage:managers", "read:analytics", "manage:billing"};
            default -> new String[]{"read:profile"};
        };
    }
    
    public UUID extractUserIdAsUUID(String token) {
        String userId = extractUserId(token);
        return UUID.fromString(userId);
    }
    
    public Instant extractIssuedAt(String token) {
        return extractClaims(token).getIssuedAt().toInstant();
    }
    
    public Instant extractExpiration(String token) {
        return extractClaims(token).getExpiration().toInstant();
    }
}