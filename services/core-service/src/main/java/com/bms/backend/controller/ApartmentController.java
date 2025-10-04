package com.bms.backend.controller;

import com.bms.backend.dto.request.ApartmentRequest;
import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.entity.Apartment;
import com.bms.backend.entity.User;
import com.bms.backend.service.ApartmentService;
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
@RequestMapping("/api/v1/apartments")
public class ApartmentController {

    @Autowired
    private ApartmentService apartmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<Apartment>> createApartment(@Valid @RequestBody ApartmentRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            Apartment apartment = apartmentService.createApartment(request, user);
            return ResponseEntity.ok(new ApiResponse<>(true, apartment, "Apartment created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to create apartment: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Apartment>>> getMyApartments() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<Apartment> apartments = apartmentService.getApartmentsByManager(user);
            return ResponseEntity.ok(new ApiResponse<>(true, apartments, "Apartments retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve apartments: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Apartment>>> searchMyApartments(@RequestParam String searchText) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<Apartment> apartments = apartmentService.searchApartmentsByManager(user, searchText);
            return ResponseEntity.ok(new ApiResponse<>(true, apartments, "Apartments search completed"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to search apartments: " + e.getMessage()));
        }
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<ApiResponse<List<Apartment>>> getApartmentsByProperty(@PathVariable UUID propertyId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<Apartment> apartments = apartmentService.getApartmentsByProperty(propertyId, user);
            return ResponseEntity.ok(new ApiResponse<>(true, apartments, "Apartments retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve apartments: " + e.getMessage()));
        }
    }

    @GetMapping("/occupied")
    public ResponseEntity<ApiResponse<List<Apartment>>> getOccupiedApartments() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<Apartment> apartments = apartmentService.getOccupiedApartmentsByManager(user);
            return ResponseEntity.ok(new ApiResponse<>(true, apartments, "Occupied apartments retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve occupied apartments: " + e.getMessage()));
        }
    }

    @GetMapping("/unoccupied")
    public ResponseEntity<ApiResponse<List<Apartment>>> getUnoccupiedApartments() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<Apartment> apartments = apartmentService.getUnoccupiedApartmentsByManager(user);
            return ResponseEntity.ok(new ApiResponse<>(true, apartments, "Unoccupied apartments retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve unoccupied apartments: " + e.getMessage()));
        }
    }

    @GetMapping("/tenant/search")
    public ResponseEntity<ApiResponse<List<Apartment>>> searchApartmentsByTenant(
            @RequestParam(required = false) String tenantName,
            @RequestParam(required = false) String tenantEmail,
            @RequestParam(required = false) String tenantPhone) {
        try {
            List<Apartment> apartments = apartmentService.getApartmentsByTenantInfo(tenantName, tenantEmail, tenantPhone);
            return ResponseEntity.ok(new ApiResponse<>(true, apartments, "Tenant search completed"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to search tenants: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Apartment>> getApartmentById(@PathVariable UUID id) {
        try {
            Optional<Apartment> apartment = apartmentService.getApartmentById(id);
            if (apartment.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, apartment.get(), "Apartment retrieved successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Apartment not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve apartment: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Apartment>> updateApartment(@PathVariable UUID id, @Valid @RequestBody ApartmentRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            Apartment apartment = apartmentService.updateApartment(id, request, user);
            return ResponseEntity.ok(new ApiResponse<>(true, apartment, "Apartment updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to update apartment: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteApartment(@PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            apartmentService.deleteApartment(id, user);
            return ResponseEntity.ok(new ApiResponse<>(true, null, "Apartment deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to delete apartment: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/tenant")
    public ResponseEntity<ApiResponse<Apartment>> assignTenant(
            @PathVariable UUID id,
            @RequestParam String tenantName,
            @RequestParam String tenantEmail,
            @RequestParam String tenantPhone) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            Apartment apartment = apartmentService.assignTenant(id, tenantName, tenantEmail, tenantPhone, user);
            return ResponseEntity.ok(new ApiResponse<>(true, apartment, "Tenant assigned successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to assign tenant: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/tenant")
    public ResponseEntity<ApiResponse<Apartment>> removeTenant(@PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            Apartment apartment = apartmentService.removeTenant(id, user);
            return ResponseEntity.ok(new ApiResponse<>(true, apartment, "Tenant removed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to remove tenant: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/upload-images")
    public ResponseEntity<ApiResponse<Apartment>> uploadApartmentImages(
            @PathVariable UUID id,
            @RequestParam("images") List<org.springframework.web.multipart.MultipartFile> images) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            Apartment apartment = apartmentService.uploadApartmentImages(id, images, user);
            return ResponseEntity.ok(new ApiResponse<>(true, apartment, "Apartment images uploaded successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to upload images: " + e.getMessage()));
        }
    }
}