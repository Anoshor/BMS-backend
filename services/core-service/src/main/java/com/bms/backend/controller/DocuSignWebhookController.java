package com.bms.backend.controller;

import com.bms.backend.service.DocuSignService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@Tag(name = "DocuSign Webhooks", description = "Webhook endpoints for DocuSign events")
public class DocuSignWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(DocuSignWebhookController.class);

    @Autowired
    private DocuSignService docuSignService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/docusign")
    @Operation(summary = "DocuSign webhook endpoint", description = "Receives envelope status updates from DocuSign Connect")
    public ResponseEntity<String> handleDocuSignWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-DocuSign-Signature-1", required = false) String signature) {

        logger.info("Received DocuSign webhook event");

        try {
            // Verify HMAC signature if configured
            if (!docuSignService.verifyWebhookSignature(payload, signature)) {
                logger.warn("Invalid DocuSign webhook signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }

            // Parse the webhook payload
            JsonNode rootNode = objectMapper.readTree(payload);

            // DocuSign Connect sends events in different formats
            // Handle the standard envelope status change format
            String envelopeId = extractEnvelopeId(rootNode);
            String status = extractStatus(rootNode);
            String eventTimestamp = extractEventTimestamp(rootNode);

            if (envelopeId == null || status == null) {
                logger.warn("Missing required fields in DocuSign webhook payload");
                return ResponseEntity.ok("OK - No action needed");
            }

            logger.info("Processing DocuSign event - EnvelopeId: {}, Status: {}", envelopeId, status);

            // Process the webhook event
            docuSignService.processWebhookEvent(envelopeId, status, eventTimestamp);

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            logger.error("Error processing DocuSign webhook", e);
            // Return 200 to prevent DocuSign from retrying, but log the error
            return ResponseEntity.ok("OK - Error logged");
        }
    }

    private String extractEnvelopeId(JsonNode rootNode) {
        // Try different paths based on DocuSign payload format

        // Standard Connect format
        if (rootNode.has("envelopeId")) {
            return rootNode.get("envelopeId").asText();
        }

        // Nested envelope format
        if (rootNode.has("data")) {
            JsonNode dataNode = rootNode.get("data");
            if (dataNode.has("envelopeId")) {
                return dataNode.get("envelopeId").asText();
            }
            if (dataNode.has("envelope") && dataNode.get("envelope").has("envelopeId")) {
                return dataNode.get("envelope").get("envelopeId").asText();
            }
        }

        // XML-style format converted to JSON
        if (rootNode.has("EnvelopeStatus")) {
            JsonNode envelopeStatus = rootNode.get("EnvelopeStatus");
            if (envelopeStatus.has("EnvelopeID")) {
                return envelopeStatus.get("EnvelopeID").asText();
            }
        }

        return null;
    }

    private String extractStatus(JsonNode rootNode) {
        // Try different paths based on DocuSign payload format

        // Standard Connect format
        if (rootNode.has("status")) {
            return rootNode.get("status").asText();
        }

        // Event type format
        if (rootNode.has("event")) {
            String event = rootNode.get("event").asText();
            // Convert event names to status
            return convertEventToStatus(event);
        }

        // Nested data format
        if (rootNode.has("data")) {
            JsonNode dataNode = rootNode.get("data");
            if (dataNode.has("status")) {
                return dataNode.get("status").asText();
            }
            if (dataNode.has("envelope") && dataNode.get("envelope").has("status")) {
                return dataNode.get("envelope").get("status").asText();
            }
        }

        // XML-style format
        if (rootNode.has("EnvelopeStatus")) {
            JsonNode envelopeStatus = rootNode.get("EnvelopeStatus");
            if (envelopeStatus.has("Status")) {
                return envelopeStatus.get("Status").asText();
            }
        }

        return null;
    }

    private String extractEventTimestamp(JsonNode rootNode) {
        // Try different paths
        if (rootNode.has("generatedDateTime")) {
            return rootNode.get("generatedDateTime").asText();
        }

        if (rootNode.has("data")) {
            JsonNode dataNode = rootNode.get("data");
            if (dataNode.has("completedDateTime")) {
                return dataNode.get("completedDateTime").asText();
            }
            if (dataNode.has("envelope") && dataNode.get("envelope").has("completedDateTime")) {
                return dataNode.get("envelope").get("completedDateTime").asText();
            }
        }

        if (rootNode.has("EnvelopeStatus") && rootNode.get("EnvelopeStatus").has("Completed")) {
            return rootNode.get("EnvelopeStatus").get("Completed").asText();
        }

        return null;
    }

    private String convertEventToStatus(String event) {
        if (event == null) return null;

        switch (event.toLowerCase()) {
            case "envelope-sent":
                return "sent";
            case "envelope-delivered":
                return "delivered";
            case "envelope-completed":
            case "envelope-signed":
                return "completed";
            case "envelope-declined":
            case "recipient-declined":
                return "declined";
            case "envelope-voided":
                return "voided";
            case "envelope-expired":
                return "expired";
            default:
                return event;
        }
    }
}
