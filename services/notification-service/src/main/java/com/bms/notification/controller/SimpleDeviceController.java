package com.bms.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/devices")
@CrossOrigin(origins = "*")
public class SimpleDeviceController {

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerDevice(@RequestBody Map<String, Object> request) {
        
        System.out.println("Registering device: " + request);
        
        Object userId = request.get("userId");
        Object deviceToken = request.get("deviceToken");
        Object platform = request.get("platform");
        
        System.out.println("Registering device for user: " + userId + ", platform: " + platform);
        System.out.println("Device token: " + deviceToken);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Device registered successfully");
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/unregister")
    public ResponseEntity<Map<String, Object>> unregisterDevice(@RequestParam String deviceToken) {
        
        System.out.println("Unregistering device with token: " + deviceToken);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Device unregistered successfully");
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDevices(@PathVariable String userId) {
        
        System.out.println("Getting devices for user: " + userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Devices retrieved successfully");
        response.put("data", "[]"); // Empty array for now
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
}