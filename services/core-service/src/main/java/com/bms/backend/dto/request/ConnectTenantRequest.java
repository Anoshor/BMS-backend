package com.bms.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.bms.backend.config.FlexibleDateDeserializer;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConnectTenantRequest {

    @NotBlank(message = "Tenant email is required")
    @Email(message = "Invalid email format")
    private String tenantEmail;

    @NotNull(message = "Apartment ID is required")
    private UUID apartmentId;

    @NotNull(message = "Start date is required")
    @JsonDeserialize(using = FlexibleDateDeserializer.class)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    @JsonDeserialize(using = FlexibleDateDeserializer.class)
    private LocalDate endDate;

    @NotNull(message = "Monthly rent is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Monthly rent must be positive")
    private Double monthlyRent;

    @NotNull(message = "Security deposit is required")
    @DecimalMin(value = "0.0", message = "Security deposit cannot be negative")
    private Double securityDeposit;

    private String notes;

    // Manual getter methods to fix Lombok compilation issues
    public String getTenantEmail() {
        return tenantEmail;
    }

    public UUID getApartmentId() {
        return apartmentId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Double getMonthlyRent() {
        return monthlyRent;
    }

    public Double getSecurityDeposit() {
        return securityDeposit;
    }

    public String getNotes() {
        return notes;
    }
}