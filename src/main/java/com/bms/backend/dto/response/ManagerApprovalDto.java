package com.bms.backend.dto.response;

import com.bms.backend.entity.ManagerProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerApprovalDto {

    private String managerId;
    private String managerEmail;
    private String firstName;
    private String lastName;
    private String companyName;
    private String businessAddress;
    private String approvalStatus;
    private Boolean adminApproved;
    private String approvedBy;
    private Instant approvalDate;
    private String rejectionReason;
    private Instant createdAt;

    public static ManagerApprovalDto from(ManagerProfile profile) {
        return ManagerApprovalDto.builder()
                .managerId(profile.getUser().getId().toString())
                .managerEmail(profile.getUser().getEmail())
                .firstName(profile.getUser().getFirstName())
                .lastName(profile.getUser().getLastName())
                .companyName(profile.getCompanyName())
                .businessAddress(profile.getBusinessAddress())
                .approvalStatus(profile.getApprovalStatus())
                .adminApproved(profile.getAdminApproved())
                .approvedBy(profile.getApprovedBy())
                .approvalDate(profile.getApprovalDate())
                .rejectionReason(profile.getRejectionReason())
                .createdAt(profile.getCreatedAt())
                .build();
    }
}