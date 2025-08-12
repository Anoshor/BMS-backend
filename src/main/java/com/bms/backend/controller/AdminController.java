package com.bms.backend.controller;

import com.bms.backend.dto.request.ManagerApprovalRequest;
import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.dto.response.ManagerApprovalDto;
import com.bms.backend.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/managers/pending")
    public ResponseEntity<ApiResponse<List<ManagerApprovalDto>>> getPendingManagers() {
        try {
            List<ManagerApprovalDto> pendingManagers = adminService.getPendingManagers();
            return ResponseEntity.ok(ApiResponse.success(pendingManagers, "Pending managers retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve pending managers"));
        }
    }

    @GetMapping("/managers")
    public ResponseEntity<ApiResponse<List<ManagerApprovalDto>>> getAllManagers() {
        try {
            List<ManagerApprovalDto> managers = adminService.getAllManagers();
            return ResponseEntity.ok(ApiResponse.success(managers, "All managers retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve managers"));
        }
    }

    @PostMapping("/managers/approve")
    public ResponseEntity<ApiResponse<ManagerApprovalDto>> approveOrRejectManager(
            @RequestBody @Valid ManagerApprovalRequest request) {
        
        try {
            // Validate rejection reason if action is REJECT
            if ("REJECT".equals(request.getAction()) && 
                (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Rejection reason is required when rejecting a manager"));
            }
            
            ManagerApprovalDto result = adminService.approveOrRejectManager(request);
            
            String message = "APPROVE".equals(request.getAction()) ? 
                    "Manager approved successfully" : "Manager rejected successfully";
            
            return ResponseEntity.ok(ApiResponse.success(result, message));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to process manager approval"));
        }
    }

    @GetMapping("/managers/{email}/status")
    public ResponseEntity<ApiResponse<ManagerApprovalDto>> getManagerStatus(
            @PathVariable String email) {
        
        try {
            ManagerApprovalDto manager = adminService.getManagerStatus(email);
            return ResponseEntity.ok(ApiResponse.success(manager, "Manager status retrieved successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve manager status"));
        }
    }
}