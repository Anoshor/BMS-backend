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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tenants")
@CrossOrigin(origins = "*")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private UserService userService;

    @PostMapping("/connect")
    public ResponseEntity<ApiResponse<String>> connectTenantToProperty(
            @RequestBody @Valid ConnectTenantRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Optional<User> managerOpt = userService.findByEmailOrPhone(userDetails.getUsername());
            if (managerOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Manager not found"));
            }

            tenantService.connectTenantToProperty(managerOpt.get(), request);
            return ResponseEntity.ok(ApiResponse.success(null, "Tenant connected to property successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to connect tenant to property"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TenantPropertyConnection>>> searchTenants(
            @RequestParam(required = false) String searchText,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Optional<User> managerOpt = userService.findByEmailOrPhone(userDetails.getUsername());
            if (managerOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Manager not found"));
            }

            List<TenantPropertyConnection> connections = tenantService.searchTenants(managerOpt.get(), searchText);
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
    public ResponseEntity<ApiResponse<List<TenantPropertyConnection>>> getTenantProperties(
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Optional<User> tenantOpt = userService.findByEmailOrPhone(userDetails.getUsername());
            if (tenantOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tenant not found"));
            }

            List<TenantPropertyConnection> properties = tenantService.getTenantProperties(tenantOpt.get());
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