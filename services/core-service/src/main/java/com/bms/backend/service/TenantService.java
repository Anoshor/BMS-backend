package com.bms.backend.service;

import com.bms.backend.dto.request.ConnectTenantRequest;
import com.bms.backend.dto.response.TenantConnectionDto;
import com.bms.backend.entity.Apartment;
import com.bms.backend.entity.PropertyBuilding;
import com.bms.backend.entity.TenantPropertyConnection;
import com.bms.backend.entity.User;
import com.bms.backend.enums.UserRole;
import com.bms.backend.repository.ApartmentRepository;
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
    
    @Autowired
    private ApartmentRepository apartmentRepository;

    public TenantPropertyConnection connectTenantToProperty(User manager, ConnectTenantRequest request) {
        // Validate manager
        if (manager.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only managers can connect tenants to properties");
        }
        
        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Find tenant by email
        User tenant = userRepository.findByEmail(request.getTenantEmail())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with email: " + request.getTenantEmail()));

        if (tenant.getRole() != UserRole.TENANT) {
            throw new IllegalArgumentException("User is not a tenant");
        }

        // Find and validate apartment
        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found with ID: " + request.getApartmentId()));

        // Verify apartment belongs to this manager
        if (!apartment.getProperty().getManager().getId().equals(manager.getId())) {
            throw new IllegalArgumentException("You don't have permission to manage this apartment");
        }

        // Check if apartment is already occupied
        if ("OCCUPIED".equalsIgnoreCase(apartment.getOccupancyStatus())) {
            throw new IllegalArgumentException("Apartment is already occupied");
        }

        // Check if tenant is already connected to this apartment
        String propertyName = apartment.getProperty().getName();
        if (connectionRepository.existsByTenantAndPropertyNameAndIsActive(tenant, propertyName, true)) {
            throw new IllegalArgumentException("Tenant is already connected to this property");
        }

        // Create connection
        TenantPropertyConnection connection = new TenantPropertyConnection(
                tenant, manager, propertyName,
                request.getStartDate(), request.getEndDate(), request.getMonthlyRent()
        );
        connection.setSecurityDeposit(request.getSecurityDeposit());
        connection.setNotes(request.getNotes());

        // Update apartment occupancy and tenant info
        apartment.setOccupancyStatus("OCCUPIED");
        apartment.setTenantName(tenant.getFirstName() + " " + tenant.getLastName());
        apartment.setTenantEmail(tenant.getEmail());
        apartment.setTenantPhone(tenant.getPhone());
        apartmentRepository.save(apartment);

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

    public List<User> searchTenantsGlobal(User manager, String searchText) {
        if (manager.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only managers can search tenants globally");
        }

        if (searchText == null || searchText.trim().isEmpty()) {
            // Return all active tenants
            return userRepository.findByRoleAndAccountStatus(UserRole.TENANT, com.bms.backend.enums.AccountStatus.ACTIVE);
        }

        // Search tenants by name, email, or phone
        return userRepository.findTenantsBySearchText(searchText.trim());
    }

    public List<TenantConnectionDto> getManagerTenantConnections(User manager, String searchText) {
        if (manager.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only managers can view their tenant connections");
        }

        List<TenantPropertyConnection> connections;
        if (searchText == null || searchText.trim().isEmpty()) {
            connections = connectionRepository.findByManagerAndIsActive(manager, true);
        } else {
            connections = connectionRepository.findByManagerAndSearchText(manager, searchText.trim());
        }

        return connections.stream()
                .map(connection -> {
                    TenantConnectionDto dto = new TenantConnectionDto(connection);
                    
                    // Find apartment and property IDs based on tenant email and property name
                    if (connection.getTenant() != null) {
                        List<Apartment> apartments = apartmentRepository.findByTenantEmail(connection.getTenant().getEmail());
                        for (Apartment apartment : apartments) {
                            if (apartment.getProperty().getName().equals(connection.getPropertyName())) {
                                dto.setApartmentId(apartment.getId());
                                dto.setPropertyId(apartment.getProperty().getId());
                                break;
                            }
                        }
                    }
                    
                    return dto;
                })
                .toList();
    }
}