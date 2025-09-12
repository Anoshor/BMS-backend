package com.bms.backend.controller;

import com.bms.backend.dto.request.MaintenanceRequestCreateRequest;
import com.bms.backend.dto.request.MaintenanceRequestUpdateRequest;
import com.bms.backend.dto.request.MaintenanceUpdateRequest;
import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.entity.*;
import com.bms.backend.service.MaintenanceRequestService;
import com.bms.backend.service.ServiceCategoryService;
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
@RequestMapping("/maintenance")
public class MaintenanceController {

    @Autowired
    private MaintenanceRequestService maintenanceRequestService;

    @Autowired
    private ServiceCategoryService serviceCategoryService;

    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<MaintenanceRequest>> createMaintenanceRequest(@Valid @RequestBody MaintenanceRequestCreateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            MaintenanceRequest maintenanceRequest = maintenanceRequestService.createMaintenanceRequest(request, user);
            return ResponseEntity.ok(new ApiResponse<>(true, maintenanceRequest, "Maintenance request created successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to create maintenance request: " + e.getMessage()));
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<MaintenanceRequest>>> getMyMaintenanceRequests() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByManager(user);
            return ResponseEntity.ok(new ApiResponse<>(true, requests, "Maintenance requests retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve maintenance requests: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/tenant")
    public ResponseEntity<ApiResponse<List<MaintenanceRequest>>> getMaintenanceRequestsByTenant(@RequestParam String tenantEmail) {
        try {
            List<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByTenant(tenantEmail);
            return ResponseEntity.ok(new ApiResponse<>(true, requests, "Tenant maintenance requests retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve tenant maintenance requests: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/status/{status}")
    public ResponseEntity<ApiResponse<List<MaintenanceRequest>>> getMaintenanceRequestsByStatus(@PathVariable MaintenanceRequest.Status status) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByStatus(status, user);
            return ResponseEntity.ok(new ApiResponse<>(true, requests, "Maintenance requests by status retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve maintenance requests by status: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/priority/{priority}")
    public ResponseEntity<ApiResponse<List<MaintenanceRequest>>> getMaintenanceRequestsByPriority(@PathVariable MaintenanceRequest.Priority priority) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByPriority(priority, user);
            return ResponseEntity.ok(new ApiResponse<>(true, requests, "Maintenance requests by priority retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve maintenance requests by priority: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<MaintenanceRequest>>> getMaintenanceRequestsByCategory(@PathVariable UUID categoryId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByServiceCategory(categoryId, user);
            return ResponseEntity.ok(new ApiResponse<>(true, requests, "Maintenance requests by category retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve maintenance requests by category: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/assigned")
    public ResponseEntity<ApiResponse<List<MaintenanceRequest>>> getAssignedMaintenanceRequests() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByAssignee(user);
            return ResponseEntity.ok(new ApiResponse<>(true, requests, "Assigned maintenance requests retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve assigned maintenance requests: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/search")
    public ResponseEntity<ApiResponse<List<MaintenanceRequest>>> searchMaintenanceRequests(@RequestParam String searchText) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<MaintenanceRequest> requests = maintenanceRequestService.searchMaintenanceRequests(searchText, user);
            return ResponseEntity.ok(new ApiResponse<>(true, requests, "Maintenance requests search completed"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to search maintenance requests: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<ApiResponse<MaintenanceRequest>> getMaintenanceRequestById(@PathVariable UUID id) {
        try {
            Optional<MaintenanceRequest> request = maintenanceRequestService.getMaintenanceRequestById(id);
            if (request.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, request.get(), "Maintenance request retrieved successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Maintenance request not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve maintenance request: " + e.getMessage()));
        }
    }

    @PutMapping("/requests/{id}")
    public ResponseEntity<ApiResponse<MaintenanceRequest>> updateMaintenanceRequest(@PathVariable UUID id, @Valid @RequestBody MaintenanceRequestUpdateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            MaintenanceRequest maintenanceRequest = maintenanceRequestService.updateMaintenanceRequest(id, request, user);
            return ResponseEntity.ok(new ApiResponse<>(true, maintenanceRequest, "Maintenance request updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to update maintenance request: " + e.getMessage()));
        }
    }

    @PostMapping("/requests/{id}/updates")
    public ResponseEntity<ApiResponse<MaintenanceUpdate>> addUpdateToMaintenanceRequest(@PathVariable UUID id, @Valid @RequestBody MaintenanceUpdateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            MaintenanceUpdate update = maintenanceRequestService.addUpdateToMaintenanceRequest(id, request, user);
            return ResponseEntity.ok(new ApiResponse<>(true, update, "Update added successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to add update: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/{id}/updates")
    public ResponseEntity<ApiResponse<List<MaintenanceUpdate>>> getUpdatesForMaintenanceRequest(@PathVariable UUID id) {
        try {
            List<MaintenanceUpdate> updates = maintenanceRequestService.getUpdatesForMaintenanceRequest(id);
            return ResponseEntity.ok(new ApiResponse<>(true, updates, "Updates retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve updates: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/{id}/photos")
    public ResponseEntity<ApiResponse<List<MaintenanceRequestPhoto>>> getPhotosForMaintenanceRequest(@PathVariable UUID id) {
        try {
            List<MaintenanceRequestPhoto> photos = maintenanceRequestService.getPhotosForMaintenanceRequest(id);
            return ResponseEntity.ok(new ApiResponse<>(true, photos, "Photos retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve photos: " + e.getMessage()));
        }
    }

    @DeleteMapping("/requests/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMaintenanceRequest(@PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            maintenanceRequestService.deleteMaintenanceRequest(id, user);
            return ResponseEntity.ok(new ApiResponse<>(true, null, "Maintenance request deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to delete maintenance request: " + e.getMessage()));
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<ServiceCategory>>> getAllServiceCategories() {
        try {
            List<ServiceCategory> categories = serviceCategoryService.getAllCategories();
            return ResponseEntity.ok(new ApiResponse<>(true, categories, "Service categories retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve service categories: " + e.getMessage()));
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<ServiceCategory>> createServiceCategory(@RequestParam String name, @RequestParam(required = false) String description) {
        try {
            ServiceCategory category = serviceCategoryService.createCategory(name, description);
            return ResponseEntity.ok(new ApiResponse<>(true, category, "Service category created successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to create service category: " + e.getMessage()));
        }
    }

    @PostMapping("/categories/init")
    public ResponseEntity<ApiResponse<Void>> initializeDefaultCategories() {
        try {
            serviceCategoryService.initializeDefaultCategories();
            return ResponseEntity.ok(new ApiResponse<>(true, null, "Default categories initialized successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to initialize default categories: " + e.getMessage()));
        }
    }
}