package com.bms.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerApprovalRequest {

    @NotBlank(message = "Manager email is required")
    @Email(message = "Invalid email format")
    private String managerEmail;

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "^(APPROVE|REJECT)$", message = "Action must be APPROVE or REJECT")
    private String action;

    private String rejectionReason; // Required if action is REJECT

    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid admin email format")
    private String adminEmail;

    // Manual getter methods to fix Lombok compilation issues
    public String getManagerEmail() {
        return managerEmail;
    }

    public String getAction() {
        return action;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getAdminEmail() {
        return adminEmail;
    }
}