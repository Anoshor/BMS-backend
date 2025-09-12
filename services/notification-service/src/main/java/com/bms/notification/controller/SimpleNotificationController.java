package com.bms.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class SimpleNotificationController {

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendNotification(@RequestBody Map<String, Object> request) {
        
        System.out.println("Received notification request: " + request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Notification sent successfully");
        response.put("timestamp", LocalDateTime.now().toString());
        
        // Log the notification details
        Object userIds = request.get("userIds");
        Object title = request.get("title");
        Object body = request.get("body");
        Object type = request.get("type");
        
        System.out.println("Sending " + type + " notification to users: " + userIds);
        System.out.println("Title: " + title);
        System.out.println("Body: " + body);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-bulk")
    public ResponseEntity<Map<String, Object>> sendBulkNotification(@RequestBody Map<String, Object> request) {
        
        System.out.println("Received bulk notification request: " + request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Bulk notification queued successfully");
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Notification service is running");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", "OK");
        
        return ResponseEntity.ok(response);
    }
}