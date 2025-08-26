package com.bms.backend.controller;

import com.bms.backend.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("")
public class HealthController {
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "Building Management System API");
        healthData.put("version", "1.0.0");
        healthData.put("timestamp", Instant.now());
        healthData.put("environment", "development");
        
        return ResponseEntity.ok(ApiResponse.success(healthData, "Service is running"));
    }
}