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

    public Apartment createApartment(ApartmentRequest request, User manager) {
        Optional<PropertyBuilding> property = propertyBuildingRepository.findById(request.getPropertyId());
        
        if (property.isEmpty() || !property.get().getManager().getId().equals(manager.getId())) {
            throw new RuntimeException("Property not found or not authorized");
        }

        Apartment apartment = new Apartment();
        apartment.setProperty(property.get());
        apartment.setUnitNumber(request.getUnitNumber());
        apartment.setUnitType(request.getUnitType());
        apartment.setFloor(request.getFloor());
        apartment.setBedrooms(request.getBedrooms());
        apartment.setBathrooms(request.getBathrooms());
        apartment.setSquareFootage(request.getSquareFootage());
        apartment.setFurnished(request.getFurnished());
        apartment.setBalcony(request.getBalcony());
        apartment.setRent(request.getRent());
        apartment.setSecurityDeposit(request.getSecurityDeposit());
        apartment.setMaintenanceCharges(request.getMaintenanceCharges());
        apartment.setOccupancyStatus(request.getOccupancyStatus());
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
        return apartmentRepository.findById(id);
    }

    public Apartment updateApartment(UUID id, ApartmentRequest request, User manager) {
        Optional<Apartment> existingApartment = apartmentRepository.findById(id);
        
        if (existingApartment.isEmpty() || 
            !existingApartment.get().getProperty().getManager().getId().equals(manager.getId())) {
            throw new RuntimeException("Apartment not found or not authorized");
        }

        Apartment apartment = existingApartment.get();
        apartment.setUnitNumber(request.getUnitNumber());
        apartment.setUnitType(request.getUnitType());
        apartment.setFloor(request.getFloor());
        apartment.setBedrooms(request.getBedrooms());
        apartment.setBathrooms(request.getBathrooms());
        apartment.setSquareFootage(request.getSquareFootage());
        apartment.setFurnished(request.getFurnished());
        apartment.setBalcony(request.getBalcony());
        apartment.setRent(request.getRent());
        apartment.setSecurityDeposit(request.getSecurityDeposit());
        apartment.setMaintenanceCharges(request.getMaintenanceCharges());
        apartment.setOccupancyStatus(request.getOccupancyStatus());
        apartment.setUtilityMeterNumbers(request.getUtilityMeterNumbers());
        apartment.setDocuments(request.getDocuments());
        apartment.setTenantName(request.getTenantName());
        apartment.setTenantEmail(request.getTenantEmail());
        apartment.setTenantPhone(request.getTenantPhone());
        apartment.setUpdatedAt(Instant.now());

        return apartmentRepository.save(apartment);
    }

    public void deleteApartment(UUID id, User manager) {
        Optional<Apartment> apartment = apartmentRepository.findById(id);
        
        if (apartment.isPresent() && 
            apartment.get().getProperty().getManager().getId().equals(manager.getId())) {
            apartmentRepository.deleteById(id);
        } else {
            throw new RuntimeException("Apartment not found or not authorized");
        }
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
        apartment.setTenantName(null);
        apartment.setTenantEmail(null);
        apartment.setTenantPhone(null);
        apartment.setOccupancyStatus("VACANT");
        apartment.setUpdatedAt(Instant.now());

        return apartmentRepository.save(apartment);
    }
}