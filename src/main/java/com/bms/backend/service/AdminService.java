package com.bms.backend.service;

import com.bms.backend.dto.request.ManagerApprovalRequest;
import com.bms.backend.dto.response.ManagerApprovalDto;
import com.bms.backend.entity.ManagerProfile;
import com.bms.backend.entity.User;
import com.bms.backend.enums.AccountStatus;
import com.bms.backend.enums.UserRole;
import com.bms.backend.repository.ManagerProfileRepository;
import com.bms.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    @Autowired
    private ManagerProfileRepository managerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ManagerApprovalDto> getPendingManagers() {
        return managerProfileRepository.findByApprovalStatus("PENDING")
                .stream()
                .map(ManagerApprovalDto::from)
                .collect(Collectors.toList());
    }

    public List<ManagerApprovalDto> getAllManagers() {
        return managerProfileRepository.findAll()
                .stream()
                .map(ManagerApprovalDto::from)
                .collect(Collectors.toList());
    }

    public ManagerApprovalDto approveOrRejectManager(ManagerApprovalRequest request) {
        // Find manager by email
        User manager = userRepository.findByEmail(request.getManagerEmail())
                .orElseThrow(() -> new IllegalArgumentException("Manager not found with email: " + request.getManagerEmail()));

        if (manager.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("User is not a manager");
        }

        ManagerProfile managerProfile = managerProfileRepository.findByUser(manager)
                .orElseThrow(() -> new IllegalArgumentException("Manager profile not found"));

        // Update approval status
        if ("APPROVE".equals(request.getAction())) {
            managerProfile.setApprovalStatus("APPROVED");
            managerProfile.setAdminApproved(true);
            managerProfile.setApprovedBy(request.getAdminEmail());
            managerProfile.setApprovalDate(Instant.now());
            managerProfile.setRejectionReason(null);
            
            // Activate the user account
            manager.setAccountStatus(AccountStatus.ACTIVE);
            userRepository.save(manager);
            
        } else if ("REJECT".equals(request.getAction())) {
            managerProfile.setApprovalStatus("REJECTED");
            managerProfile.setAdminApproved(false);
            managerProfile.setApprovedBy(request.getAdminEmail());
            managerProfile.setApprovalDate(Instant.now());
            managerProfile.setRejectionReason(request.getRejectionReason());
            
            // Suspend the user account
            manager.setAccountStatus(AccountStatus.SUSPENDED);
            userRepository.save(manager);
        } else {
            throw new IllegalArgumentException("Invalid action. Must be APPROVE or REJECT");
        }

        ManagerProfile savedProfile = managerProfileRepository.save(managerProfile);
        return ManagerApprovalDto.from(savedProfile);
    }

    public ManagerApprovalDto getManagerStatus(String managerEmail) {
        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found with email: " + managerEmail));

        if (manager.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("User is not a manager");
        }

        ManagerProfile managerProfile = managerProfileRepository.findByUser(manager)
                .orElseThrow(() -> new IllegalArgumentException("Manager profile not found"));

        return ManagerApprovalDto.from(managerProfile);
    }
}