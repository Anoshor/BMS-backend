package com.bms.backend.controller;

import com.bms.backend.dto.request.PaymentSearchRequest;
import com.bms.backend.dto.request.RecordPaymentRequest;
import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.dto.response.PaymentTransactionDto;
import com.bms.backend.entity.PaymentTransaction;
import com.bms.backend.entity.User;
import com.bms.backend.service.PaymentTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
@Tag(name = "Payment Transactions", description = "APIs for managing tenant payment transactions")
public class PaymentTransactionController {

    @Autowired
    private PaymentTransactionService paymentService;

    /**
     * Get all payment transactions for the logged-in tenant
     * Supports filtering by status and date range
     */
    @PostMapping("/search")
    @Operation(
            summary = "Search payment transactions",
            description = "Get all payment transactions for a tenant with optional filters (status, date range)"
    )
    public ResponseEntity<ApiResponse<List<PaymentTransactionDto>>> searchPayments(
            @RequestBody(required = false) PaymentSearchRequest searchRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<PaymentTransactionDto> payments = paymentService.getTenantPayments(user, searchRequest);
            return ResponseEntity.ok(new ApiResponse<>(true, payments,
                    "Payment transactions retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null,
                            "Failed to retrieve payment transactions: " + e.getMessage()));
        }
    }

    /**
     * Get all payment transactions for the logged-in tenant (simple GET version)
     */
    @GetMapping
    @Operation(
            summary = "Get all payment transactions",
            description = "Get all payment transactions for the logged-in tenant"
    )
    public ResponseEntity<ApiResponse<List<PaymentTransactionDto>>> getAllPayments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(required = false, defaultValue = "ALL") String status) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            PaymentSearchRequest searchRequest = new PaymentSearchRequest(startDate, endDate, status, null);

            List<PaymentTransactionDto> payments = paymentService.getTenantPayments(user, searchRequest);
            return ResponseEntity.ok(new ApiResponse<>(true, payments,
                    "Payment transactions retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null,
                            "Failed to retrieve payment transactions: " + e.getMessage()));
        }
    }

    /**
     * Get paid payment transactions
     */
    @GetMapping("/paid")
    @Operation(
            summary = "Get paid transactions",
            description = "Get all paid payment transactions for the logged-in tenant"
    )
    public ResponseEntity<ApiResponse<List<PaymentTransactionDto>>> getPaidPayments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<PaymentTransactionDto> payments = paymentService.getPaidPayments(user, startDate, endDate);
            return ResponseEntity.ok(new ApiResponse<>(true, payments,
                    "Paid transactions retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null,
                            "Failed to retrieve paid transactions: " + e.getMessage()));
        }
    }

    /**
     * Get pending payment transactions
     */
    @GetMapping("/pending")
    @Operation(
            summary = "Get pending transactions",
            description = "Get all pending payment transactions for the logged-in tenant"
    )
    public ResponseEntity<ApiResponse<List<PaymentTransactionDto>>> getPendingPayments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<PaymentTransactionDto> payments = paymentService.getPendingPayments(user, startDate, endDate);
            return ResponseEntity.ok(new ApiResponse<>(true, payments,
                    "Pending transactions retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null,
                            "Failed to retrieve pending transactions: " + e.getMessage()));
        }
    }

    /**
     * Get overdue payment transactions
     */
    @GetMapping("/overdue")
    @Operation(
            summary = "Get overdue transactions",
            description = "Get all overdue payment transactions for the logged-in tenant"
    )
    public ResponseEntity<ApiResponse<List<PaymentTransactionDto>>> getOverduePayments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        System.out.println("🎯 CONTROLLER: /api/v1/payments/overdue endpoint called!");
        System.out.println("   startDate: " + startDate + ", endDate: " + endDate);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<PaymentTransactionDto> payments = paymentService.getOverduePayments(user, startDate, endDate);
            System.out.println("   Returning " + payments.size() + " overdue payments to UI");
            return ResponseEntity.ok(new ApiResponse<>(true, payments,
                    "Overdue transactions retrieved successfully"));
        } catch (IllegalArgumentException e) {
            System.out.println("   ❌ ERROR: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            System.out.println("   ❌ ERROR: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null,
                            "Failed to retrieve overdue transactions: " + e.getMessage()));
        }
    }

    /**
     * Get payment history for a specific lease/connection
     */
    @GetMapping("/lease/{connectionId}/history")
    @Operation(
            summary = "Get lease payment history",
            description = "Get all payment transactions for a specific lease/connection"
    )
    public ResponseEntity<ApiResponse<List<PaymentTransactionDto>>> getLeasePaymentHistory(
            @PathVariable UUID connectionId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            List<PaymentTransactionDto> payments = paymentService.getLeasePaymentHistory(user, connectionId);
            return ResponseEntity.ok(new ApiResponse<>(true, payments,
                    "Lease payment history retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null,
                            "Failed to retrieve lease payment history: " + e.getMessage()));
        }
    }

    /**
     * Get a specific payment transaction by ID
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get payment transaction details",
            description = "Get details of a specific payment transaction"
    )
    public ResponseEntity<ApiResponse<PaymentTransactionDto>> getPaymentById(@PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            PaymentTransactionDto payment = paymentService.getPaymentById(user, id);
            return ResponseEntity.ok(new ApiResponse<>(true, payment,
                    "Payment transaction retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null,
                            "Failed to retrieve payment transaction: " + e.getMessage()));
        }
    }

    /**
     * Record a payment transaction (called by payment service webhook)
     * This endpoint should be secured and only accessible by the payment service
     */
    @PostMapping("/record")
    @Operation(
            summary = "Record payment transaction",
            description = "Record a payment transaction from payment service webhook"
    )
    public ResponseEntity<ApiResponse<PaymentTransaction>> recordPayment(
            @Valid @RequestBody RecordPaymentRequest request) {
        try {
            PaymentTransaction payment = paymentService.recordPayment(request);
            return ResponseEntity.ok(new ApiResponse<>(true, payment,
                    "Payment transaction recorded successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null,
                            "Failed to record payment transaction: " + e.getMessage()));
        }
    }
}
