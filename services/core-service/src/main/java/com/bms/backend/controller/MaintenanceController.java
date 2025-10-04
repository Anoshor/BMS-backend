package com.bms.backend.controller;

import com.bms.backend.dto.request.MaintenanceRequestCreateRequest;
import com.bms.backend.dto.request.MaintenanceRequestUpdateRequest;
import com.bms.backend.dto.request.MaintenanceUpdateRequest;
import com.bms.backend.dto.request.MaintenanceStatusUpdateRequest;
import com.bms.backend.dto.request.BulkMaintenanceRequestCreateRequest;
import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.dto.response.MaintenanceDetailsResponse;
import com.bms.backend.dto.response.MaintenanceProgressResponse;
import com.bms.backend.dto.response.MaintenanceRequestResponse;
import com.bms.backend.dto.response.BulkMaintenanceRequestResponse;
import com.bms.backend.entity.*;
import com.bms.backend.enums.UserRole;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/maintenance")
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

    @PostMapping("/requests/bulk")
    public ResponseEntity<ApiResponse<BulkMaintenanceRequestResponse>> createBulkMaintenanceRequests(@Valid @RequestBody BulkMaintenanceRequestCreateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            // Only managers can create bulk maintenance requests
            if (!user.getRole().equals(UserRole.PROPERTY_MANAGER)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Only property managers can create bulk maintenance requests"));
            }

            BulkMaintenanceRequestResponse response = maintenanceRequestService.createBulkMaintenanceRequests(request, user);

            String message = String.format("Bulk maintenance request completed. Created: %d, Failed: %d",
                    response.getTotalCreated(), response.getTotalFailed());

            return ResponseEntity.ok(new ApiResponse<>(true, response, message));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to create bulk maintenance requests: " + e.getMessage()));
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<MaintenanceRequestResponse>>> getMyMaintenanceRequests() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByManager(user);
            List<MaintenanceRequestResponse> responses = requests.stream()
                    .map(MaintenanceRequestResponse::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, responses, "Maintenance requests retrieved successfully"));
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
    public ResponseEntity<ApiResponse<List<MaintenanceRequestResponse>>> getMaintenanceRequestsByStatus(@PathVariable MaintenanceRequest.Status status) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByStatus(status, user);
            List<MaintenanceRequestResponse> responses = requests.stream()
                    .map(MaintenanceRequestResponse::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, responses, "Maintenance requests by status retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve maintenance requests by status: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/priority/{priority}")
    public ResponseEntity<ApiResponse<List<MaintenanceRequestResponse>>> getMaintenanceRequestsByPriority(@PathVariable MaintenanceRequest.Priority priority) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByPriority(priority, user);
            List<MaintenanceRequestResponse> responses = requests.stream()
                    .map(MaintenanceRequestResponse::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, responses, "Maintenance requests by priority retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve maintenance requests by priority: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<MaintenanceRequestResponse>>> getMaintenanceRequestsByCategory(@PathVariable UUID categoryId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByServiceCategory(categoryId, user);
            List<MaintenanceRequestResponse> responses = requests.stream()
                    .map(MaintenanceRequestResponse::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, responses, "Maintenance requests by category retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve maintenance requests by category: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/apartment/{apartmentId}")
    public ResponseEntity<ApiResponse<List<MaintenanceRequestResponse>>> getMaintenanceRequestsByApartment(@PathVariable UUID apartmentId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByApartment(apartmentId, user);
            List<MaintenanceRequestResponse> responses = requests.stream()
                    .map(MaintenanceRequestResponse::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, responses, "Maintenance requests by apartment retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve maintenance requests by apartment: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/assigned")
    public ResponseEntity<ApiResponse<List<MaintenanceRequestResponse>>> getAssignedMaintenanceRequests() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByAssignee(user);
            List<MaintenanceRequestResponse> responses = requests.stream()
                    .map(MaintenanceRequestResponse::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, responses, "Assigned maintenance requests retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve assigned maintenance requests: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/search")
    public ResponseEntity<ApiResponse<List<MaintenanceRequestResponse>>> searchMaintenanceRequests(@RequestParam String searchText) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<MaintenanceRequest> requests = maintenanceRequestService.searchMaintenanceRequests(searchText, user);
            List<MaintenanceRequestResponse> responses = requests.stream()
                    .map(MaintenanceRequestResponse::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, responses, "Maintenance requests search completed"));
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

    // NEW ENDPOINTS FOR ENHANCED MAINTENANCE MANAGEMENT

    @GetMapping("/requests/{id}/details")
    public ResponseEntity<ApiResponse<MaintenanceDetailsResponse>> getMaintenanceRequestDetails(@PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            MaintenanceDetailsResponse details = maintenanceRequestService.getMaintenanceRequestDetails(id, user);
            return ResponseEntity.ok(new ApiResponse<>(true, details, "Maintenance request details retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve maintenance request details: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/{id}/progress")
    public ResponseEntity<ApiResponse<List<MaintenanceProgressResponse>>> getMaintenanceRequestProgress(@PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<MaintenanceProgressResponse> progress = maintenanceRequestService.getMaintenanceRequestProgress(id, user);
            return ResponseEntity.ok(new ApiResponse<>(true, progress, "Maintenance request progress retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve maintenance request progress: " + e.getMessage()));
        }
    }

    @PutMapping("/requests/{id}/status")
    public ResponseEntity<ApiResponse<MaintenanceProgressResponse>> updateMaintenanceRequestStatus(
            @PathVariable UUID id,
            @Valid @RequestBody MaintenanceStatusUpdateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            // Only managers can update status
            if (!user.getRole().equals(UserRole.PROPERTY_MANAGER)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Only property managers can update maintenance request status"));
            }

            MaintenanceProgressResponse progress = maintenanceRequestService.updateMaintenanceRequestStatus(id, request, user);
            return ResponseEntity.ok(new ApiResponse<>(true, progress, "Maintenance request status updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to update maintenance request status: " + e.getMessage()));
        }
    }
}