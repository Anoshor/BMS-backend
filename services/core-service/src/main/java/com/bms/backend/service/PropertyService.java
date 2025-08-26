package com.bms.backend.service;

import com.bms.backend.dto.request.PropertyDetailsRequest;
import com.bms.backend.entity.Property;
import com.bms.backend.entity.User;
import com.bms.backend.enums.UserRole;
import com.bms.backend.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    public void addPropertyDetails(User user, PropertyDetailsRequest request) {
        if (user.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only managers can add property details");
        }

        Property property = new Property();
        property.setManager(user);
        mapRequestToProperty(property, request);
        
        propertyRepository.save(property);
    }

    public void updatePropertyDetails(User user, PropertyDetailsRequest request) {
        if (user.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only managers can update property details");
        }

        Property property = propertyRepository.findByPropertyNameAndManager(request.getPropertyName(), user)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        mapRequestToProperty(property, request);
        propertyRepository.save(property);
    }

    private void mapRequestToProperty(Property property, PropertyDetailsRequest request) {
        property.setPropertyName(request.getPropertyName());
        property.setPropertyManagerName(request.getPropertyManagerName());
        property.setPropertyAddress(request.getPropertyAddress());
        property.setPropertyType(request.getPropertyType());
        property.setSquareFootage(request.getSquareFootage());
        property.setNumberOfUnits(request.getNumberOfUnits());
        property.setUnitNumber(request.getUnitNumber());
        property.setUnitType(request.getUnitType());
        property.setFloor(request.getFloor());
        property.setBedrooms(request.getBedrooms());
        property.setBathrooms(request.getBathrooms());
        property.setFurnished(request.getFurnished());
        property.setBalcony(request.getBalcony());
        property.setRent(request.getRent());
        property.setSecurityDeposit(request.getSecurityDeposit());
        property.setMaintenanceCharges(request.getMaintenanceCharges());
        property.setOccupancy(request.getOccupancy());
        property.setUtilityMeterNumber(request.getUtilityMeterNumber().longValue());
    }

    public List<Property> getUnoccupiedProperties(User manager) {
        if (manager.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only managers can view their properties");
        }

        return propertyRepository.findVacantPropertiesByManager(manager);
    }
}