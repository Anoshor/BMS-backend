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

    public PropertyBuilding createProperty(PropertyBuildingRequest request, User manager, List<org.springframework.web.multipart.MultipartFile> images) {
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

        // Upload images if provided
        if (images != null && !images.isEmpty()) {
            int displayOrder = 0;
            for (org.springframework.web.multipart.MultipartFile image : images) {
                try {
                    String imageUrl = s3Service.uploadFile(image, manager.getId(), S3Service.FileType.PROPERTY);

                    com.bms.backend.entity.PropertyImage propertyImage = new com.bms.backend.entity.PropertyImage();
                    propertyImage.setProperty(savedProperty);
                    propertyImage.setImageUrl(imageUrl);
                    propertyImage.setImageName(image.getOriginalFilename());
                    propertyImage.setImageType(image.getContentType());
                    propertyImage.setImageSize(image.getSize());
                    propertyImage.setDisplayOrder(displayOrder);

                    // Set first image as primary
                    if (displayOrder == 0) {
                        propertyImage.setIsPrimary(true);
                    }

                    propertyImageRepository.save(propertyImage);
                    displayOrder++;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to upload image: " + e.getMessage());
                }
            }
        }

        return savedProperty;
    }

    public List<PropertyBuilding> getPropertiesByManager(User manager) {
        return propertyBuildingRepository.findByManager(manager);
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
            PropertyBuilding pb = property.get();
            // Count vacant and occupied units for this property
            long vacantCount = apartmentRepository.findByProperty(pb).stream()
                .filter(apt -> "vacant".equalsIgnoreCase(apt.getOccupancyStatus()))
                .count();
            long occupiedCount = apartmentRepository.findByProperty(pb).stream()
                .filter(apt -> "occupied".equalsIgnoreCase(apt.getOccupancyStatus()))
                .count();

            pb.setVacantUnits(vacantCount);
            pb.setOccupiedUnits(occupiedCount);

            // Set manager name
            if (pb.getManager() != null) {
                pb.setManagerName(pb.getManager().getFirstName() + " " + pb.getManager().getLastName());
            }
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
            property.setTotalUnits(request.getTotalUnits());
            property.setTotalFloors(request.getTotalFloors());
            property.setYearBuilt(request.getYearBuilt());
            property.setAmenities(request.getAmenities());
            property.setUpdatedAt(Instant.now());
            
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
}