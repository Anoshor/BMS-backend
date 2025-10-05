package com.bms.backend.service;

import com.bms.backend.dto.request.ApartmentRequest;
import com.bms.backend.entity.Apartment;
import com.bms.backend.entity.PropertyBuilding;
import com.bms.backend.entity.User;
import com.bms.backend.repository.ApartmentRepository;
import com.bms.backend.repository.PropertyBuildingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApartmentService {

    @Autowired
    private ApartmentRepository apartmentRepository;

    @Autowired
    private PropertyBuildingRepository propertyBuildingRepository;

    @Autowired
    private com.bms.backend.repository.UserRepository userRepository;

    @Autowired
    private com.bms.backend.repository.TenantPropertyConnectionRepository connectionRepository;

    @Autowired
    private S3Service s3Service;

    public Apartment createApartment(ApartmentRequest request, User manager) {
        Optional<PropertyBuilding> property = propertyBuildingRepository.findById(request.getPropertyId());
        
        if (property.isEmpty() || !property.get().getManager().getId().equals(manager.getId())) {
            throw new IllegalArgumentException("Property not found or not authorized");
        }
        
        // Check if unit number already exists in this property
        Optional<Apartment> existingApartment = apartmentRepository.findByPropertyAndUnitNumber(property.get(), request.getUnitNumber());
        if (existingApartment.isPresent()) {
            throw new IllegalArgumentException("Unit number '" + request.getUnitNumber() + "' already exists in this property");
        }

        Apartment apartment = new Apartment();
        apartment.setProperty(property.get());
        apartment.setUnitNumber(request.getUnitNumber());
        apartment.setUnitType(request.getUnitType());
        apartment.setFloor(request.getFloor());
        apartment.setBedrooms(request.getBedrooms());
        apartment.setBathrooms(request.getBathrooms());
        apartment.setSquareFootage(request.getSquareFootage());
        // Normalize enum-like fields to uppercase for consistency
        apartment.setFurnished(normalizeString(request.getFurnished()));
        apartment.setBalcony(normalizeString(request.getBalcony()));
        apartment.setRent(request.getRent());
        apartment.setSecurityDeposit(request.getSecurityDeposit());
        apartment.setMaintenanceCharges(request.getMaintenanceCharges());

        // IMPORTANT: On creation, only allow VACANT or MAINTENANCE status
        // OCCUPIED status should only be set via assignTenant method to ensure proper tenant connection
        String occupancyStatus = normalizeString(request.getOccupancyStatus());
        if (occupancyStatus == null || occupancyStatus.isEmpty()) {
            apartment.setOccupancyStatus("VACANT"); // Default to VACANT
        } else if ("MAINTENANCE".equals(occupancyStatus)) {
            apartment.setOccupancyStatus("MAINTENANCE");
        } else {
            apartment.setOccupancyStatus("VACANT"); // Force VACANT for all other cases
        }

        apartment.setUtilityMeterNumbers(request.getUtilityMeterNumbers());
        apartment.setDocuments(request.getDocuments());
        apartment.setTenantName(request.getTenantName());
        apartment.setTenantEmail(request.getTenantEmail());
        apartment.setTenantPhone(request.getTenantPhone());
        apartment.setCreatedAt(Instant.now());
        apartment.setUpdatedAt(Instant.now());

        return apartmentRepository.save(apartment);
    }

    public List<Apartment> getApartmentsByManager(User manager) {
        return apartmentRepository.findByPropertyManager(manager);
    }

    public List<Apartment> searchApartmentsByManager(User manager, String searchText) {
        return apartmentRepository.findByManagerAndSearchText(manager, searchText);
    }

    public List<Apartment> getApartmentsByProperty(UUID propertyId, User manager) {
        Optional<PropertyBuilding> property = propertyBuildingRepository.findById(propertyId);
        
        if (property.isEmpty() || !property.get().getManager().getId().equals(manager.getId())) {
            throw new RuntimeException("Property not found or not authorized");
        }
        
        return apartmentRepository.findByProperty(property.get());
    }

    public List<Apartment> getOccupiedApartmentsByManager(User manager) {
        return apartmentRepository.findOccupiedByManager(manager);
    }

    public List<Apartment> getUnoccupiedApartmentsByManager(User manager) {
        return apartmentRepository.findUnoccupiedByManager(manager);
    }

    public List<Apartment> getApartmentsByTenantInfo(String tenantName, String tenantEmail, String tenantPhone) {
        if (tenantEmail != null && !tenantEmail.trim().isEmpty()) {
            return apartmentRepository.findByTenantEmail(tenantEmail);
        }
        if (tenantPhone != null && !tenantPhone.trim().isEmpty()) {
            return apartmentRepository.findByTenantPhone(tenantPhone);
        }
        if (tenantName != null && !tenantName.trim().isEmpty()) {
            return apartmentRepository.findByTenantNameContaining(tenantName);
        }
        return List.of();
    }

    public Optional<Apartment> getApartmentById(UUID id) {
        Optional<Apartment> apartmentOpt = apartmentRepository.findById(id);

        if (apartmentOpt.isPresent()) {
            Apartment apartment = apartmentOpt.get();

            // Populate tenantId if tenant email exists
            if (apartment.getTenantEmail() != null && !apartment.getTenantEmail().trim().isEmpty()) {
                userRepository.findByEmail(apartment.getTenantEmail())
                    .ifPresent(tenant -> apartment.setTenantId(tenant.getId()));
            }

            // Populate connectionId from active tenant connection
            List<com.bms.backend.entity.TenantPropertyConnection> connections =
                connectionRepository.findByApartment(apartment);

            // Find the active connection for this apartment
            connections.stream()
                .filter(conn -> conn.getIsActive() != null && conn.getIsActive())
                .findFirst()
                .ifPresent(conn -> apartment.setConnectionId(conn.getId()));
        }

        return apartmentOpt;
    }

    public Apartment updateApartment(UUID id, ApartmentRequest request, User manager) {
        Optional<Apartment> existingApartment = apartmentRepository.findById(id);

        if (existingApartment.isEmpty() ||
            !existingApartment.get().getProperty().getManager().getId().equals(manager.getId())) {
            throw new RuntimeException("Apartment not found or not authorized");
        }

        Apartment apartment = existingApartment.get();

        // Verify the propertyId matches the existing apartment's property
        // (We don't allow moving apartments between properties)
        if (!apartment.getProperty().getId().equals(request.getPropertyId())) {
            throw new IllegalArgumentException("Cannot change property of an existing apartment");
        }
        apartment.setUnitNumber(request.getUnitNumber());
        apartment.setUnitType(request.getUnitType());
        apartment.setFloor(request.getFloor());
        apartment.setBedrooms(request.getBedrooms());
        apartment.setBathrooms(request.getBathrooms());
        apartment.setSquareFootage(request.getSquareFootage());
        // Normalize enum-like fields to uppercase for consistency
        apartment.setFurnished(normalizeString(request.getFurnished()));
        apartment.setBalcony(normalizeString(request.getBalcony()));
        apartment.setRent(request.getRent());
        apartment.setSecurityDeposit(request.getSecurityDeposit());
        apartment.setMaintenanceCharges(request.getMaintenanceCharges());

        // IMPORTANT: Do NOT allow manual occupancy status changes
        // Occupancy status should only be changed via assignTenant/removeTenant methods
        // This prevents data inconsistency between apartments and tenant connections
        // Only allow MAINTENANCE status to be set manually for maintenance scenarios
        if (request.getOccupancyStatus() != null) {
            String newStatus = normalizeString(request.getOccupancyStatus());
            if ("MAINTENANCE".equals(newStatus)) {
                apartment.setOccupancyStatus(newStatus);
            }
            // Silently ignore VACANT/OCCUPIED changes - they must be done via proper endpoints
        }

        apartment.setUtilityMeterNumbers(request.getUtilityMeterNumbers());
        apartment.setDocuments(request.getDocuments());

        // IMPORTANT: Only update tenant fields if they are provided in the request
        // This preserves existing tenant connections during unit updates
        if (request.getTenantName() != null) {
            apartment.setTenantName(request.getTenantName());
        }
        if (request.getTenantEmail() != null) {
            apartment.setTenantEmail(request.getTenantEmail());
        }
        if (request.getTenantPhone() != null) {
            apartment.setTenantPhone(request.getTenantPhone());
        }

        apartment.setUpdatedAt(Instant.now());

        return apartmentRepository.save(apartment);
    }

    public void deleteApartment(UUID id, User manager) {
        Optional<Apartment> apartment = apartmentRepository.findById(id);

        if (apartment.isEmpty() ||
            !apartment.get().getProperty().getManager().getId().equals(manager.getId())) {
            throw new RuntimeException("Apartment not found or not authorized");
        }

        Apartment apt = apartment.get();

        // First, delete all tenant connections for this apartment
        List<com.bms.backend.entity.TenantPropertyConnection> connections =
            connectionRepository.findByApartment(apt);

        if (!connections.isEmpty()) {
            connectionRepository.deleteAll(connections);
        }

        // Then delete the apartment
        apartmentRepository.deleteById(id);
    }

    public Apartment assignTenant(UUID apartmentId, String tenantName, String tenantEmail, String tenantPhone, User manager) {
        Optional<Apartment> existingApartment = apartmentRepository.findById(apartmentId);
        
        if (existingApartment.isEmpty() || 
            !existingApartment.get().getProperty().getManager().getId().equals(manager.getId())) {
            throw new RuntimeException("Apartment not found or not authorized");
        }

        Apartment apartment = existingApartment.get();
        apartment.setTenantName(tenantName);
        apartment.setTenantEmail(tenantEmail);
        apartment.setTenantPhone(tenantPhone);
        apartment.setOccupancyStatus("OCCUPIED");
        apartment.setUpdatedAt(Instant.now());

        return apartmentRepository.save(apartment);
    }

    public Apartment removeTenant(UUID apartmentId, User manager) {
        Optional<Apartment> existingApartment = apartmentRepository.findById(apartmentId);

        if (existingApartment.isEmpty() ||
            !existingApartment.get().getProperty().getManager().getId().equals(manager.getId())) {
            throw new RuntimeException("Apartment not found or not authorized");
        }

        Apartment apartment = existingApartment.get();

        // First, deactivate all tenant connections for this apartment
        List<com.bms.backend.entity.TenantPropertyConnection> connections =
            connectionRepository.findByApartment(apartment);

        for (com.bms.backend.entity.TenantPropertyConnection connection : connections) {
            if (connection.getIsActive()) {
                connection.setIsActive(false);
                connectionRepository.save(connection);
            }
        }

        // Then clear tenant info from apartment
        apartment.setTenantName(null);
        apartment.setTenantEmail(null);
        apartment.setTenantPhone(null);
        apartment.setOccupancyStatus("VACANT");
        apartment.setUpdatedAt(Instant.now());

        return apartmentRepository.save(apartment);
    }
    
    public Apartment uploadApartmentImages(UUID apartmentId, List<org.springframework.web.multipart.MultipartFile> images, User manager) {
        Optional<Apartment> existingApartment = apartmentRepository.findById(apartmentId);

        if (existingApartment.isEmpty() ||
            !existingApartment.get().getProperty().getManager().getId().equals(manager.getId())) {
            throw new RuntimeException("Apartment not found or not authorized");
        }

        Apartment apartment = existingApartment.get();
        List<String> imageUrls = new java.util.ArrayList<>();

        // Upload each image to S3
        for (org.springframework.web.multipart.MultipartFile image : images) {
            try {
                String imageUrl = s3Service.uploadFile(image, manager.getId(), S3Service.FileType.APARTMENT);
                imageUrls.add(imageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }

        // Convert image URLs to JSON array string
        String imagesJson = new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(imageUrls).toString();
        apartment.setImages(imagesJson);
        apartment.setUpdatedAt(Instant.now());

        return apartmentRepository.save(apartment);
    }

    /**
     * Normalizes string values to uppercase for consistency
     * Handles null and empty strings gracefully
     */
    private String normalizeString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }
        return value.trim().toUpperCase();
    }
}