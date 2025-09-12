package com.bms.backend.controller;

import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.dto.response.MaintenanceRequestResponse;
import com.bms.backend.entity.*;
import com.bms.backend.service.MaintenanceRequestService;
import com.bms.backend.service.ServiceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tenant/dashboard")
@PreAuthorize("hasRole('TENANT')")
public class TenantDashboardController {

    @Autowired
    private MaintenanceRequestService maintenanceRequestService;

    @Autowired
    private ServiceCategoryService serviceCategoryService;

    @GetMapping("/maintenance/my-requests")
    public ResponseEntity<ApiResponse<List<MaintenanceRequestResponse>>> getMyMaintenanceRequests() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User tenant = (User) authentication.getPrincipal();

            List<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByTenant(tenant.getEmail());
            List<MaintenanceRequestResponse> responses = requests.stream()
                    .map(MaintenanceRequestResponse::new)
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(new ApiResponse<>(true, responses, "My maintenance requests retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve maintenance requests: " + e.getMessage()));
        }
    }

    @GetMapping("/maintenance/my-requests/status/{status}")
    public ResponseEntity<ApiResponse<List<MaintenanceRequestResponse>>> getMyMaintenanceRequestsByStatus(@PathVariable MaintenanceRequest.Status status) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User tenant = (User) authentication.getPrincipal();

            List<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByTenantAndStatus(tenant.getEmail(), status);
            List<MaintenanceRequestResponse> responses = requests.stream()
                    .map(MaintenanceRequestResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse<>(true, responses, "My maintenance requests by status retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve maintenance requests: " + e.getMessage()));
        }
    }

    @GetMapping("/maintenance/my-requests/priority/{priority}")
    public ResponseEntity<ApiResponse<List<MaintenanceRequestResponse>>> getMyMaintenanceRequestsByPriority(@PathVariable MaintenanceRequest.Priority priority) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User tenant = (User) authentication.getPrincipal();

            List<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByTenantAndPriority(tenant.getEmail(), priority);
            List<MaintenanceRequestResponse> responses = requests.stream()
                    .map(MaintenanceRequestResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse<>(true, responses, "My maintenance requests by priority retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve maintenance requests: " + e.getMessage()));
        }
    }

    @GetMapping("/maintenance/my-requests/{id}")
    public ResponseEntity<ApiResponse<MaintenanceRequestResponse>> getMyMaintenanceRequest(@PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User tenant = (User) authentication.getPrincipal();

            MaintenanceRequest request = maintenanceRequestService.getMaintenanceRequestByIdAndTenant(id, tenant.getEmail());
            if (request != null) {
                MaintenanceRequestResponse response = new MaintenanceRequestResponse(request);
                return ResponseEntity.ok(new ApiResponse<>(true, response, "Maintenance request retrieved successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Maintenance request not found or not authorized"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve maintenance request: " + e.getMessage()));
        }
    }

    @GetMapping("/maintenance/my-requests/{id}/updates")
    public ResponseEntity<ApiResponse<List<MaintenanceUpdate>>> getMyMaintenanceRequestUpdates(@PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User tenant = (User) authentication.getPrincipal();

            // First verify the request belongs to this tenant
            MaintenanceRequest request = maintenanceRequestService.getMaintenanceRequestByIdAndTenant(id, tenant.getEmail());
            if (request == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Maintenance request not found or not authorized"));
            }

            List<MaintenanceUpdate> updates = maintenanceRequestService.getUpdatesForMaintenanceRequest(id);
            return ResponseEntity.ok(new ApiResponse<>(true, updates, "Maintenance request updates retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve updates: " + e.getMessage()));
        }
    }

    @GetMapping("/maintenance/my-requests/{id}/photos")
    public ResponseEntity<ApiResponse<List<MaintenanceRequestPhoto>>> getMyMaintenanceRequestPhotos(@PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User tenant = (User) authentication.getPrincipal();

            // First verify the request belongs to this tenant
            MaintenanceRequest request = maintenanceRequestService.getMaintenanceRequestByIdAndTenant(id, tenant.getEmail());
            if (request == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Maintenance request not found or not authorized"));
            }

            List<MaintenanceRequestPhoto> photos = maintenanceRequestService.getPhotosForMaintenanceRequest(id);
            return ResponseEntity.ok(new ApiResponse<>(true, photos, "Maintenance request photos retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve photos: " + e.getMessage()));
        }
    }

    @GetMapping("/maintenance/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMaintenanceSummary() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User tenant = (User) authentication.getPrincipal();

            List<MaintenanceRequest> allRequests = maintenanceRequestService.getMaintenanceRequestsByTenant(tenant.getEmail());
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalRequests", allRequests.size());
            
            // Count by status
            Map<String, Long> statusCounts = allRequests.stream()
                    .collect(Collectors.groupingBy(req -> req.getStatus().toString(), Collectors.counting()));
            summary.put("statusBreakdown", statusCounts);
            
            // Count by priority
            Map<String, Long> priorityCounts = allRequests.stream()
                    .collect(Collectors.groupingBy(req -> req.getPriority().toString(), Collectors.counting()));
            summary.put("priorityBreakdown", priorityCounts);
            
            // Recent requests (last 5)
            List<MaintenanceRequestResponse> recentRequests = allRequests.stream()
                    .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                    .limit(5)
                    .map(MaintenanceRequestResponse::new)
                    .collect(Collectors.toList());
            summary.put("recentRequests", recentRequests);

            return ResponseEntity.ok(new ApiResponse<>(true, summary, "Maintenance summary retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve maintenance summary: " + e.getMessage()));
        }
    }

    @GetMapping("/maintenance/recent-updates")
    public ResponseEntity<ApiResponse<List<MaintenanceUpdate>>> getRecentUpdates(@RequestParam(defaultValue = "10") int limit) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User tenant = (User) authentication.getPrincipal();

            List<MaintenanceUpdate> updates = maintenanceRequestService.getRecentUpdatesByTenant(tenant.getEmail(), limit);
            return ResponseEntity.ok(new ApiResponse<>(true, updates, "Recent maintenance updates retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve recent updates: " + e.getMessage()));
        }
    }

    @GetMapping("/maintenance/categories")
    public ResponseEntity<ApiResponse<List<ServiceCategory>>> getServiceCategories() {
        try {
            List<ServiceCategory> categories = serviceCategoryService.getAllCategories();
            return ResponseEntity.ok(new ApiResponse<>(true, categories, "Service categories retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve service categories: " + e.getMessage()));
        }
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUnreadNotificationCount() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User tenant = (User) authentication.getPrincipal();

            // Count maintenance requests with recent updates (could be new notifications)
            List<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByTenant(tenant.getEmail());
            long unreadMaintenanceCount = requests.stream()
                    .filter(req -> req.getStatus() == MaintenanceRequest.Status.IN_PROGRESS || 
                                  req.getStatus() == MaintenanceRequest.Status.SUBMITTED)
                    .count();

            Map<String, Object> notificationCounts = new HashMap<>();
            notificationCounts.put("maintenanceUpdates", unreadMaintenanceCount);
            notificationCounts.put("totalUnread", unreadMaintenanceCount);

            return ResponseEntity.ok(new ApiResponse<>(true, notificationCounts, "Notification counts retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve notification counts: " + e.getMessage()));
        }
    }
}