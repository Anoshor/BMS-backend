package com.bms.backend.controller;

import com.bms.backend.dto.request.PropertyDetailsRequest;
import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.entity.Property;
import com.bms.backend.entity.User;
import com.bms.backend.service.PropertyService;
import com.bms.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/properties")
@CrossOrigin(origins = "*")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;
    
    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addPropertyDetails(
            @RequestBody @Valid PropertyDetailsRequest request) {
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();
            propertyService.addPropertyDetails(user, request);
            
            return ResponseEntity.ok(ApiResponse.success(null, "Property details added successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to add property details. Please try again."));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<String>> updatePropertyDetails(
            @RequestBody @Valid PropertyDetailsRequest request) {
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();
            propertyService.updatePropertyDetails(user, request);
            
            return ResponseEntity.ok(ApiResponse.success(null, "Property details updated successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update property details. Please try again."));
        }
    }

    @GetMapping("/unoccupied")
    public ResponseEntity<ApiResponse<List<Property>>> getUnoccupiedProperties() {
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();
            List<Property> properties = propertyService.getUnoccupiedProperties(user);
            
            return ResponseEntity.ok(ApiResponse.success(properties, "Unoccupied properties retrieved successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve unoccupied properties"));
        }
    }
}