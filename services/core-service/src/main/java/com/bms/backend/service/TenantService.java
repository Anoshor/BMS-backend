package com.bms.backend.service;

import com.bms.backend.dto.request.ConnectTenantRequest;
import com.bms.backend.entity.Property;
import com.bms.backend.entity.TenantPropertyConnection;
import com.bms.backend.entity.User;
import com.bms.backend.enums.UserRole;
import com.bms.backend.repository.PropertyRepository;
import com.bms.backend.repository.TenantPropertyConnectionRepository;
import com.bms.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TenantService {

    @Autowired
    private TenantPropertyConnectionRepository connectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    public TenantPropertyConnection connectTenantToProperty(User manager, ConnectTenantRequest request) {
        // Validate manager
        if (manager.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only managers can connect tenants to properties");
        }

        // Find tenant by email
        User tenant = userRepository.findByEmail(request.getTenantEmail())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with email: " + request.getTenantEmail()));

        if (tenant.getRole() != UserRole.TENANT) {
            throw new IllegalArgumentException("User is not a tenant");
        }

        // Check if tenant is already connected to this property
        if (connectionRepository.existsByTenantAndPropertyNameAndIsActive(tenant, request.getPropertyName(), true)) {
            throw new IllegalArgumentException("Tenant is already connected to this property");
        }

        // Check if property exists for this manager
        Property property = propertyRepository.findByPropertyNameAndManager(request.getPropertyName(), manager)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + request.getPropertyName()));

        // Create connection
        TenantPropertyConnection connection = new TenantPropertyConnection(
                tenant, manager, request.getPropertyName(),
                request.getStartDate(), request.getEndDate(), request.getMonthlyRent()
        );
        connection.setSecurityDeposit(request.getSecurityDeposit());
        connection.setNotes(request.getNotes());

        return connectionRepository.save(connection);
    }

    public List<TenantPropertyConnection> searchTenants(User manager, String searchText) {
        if (manager.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only managers can search tenants");
        }

        if (searchText == null || searchText.trim().isEmpty()) {
            return connectionRepository.findByManagerAndIsActive(manager, true);
        }

        return connectionRepository.findByManagerAndSearchText(manager, searchText.trim());
    }

    public List<TenantPropertyConnection> getTenantProperties(User tenant) {
        if (tenant.getRole() != UserRole.TENANT) {
            throw new IllegalArgumentException("Only tenants can view their properties");
        }

        return connectionRepository.findByTenantAndIsActive(tenant, true);
    }
}