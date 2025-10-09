package com.bms.backend.service;

import com.bms.backend.dto.request.PropertyBuildingRequest;
import com.bms.backend.entity.PropertyBuilding;
import com.bms.backend.entity.User;
import com.bms.backend.repository.PropertyBuildingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class
PropertyBuildingService {

    @Autowired
    private PropertyBuildingRepository propertyBuildingRepository;

    @Autowired
    private com.bms.backend.repository.ApartmentRepository apartmentRepository;

    @Autowired
    private com.bms.backend.repository.PropertyImageRepository propertyImageRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private com.bms.backend.repository.TenantPropertyConnectionRepository connectionRepository;

    @Autowired
    private com.bms.backend.repository.MaintenanceRequestRepository maintenanceRequestRepository;

    public PropertyBuilding createProperty(PropertyBuildingRequest request, User manager) {
        PropertyBuilding property = new PropertyBuilding();
        property.setName(request.getName());
        property.setAddress(request.getAddress());
        property.setPropertyType(request.getPropertyType());
        property.setResidentialType(request.getResidentialType());
        property.setTotalUnits(request.getTotalUnits());
        property.setTotalFloors(request.getTotalFloors());
        property.setYearBuilt(request.getYearBuilt());
        property.setAmenities(request.getAmenities());
        property.setManager(manager);
        property.setCreatedAt(Instant.now());
        property.setUpdatedAt(Instant.now());

        // Save property first to get the ID
        PropertyBuilding savedProperty = propertyBuildingRepository.save(property);

        // Save image URLs if provided (already uploaded to S3 by UI)
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            int displayOrder = 0;
            for (String imageUrl : request.getImages()) {
                com.bms.backend.entity.PropertyImage propertyImage = new com.bms.backend.entity.PropertyImage();
                propertyImage.setProperty(savedProperty);
                propertyImage.setImageUrl(imageUrl);
                propertyImage.setDisplayOrder(displayOrder);

                // Set first image as primary
                if (displayOrder == 0) {
                    propertyImage.setIsPrimary(true);
                }

                propertyImageRepository.save(propertyImage);
                displayOrder++;
            }
        }

        return savedProperty;
    }

    public List<PropertyBuilding> getPropertiesByManager(User manager) {
        List<PropertyBuilding> properties = propertyBuildingRepository.findByManager(manager);

        // Populate transient fields for each property
        for (PropertyBuilding property : properties) {
            populatePropertyFields(property);
        }

        return properties;
    }

    public List<PropertyBuilding> searchPropertiesByManager(User manager, String searchText) {
        return propertyBuildingRepository.findByManagerAndSearchText(manager, searchText);
    }

    public List<PropertyBuilding> getAllProperties() {
        return propertyBuildingRepository.findAll();
    }

    public List<PropertyBuilding> searchAllProperties(String searchText) {
        return propertyBuildingRepository.findBySearchText(searchText);
    }

    public Optional<PropertyBuilding> getPropertyById(UUID id) {
        Optional<PropertyBuilding> property = propertyBuildingRepository.findById(id);

        if (property.isPresent()) {
            populatePropertyFields(property.get());
        }

        return property;
    }

    public PropertyBuilding updateProperty(UUID id, PropertyBuildingRequest request, User manager) {
        Optional<PropertyBuilding> existingProperty = propertyBuildingRepository.findById(id);
        
        if (existingProperty.isPresent() && existingProperty.get().getManager().getId().equals(manager.getId())) {
            PropertyBuilding property = existingProperty.get();
            property.setName(request.getName());
            property.setAddress(request.getAddress());
            property.setPropertyType(request.getPropertyType());
            property.setResidentialType(request.getResidentialType());
            property.setTotalUnits(request.getTotalUnits());
            property.setTotalFloors(request.getTotalFloors());
            property.setYearBuilt(request.getYearBuilt());
            property.setAmenities(request.getAmenities());
            property.setUpdatedAt(Instant.now());

            // Handle images - if provided, update property images
            if (request.getImages() != null) {
                // Delete existing images first
                List<com.bms.backend.entity.PropertyImage> existingImages =
                    propertyImageRepository.findByProperty(property);
                if (!existingImages.isEmpty()) {
                    propertyImageRepository.deleteAll(existingImages);
                }

                // Add new images
                if (!request.getImages().isEmpty()) {
                    int displayOrder = 0;
                    for (String imageUrl : request.getImages()) {
                        com.bms.backend.entity.PropertyImage propertyImage = new com.bms.backend.entity.PropertyImage();
                        propertyImage.setProperty(property);
                        propertyImage.setImageUrl(imageUrl);
                        propertyImage.setDisplayOrder(displayOrder);

                        // Set first image as primary
                        if (displayOrder == 0) {
                            propertyImage.setIsPrimary(true);
                        }

                        propertyImageRepository.save(propertyImage);
                        displayOrder++;
                    }
                }
            }

            return propertyBuildingRepository.save(property);
        }
        
        throw new RuntimeException("Property not found or not authorized");
    }

    public void deleteProperty(UUID id, User manager) {
        Optional<PropertyBuilding> property = propertyBuildingRepository.findById(id);

        if (property.isPresent() && property.get().getManager().getId().equals(manager.getId())) {
            propertyBuildingRepository.deleteById(id);
        } else {
            throw new RuntimeException("Property not found or not authorized");
        }
    }

    public PropertyBuilding uploadPropertyImages(UUID propertyId, List<org.springframework.web.multipart.MultipartFile> images, User manager) {
        Optional<PropertyBuilding> existingProperty = propertyBuildingRepository.findById(propertyId);

        if (existingProperty.isEmpty() ||
            !existingProperty.get().getManager().getId().equals(manager.getId())) {
            throw new RuntimeException("Property not found or not authorized");
        }

        PropertyBuilding property = existingProperty.get();

        // Get current image count to determine display order
        Long currentImageCount = propertyImageRepository.countByPropertyId(propertyId);
        int displayOrder = currentImageCount.intValue();

        // Upload each image to S3 and create PropertyImage records
        for (org.springframework.web.multipart.MultipartFile image : images) {
            try {
                String imageUrl = s3Service.uploadFile(image, manager.getId(), S3Service.FileType.PROPERTY);

                com.bms.backend.entity.PropertyImage propertyImage = new com.bms.backend.entity.PropertyImage();
                propertyImage.setProperty(property);
                propertyImage.setImageUrl(imageUrl);
                propertyImage.setImageName(image.getOriginalFilename());
                propertyImage.setImageType(image.getContentType());
                propertyImage.setImageSize(image.getSize());
                propertyImage.setDisplayOrder(displayOrder++);

                // Set first image as primary if no primary exists
                if (currentImageCount == 0 && displayOrder == 1) {
                    propertyImage.setIsPrimary(true);
                }

                propertyImageRepository.save(propertyImage);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }

        property.setUpdatedAt(Instant.now());
        return propertyBuildingRepository.save(property);
    }

    /**
     * Helper method to populate transient fields for a property
     */
    private void populatePropertyFields(PropertyBuilding property) {
        // Get all apartments for this property
        List<com.bms.backend.entity.Apartment> apartments = apartmentRepository.findByProperty(property);

        // Count units by status
        long vacantCount = apartments.stream()
            .filter(apt -> "VACANT".equalsIgnoreCase(apt.getOccupancyStatus()))
            .count();
        long occupiedCount = apartments.stream()
            .filter(apt -> "OCCUPIED".equalsIgnoreCase(apt.getOccupancyStatus()))
            .count();
        long maintenanceCount = apartments.stream()
            .filter(apt -> "MAINTENANCE".equalsIgnoreCase(apt.getOccupancyStatus()))
            .count();

        property.setVacantUnits(vacantCount);
        property.setOccupiedUnits(occupiedCount);
        property.setUnderMaintenanceUnits(maintenanceCount);

        // Set manager name
        if (property.getManager() != null) {
            property.setManagerName(property.getManager().getFirstName() + " " + property.getManager().getLastName());
        }

        // Get image URLs from property_images table
        List<com.bms.backend.entity.PropertyImage> propertyImages = propertyImageRepository.findByPropertyOrderByDisplayOrderAsc(property);
        if (propertyImages != null && !propertyImages.isEmpty()) {
            List<String> imageUrls = propertyImages.stream()
                .map(com.bms.backend.entity.PropertyImage::getImageUrl)
                .collect(java.util.stream.Collectors.toList());
            property.setImageUrls(imageUrls);
        }
    }

    /**
     * Get all current tenants for a property
     */
    public List<com.bms.backend.dto.response.TenantDetailsDto> getCurrentTenantsByProperty(UUID propertyId, User manager) {
        // Verify property belongs to manager
        Optional<PropertyBuilding> property = propertyBuildingRepository.findById(propertyId);
        if (property.isEmpty() || !property.get().getManager().getId().equals(manager.getId())) {
            throw new RuntimeException("Property not found or not authorized");
        }

        // Get all active connections for this property
        List<com.bms.backend.entity.TenantPropertyConnection> connections =
            connectionRepository.findByPropertyAndIsActiveOrderByCreatedAtDesc(property.get(), true);

        // Convert to TenantDetailsDto with complete information
        return connections.stream()
            .map(connection -> {
                com.bms.backend.dto.response.TenantDetailsDto dto = new com.bms.backend.dto.response.TenantDetailsDto();

                // Basic tenant info
                if (connection.getTenant() != null) {
                    dto.setTenantId(connection.getTenant().getId());
                    dto.setTenantName(connection.getTenant().getFirstName() + " " + connection.getTenant().getLastName());
                    dto.setFirstName(connection.getTenant().getFirstName());
                    dto.setLastName(connection.getTenant().getLastName());
                    dto.setEmail(connection.getTenant().getEmail());
                    dto.setPhone(connection.getTenant().getPhone());
                    dto.setPhoto(connection.getTenant().getProfileImageUrl());
                    dto.setCreatedAt(connection.getTenant().getCreatedAt());
                }

                // Lease summary info
                dto.setTotalActiveLeases(1);
                dto.setTotalProperties(1);
                dto.setTotalMonthlyRent(connection.getMonthlyRent());

                // Create property info list with single property
                com.bms.backend.dto.response.TenantDetailsDto.TenantPropertyInfo propertyInfo =
                    new com.bms.backend.dto.response.TenantDetailsDto.TenantPropertyInfo();

                propertyInfo.setConnectionId(connection.getId());
                propertyInfo.setLeaseStartDate(connection.getStartDate());
                propertyInfo.setLeaseEndDate(connection.getEndDate());
                propertyInfo.setMonthlyRent(connection.getMonthlyRent());
                propertyInfo.setSecurityDeposit(connection.getSecurityDeposit());
                propertyInfo.setIsActive(connection.getIsActive());

                // Apartment info
                if (connection.getApartment() != null) {
                    com.bms.backend.entity.Apartment apt = connection.getApartment();
                    propertyInfo.setApartmentId(apt.getId());
                    propertyInfo.setUnitNumber(apt.getUnitNumber());
                    propertyInfo.setUnitType(apt.getUnitType());
                    propertyInfo.setFloor(apt.getFloor());
                    propertyInfo.setBedrooms(apt.getBedrooms());
                    propertyInfo.setBathrooms(apt.getBathrooms());
                    propertyInfo.setSquareFootage(apt.getSquareFootage());
                    propertyInfo.setOccupancyStatus(apt.getOccupancyStatus());
                    propertyInfo.setFurnished(apt.getFurnished());
                    propertyInfo.setBalcony(apt.getBalcony());
                    propertyInfo.setImages(apt.getImages());
                    propertyInfo.setUtilityMeterNumbers(apt.getUtilityMeterNumbers());
                    propertyInfo.setMaintenanceCharges(apt.getMaintenanceCharges());

                    // Property info from apartment
                    if (apt.getProperty() != null) {
                        propertyInfo.setPropertyId(apt.getProperty().getId());
                        propertyInfo.setPropertyName(apt.getProperty().getName());
                        propertyInfo.setPropertyType(apt.getProperty().getPropertyType());
                        propertyInfo.setPropertyAddress(apt.getProperty().getAddress());
                    }
                }

                // Manager info
                if (connection.getManager() != null) {
                    propertyInfo.setManagerId(connection.getManager().getId());
                    propertyInfo.setManagerName(connection.getManager().getFirstName() + " " + connection.getManager().getLastName());
                    propertyInfo.setManagerEmail(connection.getManager().getEmail());
                    propertyInfo.setManagerPhone(connection.getManager().getPhone());
                }

                dto.setProperties(java.util.Collections.singletonList(propertyInfo));

                return dto;
            })
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get recent maintenance requests for a property
     */
    public List<com.bms.backend.dto.response.MaintenanceRequestResponse> getRecentMaintenanceByProperty(UUID propertyId, User manager) {
        // Verify property belongs to manager
        Optional<PropertyBuilding> property = propertyBuildingRepository.findById(propertyId);
        if (property.isEmpty() || !property.get().getManager().getId().equals(manager.getId())) {
            throw new RuntimeException("Property not found or not authorized");
        }

        // Get all apartments for this property
        List<com.bms.backend.entity.Apartment> apartments = apartmentRepository.findByProperty(property.get());
        if (apartments.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // Get maintenance requests for all apartments in this property
        List<com.bms.backend.entity.MaintenanceRequest> requests = new java.util.ArrayList<>();
        for (com.bms.backend.entity.Apartment apartment : apartments) {
            requests.addAll(maintenanceRequestRepository.findByApartment(apartment));
        }

        // Sort by created date descending and limit to 10 most recent
        return requests.stream()
            .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
            .limit(10)
            .map(com.bms.backend.dto.response.MaintenanceRequestResponse::new)
            .collect(java.util.stream.Collectors.toList());
    }
}