package com.bms.backend.controller;

import com.bms.backend.dto.request.LeaseUpdateRequest;
import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.dto.response.LeaseDetailsDto;
import com.bms.backend.dto.response.LeaseListingDto;
import com.bms.backend.entity.TenantPropertyConnection;
import com.bms.backend.entity.User;
import com.bms.backend.service.LeaseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leases")
@CrossOrigin(origins = "*")
public class LeaseController {

    @Autowired
    private LeaseService leaseService;

    // CREATE operation is handled by /tenants/connect endpoint

    // READ - Get all leases (Manager view)
    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaseListingDto>>> getAllLeases(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String propertyName,
            @RequestParam(required = false) String tenantName) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<LeaseListingDto> leases = leaseService.getAllLeases(user, status, propertyName, tenantName);
            return ResponseEntity.ok(new ApiResponse<>(true, leases, "Leases retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve leases: " + e.getMessage()));
        }
    }

    // READ - Get specific lease details
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaseDetailsDto>> getLeaseById(@PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            LeaseDetailsDto lease = leaseService.getLeaseById(user, id);
            return ResponseEntity.ok(new ApiResponse<>(true, lease, "Lease details retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve lease details: " + e.getMessage()));
        }
    }

    // UPDATE - Update lease details (Manager only)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantPropertyConnection>> updateLease(
            @PathVariable UUID id,
            @Valid @RequestBody LeaseUpdateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            TenantPropertyConnection updatedLease = leaseService.updateLease(user, id, request);
            return ResponseEntity.ok(new ApiResponse<>(true, updatedLease, "Lease updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to update lease: " + e.getMessage()));
        }
    }

    // DELETE - Terminate lease (Soft delete - set isActive to false)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> terminateLease(@PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            leaseService.terminateLease(user, id);
            return ResponseEntity.ok(new ApiResponse<>(true, null, "Lease terminated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to terminate lease: " + e.getMessage()));
        }
    }

    // SEARCH - Search leases
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<LeaseListingDto>>> searchLeases(@RequestParam String searchText) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<LeaseListingDto> leases = leaseService.searchLeases(user, searchText);
            return ResponseEntity.ok(new ApiResponse<>(true, leases, "Lease search completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to search leases: " + e.getMessage()));
        }
    }

    // Get active leases
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<LeaseListingDto>>> getActiveLeases() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<LeaseListingDto> leases = leaseService.getActiveLeases(user);
            return ResponseEntity.ok(new ApiResponse<>(true, leases, "Active leases retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve active leases: " + e.getMessage()));
        }
    }

    // Get expired leases
    @GetMapping("/expired")
    public ResponseEntity<ApiResponse<List<LeaseListingDto>>> getExpiredLeases() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<LeaseListingDto> leases = leaseService.getExpiredLeases(user);
            return ResponseEntity.ok(new ApiResponse<>(true, leases, "Expired leases retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve expired leases: " + e.getMessage()));
        }
    }

    // Get upcoming leases
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<LeaseListingDto>>> getUpcomingLeases() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<LeaseListingDto> leases = leaseService.getUpcomingLeases(user);
            return ResponseEntity.ok(new ApiResponse<>(true, leases, "Upcoming leases retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve upcoming leases: " + e.getMessage()));
        }
    }

    // Get upcoming lease expirations (expiring in next 3 months)
    @GetMapping("/expiring-soon")
    public ResponseEntity<ApiResponse<List<LeaseListingDto>>> getUpcomingExpirations(
            @RequestParam(defaultValue = "3") int months) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<LeaseListingDto> leases = leaseService.getUpcomingExpirations(user, months);
            return ResponseEntity.ok(new ApiResponse<>(true, leases, "Upcoming lease expirations retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve upcoming expirations: " + e.getMessage()));
        }
    }

    // Reactivate terminated lease
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponse<TenantPropertyConnection>> reactivateLease(@PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            TenantPropertyConnection lease = leaseService.reactivateLease(user, id);
            return ResponseEntity.ok(new ApiResponse<>(true, lease, "Lease reactivated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to reactivate lease: " + e.getMessage()));
        }
    }
}