package com.bms.backend.security;

import com.bms.backend.entity.User;
import com.bms.backend.service.JwtService;
import com.bms.backend.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip JWT validation for public endpoints
        String path = request.getRequestURI();
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userId;

        // Check if Authorization header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token
        jwt = authHeader.substring(7);

        try {
            // Extract user ID from JWT
            userId = jwtService.extractUserId(jwt);

            // If user ID exists and no authentication is set in context
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Validate token
                if (jwtService.isValidToken(jwt) && jwtService.isAccessToken(jwt)) {

                    // Load user from database
                    Optional<User> userOpt = userService.findById(UUID.fromString(userId));

                    if (userOpt.isPresent()) {
                        User user = userOpt.get();

                        // Check if account is active and not locked
                        if (user.isAccountActive() && user.isAccountNonLocked()) {

                            // Create authorities based on user role
                            // Important: Use the role name directly without ROLE_ prefix
                            // Spring Security will add it automatically
                            List<SimpleGrantedAuthority> authorities = List.of(
                                    new SimpleGrantedAuthority(user.getRole().name())
                            );

                            // Create authentication token
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(
                                            user,
                                            null,
                                            authorities
                                    );

                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            // Set authentication in security context
                            SecurityContextHolder.getContext().setAuthentication(authToken);

                            logger.debug("Successfully authenticated user: {} with role: {}",
                                    user.getEmail(), user.getRole());
                        } else {
                            logger.warn("User account is inactive or locked: {}", user.getEmail());
                        }
                    } else {
                        logger.warn("User not found with ID: {}", userId);
                    }
                } else {
                    logger.warn("Invalid or expired token");
                }
            }
        } catch (Exception e) {
            // Log the error and continue (don't block the request)
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        // Note: context-path is /api/v1, so actual paths are relative to that
        return (path.startsWith("/auth/") && 
                !path.equals("/auth/profile")) ||
                path.equals("/health") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.equals("/swagger-ui.html") ||
                path.startsWith("/h2-console") ||
                path.equals("/error");
    }
}