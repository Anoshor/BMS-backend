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

    // Manual builder pattern to fix Lombok compilation issues
    public static ManagerApprovalDtoBuilder builder() {
        return new ManagerApprovalDtoBuilder();
    }

    public static class ManagerApprovalDtoBuilder {
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

        public ManagerApprovalDtoBuilder managerId(String managerId) {
            this.managerId = managerId;
            return this;
        }

        public ManagerApprovalDtoBuilder managerEmail(String managerEmail) {
            this.managerEmail = managerEmail;
            return this;
        }

        public ManagerApprovalDtoBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public ManagerApprovalDtoBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public ManagerApprovalDtoBuilder companyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public ManagerApprovalDtoBuilder businessAddress(String businessAddress) {
            this.businessAddress = businessAddress;
            return this;
        }

        public ManagerApprovalDtoBuilder approvalStatus(String approvalStatus) {
            this.approvalStatus = approvalStatus;
            return this;
        }

        public ManagerApprovalDtoBuilder adminApproved(Boolean adminApproved) {
            this.adminApproved = adminApproved;
            return this;
        }

        public ManagerApprovalDtoBuilder approvedBy(String approvedBy) {
            this.approvedBy = approvedBy;
            return this;
        }

        public ManagerApprovalDtoBuilder approvalDate(Instant approvalDate) {
            this.approvalDate = approvalDate;
            return this;
        }

        public ManagerApprovalDtoBuilder rejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
            return this;
        }

        public ManagerApprovalDtoBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ManagerApprovalDto build() {
            ManagerApprovalDto dto = new ManagerApprovalDto();
            dto.managerId = this.managerId;
            dto.managerEmail = this.managerEmail;
            dto.firstName = this.firstName;
            dto.lastName = this.lastName;
            dto.companyName = this.companyName;
            dto.businessAddress = this.businessAddress;
            dto.approvalStatus = this.approvalStatus;
            dto.adminApproved = this.adminApproved;
            dto.approvedBy = this.approvedBy;
            dto.approvalDate = this.approvalDate;
            dto.rejectionReason = this.rejectionReason;
            dto.createdAt = this.createdAt;
            return dto;
        }
    }

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

    // Manual getter methods for key fields
    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public Boolean getAdminApproved() {
        return adminApproved;
    }

    public void setAdminApproved(Boolean adminApproved) {
        this.adminApproved = adminApproved;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Instant getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Instant approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}