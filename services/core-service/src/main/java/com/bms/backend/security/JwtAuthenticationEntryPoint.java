package com.bms.backend.security;

import com.bms.backend.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public void commence(HttpServletRequest request, 
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        
        // Set response status and content type
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        // Determine the appropriate error message
        String message = determineErrorMessage(request, authException);
        
        // Create error response
        ApiResponse<Object> errorResponse = ApiResponse.error(message);
        errorResponse.setPath(request.getRequestURI());
        
        // Write response
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
    
    private String determineErrorMessage(HttpServletRequest request, AuthenticationException authException) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null) {
            return "Authentication required. Please provide a valid access token.";
        }
        
        if (!authHeader.startsWith("Bearer ")) {
            return "Invalid authorization header format. Expected 'Bearer <token>'.";
        }
        
        // Check if token is expired or invalid
        String message = authException.getMessage();
        if (message != null) {
            if (message.contains("expired")) {
                return "Access token has expired. Please refresh your token.";
            }
            if (message.contains("invalid")) {
                return "Invalid access token. Please login again.";
            }
        }
        
        return "Authentication failed. Please login again.";
    }
}