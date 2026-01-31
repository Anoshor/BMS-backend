package com.bms.backend.controller;

import com.bms.backend.dto.request.EmbeddedSigningRequest;
import com.bms.backend.dto.request.SendLeaseForSigningRequest;
import com.bms.backend.dto.request.VoidEnvelopeRequest;
import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.dto.response.DocuSignEmbeddedSigningResponse;
import com.bms.backend.dto.response.LeaseSigningStatusResponse;
import com.bms.backend.dto.response.LeaseUploadResponse;
import com.bms.backend.entity.TenantPropertyConnection;
import com.bms.backend.entity.User;
import com.bms.backend.service.DocuSignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/docusign")
@CrossOrigin(origins = "*")
@Tag(name = "DocuSign Lease Signing", description = "APIs for DocuSign lease agreement signing")
public class DocuSignController {

    @Autowired
    private DocuSignService docuSignService;

    // ==================== Manager Endpoints ====================

    @PostMapping(value = "/leases/{connectionId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload lease document", description = "Upload a lease PDF document for a tenant connection")
    public ResponseEntity<ApiResponse<LeaseUploadResponse>> uploadLeaseDocument(
            @PathVariable UUID connectionId,
            @RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File cannot be empty"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Only PDF files are allowed"));
            }

            LeaseUploadResponse response = docuSignService.uploadLeaseDocument(connectionId, file, user);
            return ResponseEntity.ok(ApiResponse.success(response, "Lease document uploaded successfully"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload lease document: " + e.getMessage()));
        }
    }

    @PostMapping("/leases/{connectionId}/send")
    @Operation(summary = "Send lease for signing", description = "Create DocuSign envelope and send lease to tenant for signing")
    public ResponseEntity<ApiResponse<String>> sendLeaseForSigning(
            @PathVariable UUID connectionId,
            @Valid @RequestBody(required = false) SendLeaseForSigningRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            if (request == null) {
                request = new SendLeaseForSigningRequest();
            }

            String envelopeId = docuSignService.sendLeaseForSigning(connectionId, request, user);
            return ResponseEntity.ok(ApiResponse.success(envelopeId, "Lease sent for signing successfully"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to send lease for signing: " + e.getMessage()));
        }
    }

    @GetMapping("/leases/{connectionId}/status")
    @Operation(summary = "Get lease signing status", description = "Get the current signing status of a lease")
    public ResponseEntity<ApiResponse<LeaseSigningStatusResponse>> getLeaseSigningStatus(
            @PathVariable UUID connectionId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            LeaseSigningStatusResponse status = docuSignService.getLeaseSigningStatus(connectionId, user);
            return ResponseEntity.ok(ApiResponse.success(status, "Lease signing status retrieved successfully"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get lease signing status: " + e.getMessage()));
        }
    }

    @PutMapping("/leases/{connectionId}/void")
    @Operation(summary = "Void lease envelope", description = "Cancel/void the DocuSign envelope for a lease")
    public ResponseEntity<ApiResponse<String>> voidLeaseEnvelope(
            @PathVariable UUID connectionId,
            @Valid @RequestBody VoidEnvelopeRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            docuSignService.voidEnvelope(connectionId, user, request.getVoidReason());
            return ResponseEntity.ok(ApiResponse.success(null, "Lease envelope voided successfully"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to void lease envelope: " + e.getMessage()));
        }
    }

    @GetMapping("/leases/{connectionId}/download")
    @Operation(summary = "Download signed lease", description = "Download the signed lease document from DocuSign")
    public ResponseEntity<ApiResponse<String>> downloadSignedLease(@PathVariable UUID connectionId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            String signedDocumentUrl = docuSignService.downloadSignedDocument(connectionId, user);
            return ResponseEntity.ok(ApiResponse.success(signedDocumentUrl, "Signed lease downloaded successfully"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to download signed lease: " + e.getMessage()));
        }
    }

    // ==================== Tenant Endpoints ====================

    @GetMapping("/tenant/lease/signing-url")
    @Operation(summary = "Get embedded signing URL", description = "Get the DocuSign embedded signing URL for the tenant's pending lease")
    public ResponseEntity<ApiResponse<DocuSignEmbeddedSigningResponse>> getEmbeddedSigningUrl(
            @Valid @RequestBody EmbeddedSigningRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User tenant = (User) authentication.getPrincipal();

            // Find the tenant's pending lease
            TenantPropertyConnection pendingLease = docuSignService.findPendingLeaseForTenant(tenant);
            if (pendingLease == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No pending lease found for signing"));
            }

            DocuSignEmbeddedSigningResponse response = docuSignService.getEmbeddedSigningUrl(
                    pendingLease.getId(), tenant, request.getReturnUrl());
            return ResponseEntity.ok(ApiResponse.success(response, "Signing URL generated successfully"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get signing URL: " + e.getMessage()));
        }
    }

    @PostMapping("/tenant/lease/{connectionId}/signing-url")
    @Operation(summary = "Get embedded signing URL for specific lease", description = "Get the DocuSign embedded signing URL for a specific lease")
    public ResponseEntity<ApiResponse<DocuSignEmbeddedSigningResponse>> getEmbeddedSigningUrlForLease(
            @PathVariable UUID connectionId,
            @Valid @RequestBody EmbeddedSigningRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User tenant = (User) authentication.getPrincipal();

            DocuSignEmbeddedSigningResponse response = docuSignService.getEmbeddedSigningUrl(
                    connectionId, tenant, request.getReturnUrl());
            return ResponseEntity.ok(ApiResponse.success(response, "Signing URL generated successfully"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get signing URL: " + e.getMessage()));
        }
    }

    @GetMapping("/tenant/lease/status")
    @Operation(summary = "Get tenant's lease signing status", description = "Get the signing status of the tenant's pending lease")
    public ResponseEntity<ApiResponse<LeaseSigningStatusResponse>> getTenantLeaseStatus() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User tenant = (User) authentication.getPrincipal();

            TenantPropertyConnection pendingLease = docuSignService.findPendingLeaseForTenant(tenant);
            if (pendingLease == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No pending lease found"));
            }

            LeaseSigningStatusResponse status = docuSignService.getLeaseSigningStatus(pendingLease.getId(), tenant);
            return ResponseEntity.ok(ApiResponse.success(status, "Lease signing status retrieved successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get lease signing status: " + e.getMessage()));
        }
    }
}
