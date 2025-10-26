package com.bms.backend.config;

import com.bms.backend.security.JwtAuthenticationEntryPoint;
import com.bms.backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Authentication endpoints that require authentication
                        .requestMatchers("/api/v1/auth/profile").authenticated()

                        // Public authentication endpoints
                        .requestMatchers("/api/v1/auth/signup", "/api/v1/auth/login",
                                       "/api/v1/auth/refresh-token", "/api/v1/auth/logout",
                                       "/api/v1/auth/logout-all-devices", "/api/v1/auth/verify-email",
                                       "/api/v1/auth/verify-phone",
                                       "/api/v1/auth/manager/register", "/api/v1/auth/manager/verify-email", "/api/v1/auth/manager/verify-phone",
                                       "/api/v1/auth/manager/resend-email-verification", "/api/v1/auth/manager/resend-phone-verification",
                                       "/api/v1/auth/tenant/register", "/api/v1/auth/tenant/verify-email", "/api/v1/auth/tenant/verify-phone",
                                       "/api/v1/auth/tenant/resend-email-verification", "/api/v1/auth/tenant/resend-phone-verification").permitAll()

                        // Health check and monitoring
                        .requestMatchers("/api/v1/health").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // Payment recording endpoint (called from payment-service)
                        .requestMatchers("/api/v1/payments/record").permitAll()

                        // H2 Console (for development)
                        .requestMatchers("/h2-console/**").permitAll()

                        // API documentation (if using SpringDoc/Swagger)
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()

                        // Error endpoint
                        .requestMatchers("/error").permitAll()

                        // Tenant specific endpoints - Use hasRole which expects ROLE_ prefix
                        .requestMatchers("/api/v1/tenant/**").hasRole("TENANT")

                        // Manager specific endpoints
                        .requestMatchers("/api/v1/manager/**").hasAnyRole("PROPERTY_MANAGER", "BUILDING_OWNER")

                        // Building owner specific endpoints
                        .requestMatchers("/api/v1/owner/**").hasRole("BUILDING_OWNER")

                        // Admin endpoints - For now allow all (in production add proper admin auth)
                        .requestMatchers("/api/v1/admin/**").permitAll()

                        // Debug endpoints (development only)
                        .requestMatchers("/api/v1/debug/**").permitAll()

                        // Property endpoints (accessible by authenticated managers)
                        .requestMatchers("/api/v1/properties/**").authenticated()

                        // Tenant endpoints (accessible by authenticated managers and tenants)
                        .requestMatchers("/api/v1/tenants/**").authenticated()

                        // All other requests need authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                );

        // Allow H2 console frames (for development)
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow all origins in development (restrict in production)
        configuration.setAllowedOriginPatterns(List.of("*"));

        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Expose authorization header to frontend
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength 12 for better security
    }
}