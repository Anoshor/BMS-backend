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
                        .requestMatchers("/auth/profile").authenticated()
                        
                        // Public authentication endpoints
                        // Note: context-path is /api/v1, so actual paths are relative to that
                        .requestMatchers("/auth/signup", "/auth/login", 
                                       "/auth/refresh-token", "/auth/logout",
                                       "/auth/logout-all-devices", "/auth/verify-email",
                                       "/auth/verify-phone",
                                       "/auth/manager/register", "/auth/manager/verify-email", "/auth/manager/verify-phone",
                                       "/auth/manager/resend-email-verification", "/auth/manager/resend-phone-verification",
                                       "/auth/tenant/register", "/auth/tenant/verify-email", "/auth/tenant/verify-phone", 
                                       "/auth/tenant/resend-email-verification", "/auth/tenant/resend-phone-verification").permitAll()

                        // Health check and monitoring
                        .requestMatchers("/health").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // H2 Console (for development)
                        .requestMatchers("/h2-console/**").permitAll()

                        // API documentation (if using SpringDoc/Swagger)
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()

                        // Error endpoint
                        .requestMatchers("/error").permitAll()

                        // Tenant specific endpoints - Use hasRole which expects ROLE_ prefix
                        .requestMatchers("/tenant/**").hasRole("TENANT")

                        // Manager specific endpoints
                        .requestMatchers("/manager/**").hasAnyRole("PROPERTY_MANAGER", "BUILDING_OWNER")

                        // Building owner specific endpoints
                        .requestMatchers("/owner/**").hasRole("BUILDING_OWNER")

                        // Admin endpoints - For now allow all (in production add proper admin auth)
                        .requestMatchers("/admin/**").permitAll()
                        
                        // Property endpoints (accessible by authenticated managers)
                        .requestMatchers("/properties/**").authenticated()
                        
                        // Tenant endpoints (accessible by authenticated managers and tenants)
                        .requestMatchers("/tenants/**").authenticated()

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