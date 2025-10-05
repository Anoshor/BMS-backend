package com.bms.backend.controller;

import com.bms.backend.dto.request.PropertyBuildingRequest;
import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.entity.PropertyBuilding;
import com.bms.backend.entity.User;
import com.bms.backend.service.PropertyBuildingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
public class PropertyBuildingController {

    @Autowired
    private PropertyBuildingService propertyBuildingService;

    @PostMapping("/buildings")
    public ResponseEntity<ApiResponse<PropertyBuilding>> createProperty(
            @Valid @ModelAttribute PropertyBuildingRequest request,
            @RequestParam(value = "images", required = false) List<org.springframework.web.multipart.MultipartFile> images) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            PropertyBuilding property = propertyBuildingService.createProperty(request, user, images);
            return ResponseEntity.ok(new ApiResponse<>(true, property, "Property created successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to create property: " + e.getMessage()));
        }
    }

    @GetMapping("/buildings")
    public ResponseEntity<ApiResponse<List<PropertyBuilding>>> getMyProperties() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<PropertyBuilding> properties = propertyBuildingService.getPropertiesByManager(user);
            return ResponseEntity.ok(new ApiResponse<>(true, properties, "Properties retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve properties: " + e.getMessage()));
        }
    }

    @GetMapping("/buildings/search")
    public ResponseEntity<ApiResponse<List<PropertyBuilding>>> searchMyProperties(@RequestParam String searchText) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<PropertyBuilding> properties = propertyBuildingService.searchPropertiesByManager(user, searchText);
            return ResponseEntity.ok(new ApiResponse<>(true, properties, "Properties search completed"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to search properties: " + e.getMessage()));
        }
    }

    @GetMapping("/buildings/all")
    public ResponseEntity<ApiResponse<List<PropertyBuilding>>> getAllProperties() {
        try {
            List<PropertyBuilding> properties = propertyBuildingService.getAllProperties();
            return ResponseEntity.ok(new ApiResponse<>(true, properties, "All properties retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve all properties: " + e.getMessage()));
        }
    }

    @GetMapping("/buildings/search/global")
    public ResponseEntity<ApiResponse<List<PropertyBuilding>>> globalSearchProperties(@RequestParam String searchText) {
        try {
            List<PropertyBuilding> properties = propertyBuildingService.searchAllProperties(searchText);
            return ResponseEntity.ok(new ApiResponse<>(true, properties, "Global search completed"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to perform global search: " + e.getMessage()));
        }
    }

    @GetMapping("/buildings/{id}")
    public ResponseEntity<ApiResponse<PropertyBuilding>> getPropertyById(@PathVariable UUID id) {
        try {
            Optional<PropertyBuilding> property = propertyBuildingService.getPropertyById(id);
            if (property.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, property.get(), "Property retrieved successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Property not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve property: " + e.getMessage()));
        }
    }

    @PutMapping("/buildings/{id}")
    public ResponseEntity<ApiResponse<PropertyBuilding>> updateProperty(@PathVariable UUID id, @Valid @RequestBody PropertyBuildingRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            PropertyBuilding property = propertyBuildingService.updateProperty(id, request, user);
            return ResponseEntity.ok(new ApiResponse<>(true, property, "Property updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to update property: " + e.getMessage()));
        }
    }

    @DeleteMapping("/buildings/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProperty(@PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            propertyBuildingService.deleteProperty(id, user);
            return ResponseEntity.ok(new ApiResponse<>(true, null, "Property deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to delete property: " + e.getMessage()));
        }
    }

    @PostMapping("/buildings/{id}/upload-images")
    public ResponseEntity<ApiResponse<PropertyBuilding>> uploadPropertyImages(
            @PathVariable UUID id,
            @RequestParam("images") List<org.springframework.web.multipart.MultipartFile> images) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            PropertyBuilding property = propertyBuildingService.uploadPropertyImages(id, images, user);
            return ResponseEntity.ok(new ApiResponse<>(true, property, "Property images uploaded successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to upload images: " + e.getMessage()));
        }
    }
}