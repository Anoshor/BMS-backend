package com.bms.backend.service;

import com.bms.backend.dto.request.PropertyDetailsRequest;
import com.bms.backend.entity.ManagerProfile;
import com.bms.backend.entity.TenantPropertyConnection;
import com.bms.backend.entity.User;
import com.bms.backend.enums.UserRole;
import com.bms.backend.repository.ManagerProfileRepository;
import com.bms.backend.repository.TenantPropertyConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PropertyService {

    @Autowired
    private ManagerProfileRepository managerProfileRepository;

    @Autowired
    private TenantPropertyConnectionRepository connectionRepository;

    public void addPropertyDetails(User user, PropertyDetailsRequest request) {
        if (user.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only managers can add property details");
        }

        ManagerProfile profile = managerProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Manager profile not found"));

        updateManagerProfileWithPropertyDetails(profile, request);
        managerProfileRepository.save(profile);
    }

    public void updatePropertyDetails(User user, PropertyDetailsRequest request) {
        if (user.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only managers can update property details");
        }

        ManagerProfile profile = managerProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Manager profile not found"));

        updateManagerProfileWithPropertyDetails(profile, request);
        managerProfileRepository.save(profile);
    }

    private void updateManagerProfileWithPropertyDetails(ManagerProfile profile, PropertyDetailsRequest request) {
        profile.setCompanyName(request.getPropertyName());
        profile.setBusinessLicenseNumber(request.getPropertyManagerName());
        profile.setBusinessAddress(request.getPropertyAddress());
        profile.setProfileCompleted(true);
    }

    public List<ManagerProfile> getUnoccupiedProperties(User manager) {
        if (manager.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only managers can view their properties");
        }

        // Get manager profile
        ManagerProfile managerProfile = managerProfileRepository.findByUser(manager)
                .orElseThrow(() -> new IllegalArgumentException("Manager profile not found"));

        // For now, return the manager's properties
        // In a real implementation, you would have a separate Property entity
        // and check which ones are not occupied based on TenantPropertyConnection
        List<TenantPropertyConnection> occupiedProperties = connectionRepository.findByManagerAndIsActive(manager, true);
        
        // For simplicity, just return manager profile if no properties are occupied
        // In a real system, you'd have a proper Property entity
        return List.of(managerProfile);
    }
}