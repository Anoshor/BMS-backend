package com.bms.backend.controller;

import com.bms.backend.dto.request.ConnectTenantRequest;
import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.dto.response.LeaseDetailsDto;
import com.bms.backend.dto.response.TenantConnectionDto;
import com.bms.backend.dto.response.TenantPropertyDto;
import com.bms.backend.dto.response.UserDto;
import com.bms.backend.entity.TenantPropertyConnection;
import com.bms.backend.entity.User;
import com.bms.backend.service.TenantService;
import com.bms.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/tenants")
@CrossOrigin(origins = "*")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private UserService userService;

    @PostMapping("/connect")
    public ResponseEntity<ApiResponse<String>> connectTenantToProperty(
            @RequestBody @Valid ConnectTenantRequest request,
            BindingResult bindingResult) {

        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                for (FieldError error : bindingResult.getFieldErrors()) {
                    errors.put(error.getField(), error.getDefaultMessage());
                }
                String errorMessage = "Validation failed: " + errors.toString();
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(errorMessage));
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User manager = (User) authentication.getPrincipal();

            tenantService.connectTenantToProperty(manager, request);
            return ResponseEntity.ok(ApiResponse.success(null, "Tenant connected to property successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to connect tenant to property: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TenantPropertyConnection>>> searchTenants(
            @RequestParam(required = false) String searchText) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User manager = (User) authentication.getPrincipal();

            List<TenantPropertyConnection> connections = tenantService.searchTenants(manager, searchText);
            return ResponseEntity.ok(ApiResponse.success(connections, "Tenants retrieved successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to search tenants"));
        }
    }

    @GetMapping("/search/global")
    public ResponseEntity<ApiResponse<List<UserDto>>> searchTenantsGlobal(
            @RequestParam(required = false) String searchText) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User manager = (User) authentication.getPrincipal();

            List<User> tenants = tenantService.searchTenantsGlobal(manager, searchText);
            List<UserDto> tenantDtos = tenants.stream().map(UserDto::from).toList();
            return ResponseEntity.ok(ApiResponse.success(tenantDtos, "Global tenant search completed successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to search tenants globally"));
        }
    }

    @GetMapping("/connections")
    public ResponseEntity<ApiResponse<List<TenantConnectionDto>>> getManagerTenantConnections(
            @RequestParam(required = false) String searchText) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User manager = (User) authentication.getPrincipal();

            List<TenantConnectionDto> connections = tenantService.getManagerTenantConnections(manager, searchText);
            return ResponseEntity.ok(ApiResponse.success(connections, "Tenant connections retrieved successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve tenant connections"));
        }
    }

    @GetMapping("/my-properties")
    public ResponseEntity<ApiResponse<List<TenantPropertyDto>>> getTenantProperties() {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User tenant = (User) authentication.getPrincipal();

            List<TenantPropertyDto> properties = tenantService.getTenantPropertiesEnhanced(tenant);
            return ResponseEntity.ok(ApiResponse.success(properties, "Properties retrieved successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve properties"));
        }
    }

    @GetMapping("/lease/{connectionId}")
    public ResponseEntity<ApiResponse<LeaseDetailsDto>> getLeaseDetails(@PathVariable UUID connectionId) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            LeaseDetailsDto leaseDetails = tenantService.getLeaseDetails(user, connectionId);
            return ResponseEntity.ok(ApiResponse.success(leaseDetails, "Lease details retrieved successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve lease details"));
        }
    }
}