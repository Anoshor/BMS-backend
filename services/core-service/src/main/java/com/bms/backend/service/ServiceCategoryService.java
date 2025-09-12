package com.bms.backend.service;

import com.bms.backend.entity.ServiceCategory;
import com.bms.backend.repository.ServiceCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ServiceCategoryService {

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    public ServiceCategory createCategory(String name, String description) {
        ServiceCategory category = new ServiceCategory();
        category.setName(name);
        category.setDescription(description);
        category.setCreatedAt(Instant.now());
        category.setUpdatedAt(Instant.now());
        
        return serviceCategoryRepository.save(category);
    }

    public List<ServiceCategory> getAllCategories() {
        return serviceCategoryRepository.findAll();
    }

    public Optional<ServiceCategory> getCategoryById(UUID id) {
        return serviceCategoryRepository.findById(id);
    }

    public Optional<ServiceCategory> getCategoryByName(String name) {
        return serviceCategoryRepository.findByName(name);
    }

    public ServiceCategory updateCategory(UUID id, String name, String description) {
        Optional<ServiceCategory> existingCategory = serviceCategoryRepository.findById(id);
        
        if (existingCategory.isPresent()) {
            ServiceCategory category = existingCategory.get();
            category.setName(name);
            category.setDescription(description);
            category.setUpdatedAt(Instant.now());
            
            return serviceCategoryRepository.save(category);
        }
        
        throw new RuntimeException("Service category not found");
    }

    public void deleteCategory(UUID id) {
        serviceCategoryRepository.deleteById(id);
    }

    public void initializeDefaultCategories() {
        List<String> defaultCategories = List.of(
            "Plumbing", "Electrical", "HVAC", "Painting", "Carpentry",
            "Cleaning", "Landscaping", "Security", "Appliance Repair", "General Maintenance"
        );

        for (String categoryName : defaultCategories) {
            if (serviceCategoryRepository.findByName(categoryName).isEmpty()) {
                createCategory(categoryName, "Default " + categoryName + " services");
            }
        }
    }
}