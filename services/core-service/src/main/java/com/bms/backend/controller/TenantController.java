package com.bms.backend.controller;

import com.bms.backend.dto.request.ConnectTenantRequest;
import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.entity.TenantPropertyConnection;
import com.bms.backend.entity.User;
import com.bms.backend.service.TenantService;
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
@RequestMapping("/tenants")
@CrossOrigin(origins = "*")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private UserService userService;

    @PostMapping("/connect")
    public ResponseEntity<ApiResponse<String>> connectTenantToProperty(
            @RequestBody @Valid ConnectTenantRequest request) {

        try {
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
                    .body(ApiResponse.error("Failed to connect tenant to property"));
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

    @GetMapping("/my-properties")
    public ResponseEntity<ApiResponse<List<TenantPropertyConnection>>> getTenantProperties() {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User tenant = (User) authentication.getPrincipal();

            List<TenantPropertyConnection> properties = tenantService.getTenantProperties(tenant);
            return ResponseEntity.ok(ApiResponse.success(properties, "Properties retrieved successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve properties"));
        }
    }
}