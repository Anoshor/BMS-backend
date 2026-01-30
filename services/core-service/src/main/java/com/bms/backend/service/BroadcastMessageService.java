package com.bms.backend.service;

import com.bms.backend.dto.request.CreateBroadcastRequest;
import com.bms.backend.dto.response.BroadcastMessageResponse;
import com.bms.backend.dto.response.TenantBroadcastMessageResponse;
import com.bms.backend.entity.BroadcastMessage;
import com.bms.backend.entity.BroadcastMessageRecipient;
import com.bms.backend.entity.PropertyBuilding;
import com.bms.backend.entity.TenantPropertyConnection;
import com.bms.backend.entity.User;
import com.bms.backend.enums.BroadcastTargetType;
import com.bms.backend.enums.UserRole;
import com.bms.backend.repository.BroadcastMessageRecipientRepository;
import com.bms.backend.repository.BroadcastMessageRepository;
import com.bms.backend.repository.PropertyBuildingRepository;
import com.bms.backend.repository.TenantPropertyConnectionRepository;
import com.bms.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BroadcastMessageService {

    @Autowired
    private BroadcastMessageRepository broadcastMessageRepository;

    @Autowired
    private BroadcastMessageRecipientRepository recipientRepository;

    @Autowired
    private TenantPropertyConnectionRepository tenantConnectionRepository;

    @Autowired
    private PropertyBuildingRepository propertyBuildingRepository;

    @Autowired
    private UserRepository userRepository;

    // ==================== MANAGER METHODS ====================

    @Transactional
    public BroadcastMessageResponse createBroadcast(CreateBroadcastRequest request, User manager) {
        if (manager.getRole() != UserRole.PROPERTY_MANAGER && manager.getRole() != UserRole.BUILDING_OWNER) {
            throw new IllegalArgumentException("Only property managers can create broadcast messages");
        }

        // Validate expiration date is in the future
        if (request.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Expiration date must be in the future");
        }

        // Create the broadcast message
        BroadcastMessage message = new BroadcastMessage();
        message.setManager(manager);
        message.setTitle(request.getTitle());
        message.setContent(request.getContent());
        message.setPriority(request.getPriority());
        message.setTargetType(request.getTargetType());
        message.setTargetPropertyId(request.getTargetPropertyId());
        message.setActionUrl(request.getActionUrl());
        message.setExpiresAt(request.getExpiresAt());
        message.setIsActive(true);

        BroadcastMessage savedMessage = broadcastMessageRepository.save(message);

        // Resolve recipients based on target type
        List<User> recipients = resolveRecipients(request, manager);

        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("No valid recipients found for this broadcast");
        }

        // Create recipient records
        for (User tenant : recipients) {
            BroadcastMessageRecipient recipient = new BroadcastMessageRecipient(savedMessage, tenant);
            recipientRepository.save(recipient);
        }

        // Build response with stats
        BroadcastMessageResponse response = new BroadcastMessageResponse(savedMessage);
        response.setTotalRecipients((long) recipients.size());
        response.setReadCount(0L);
        response.setDismissedCount(0L);

        // Set property name if targeting specific property
        if (request.getTargetType() == BroadcastTargetType.SPECIFIC_PROPERTY && request.getTargetPropertyId() != null) {
            Optional<PropertyBuilding> property = propertyBuildingRepository.findById(request.getTargetPropertyId());
            property.ifPresent(p -> response.setTargetPropertyName(p.getName()));
        }

        return response;
    }

    public List<BroadcastMessageResponse> getManagerBroadcasts(User manager) {
        List<BroadcastMessage> messages = broadcastMessageRepository.findByManagerOrderByCreatedAtDesc(manager);

        return messages.stream()
                .map(message -> {
                    BroadcastMessageResponse response = new BroadcastMessageResponse(message);
                    response.setTotalRecipients(recipientRepository.countByBroadcastMessage(message));
                    response.setReadCount(recipientRepository.countReadByBroadcastMessage(message));
                    response.setDismissedCount(recipientRepository.countDismissedByBroadcastMessage(message));

                    // Set property name if applicable
                    if (message.getTargetPropertyId() != null) {
                        Optional<PropertyBuilding> property = propertyBuildingRepository.findById(message.getTargetPropertyId());
                        property.ifPresent(p -> response.setTargetPropertyName(p.getName()));
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }

    public BroadcastMessageResponse getBroadcastDetails(UUID messageId, User manager) {
        Optional<BroadcastMessage> messageOpt = broadcastMessageRepository.findById(messageId);

        if (messageOpt.isEmpty()) {
            throw new IllegalArgumentException("Broadcast message not found");
        }

        BroadcastMessage message = messageOpt.get();

        // Check ownership
        if (!message.getManager().getId().equals(manager.getId())) {
            throw new IllegalArgumentException("You don't have permission to view this broadcast");
        }

        BroadcastMessageResponse response = new BroadcastMessageResponse(message);
        response.setTotalRecipients(recipientRepository.countByBroadcastMessage(message));
        response.setReadCount(recipientRepository.countReadByBroadcastMessage(message));
        response.setDismissedCount(recipientRepository.countDismissedByBroadcastMessage(message));

        if (message.getTargetPropertyId() != null) {
            Optional<PropertyBuilding> property = propertyBuildingRepository.findById(message.getTargetPropertyId());
            property.ifPresent(p -> response.setTargetPropertyName(p.getName()));
        }

        return response;
    }

    @Transactional
    public BroadcastMessageResponse expireBroadcast(UUID messageId, User manager) {
        Optional<BroadcastMessage> messageOpt = broadcastMessageRepository.findById(messageId);

        if (messageOpt.isEmpty()) {
            throw new IllegalArgumentException("Broadcast message not found");
        }

        BroadcastMessage message = messageOpt.get();

        if (!message.getManager().getId().equals(manager.getId())) {
            throw new IllegalArgumentException("You don't have permission to expire this broadcast");
        }

        message.setIsActive(false);
        BroadcastMessage savedMessage = broadcastMessageRepository.save(message);

        BroadcastMessageResponse response = new BroadcastMessageResponse(savedMessage);
        response.setTotalRecipients(recipientRepository.countByBroadcastMessage(savedMessage));
        response.setReadCount(recipientRepository.countReadByBroadcastMessage(savedMessage));
        response.setDismissedCount(recipientRepository.countDismissedByBroadcastMessage(savedMessage));

        return response;
    }

    @Transactional
    public void deleteBroadcast(UUID messageId, User manager) {
        Optional<BroadcastMessage> messageOpt = broadcastMessageRepository.findById(messageId);

        if (messageOpt.isEmpty()) {
            throw new IllegalArgumentException("Broadcast message not found");
        }

        BroadcastMessage message = messageOpt.get();

        if (!message.getManager().getId().equals(manager.getId())) {
            throw new IllegalArgumentException("You don't have permission to delete this broadcast");
        }

        // Delete recipients first
        recipientRepository.deleteByBroadcastMessage(message);

        // Delete the message
        broadcastMessageRepository.delete(message);
    }

    // ==================== TENANT METHODS ====================

    public List<TenantBroadcastMessageResponse> getActiveMessagesForTenant(User tenant) {
        List<BroadcastMessageRecipient> recipients = recipientRepository.findActiveMessagesForTenant(tenant, Instant.now());

        return recipients.stream()
                .map(TenantBroadcastMessageResponse::new)
                .collect(Collectors.toList());
    }

    public List<TenantBroadcastMessageResponse> getAllMessagesForTenant(User tenant) {
        List<BroadcastMessageRecipient> recipients = recipientRepository.findAllMessagesForTenant(tenant);

        return recipients.stream()
                .map(TenantBroadcastMessageResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public TenantBroadcastMessageResponse markAsRead(UUID recipientId, User tenant) {
        Optional<BroadcastMessageRecipient> recipientOpt = recipientRepository.findById(recipientId);

        if (recipientOpt.isEmpty()) {
            throw new IllegalArgumentException("Message not found");
        }

        BroadcastMessageRecipient recipient = recipientOpt.get();

        if (!recipient.getTenant().getId().equals(tenant.getId())) {
            throw new IllegalArgumentException("You don't have permission to access this message");
        }

        recipient.markAsRead();
        BroadcastMessageRecipient savedRecipient = recipientRepository.save(recipient);

        return new TenantBroadcastMessageResponse(savedRecipient);
    }

    @Transactional
    public TenantBroadcastMessageResponse dismissMessage(UUID recipientId, User tenant) {
        Optional<BroadcastMessageRecipient> recipientOpt = recipientRepository.findById(recipientId);

        if (recipientOpt.isEmpty()) {
            throw new IllegalArgumentException("Message not found");
        }

        BroadcastMessageRecipient recipient = recipientOpt.get();

        if (!recipient.getTenant().getId().equals(tenant.getId())) {
            throw new IllegalArgumentException("You don't have permission to access this message");
        }

        recipient.dismiss();
        // Also mark as read when dismissing
        recipient.markAsRead();
        BroadcastMessageRecipient savedRecipient = recipientRepository.save(recipient);

        return new TenantBroadcastMessageResponse(savedRecipient);
    }

    public Long getUnreadCountForTenant(User tenant) {
        return recipientRepository.countUnreadForTenant(tenant, Instant.now());
    }

    // ==================== HELPER METHODS ====================

    private List<User> resolveRecipients(CreateBroadcastRequest request, User manager) {
        Set<User> recipients = new HashSet<>();

        switch (request.getTargetType()) {
            case SINGLE_TENANT:
                if (request.getTargetTenantIds() == null || request.getTargetTenantIds().isEmpty()) {
                    throw new IllegalArgumentException("Target tenant ID is required for single tenant targeting");
                }
                UUID tenantId = request.getTargetTenantIds().get(0);
                validateAndAddTenant(tenantId, manager, recipients);
                break;

            case MULTIPLE_TENANTS:
                if (request.getTargetTenantIds() == null || request.getTargetTenantIds().isEmpty()) {
                    throw new IllegalArgumentException("Target tenant IDs are required for multiple tenants targeting");
                }
                for (UUID id : request.getTargetTenantIds()) {
                    validateAndAddTenant(id, manager, recipients);
                }
                break;

            case SPECIFIC_PROPERTY:
                if (request.getTargetPropertyId() == null) {
                    throw new IllegalArgumentException("Target property ID is required for property targeting");
                }
                Optional<PropertyBuilding> propertyOpt = propertyBuildingRepository.findById(request.getTargetPropertyId());
                if (propertyOpt.isEmpty()) {
                    throw new IllegalArgumentException("Property not found");
                }
                PropertyBuilding property = propertyOpt.get();
                if (!property.getManager().getId().equals(manager.getId())) {
                    throw new IllegalArgumentException("You don't have permission to send broadcasts to this property");
                }
                // Get all active tenants connected to this property
                List<TenantPropertyConnection> propertyConnections =
                    tenantConnectionRepository.findByPropertyAndIsActiveOrderByCreatedAtDesc(property, true);
                for (TenantPropertyConnection connection : propertyConnections) {
                    recipients.add(connection.getTenant());
                }
                break;

            case ALL_TENANTS:
                // Get all active tenant connections for this manager
                List<TenantPropertyConnection> allConnections =
                    tenantConnectionRepository.findByManagerAndIsActive(manager, true);
                for (TenantPropertyConnection connection : allConnections) {
                    recipients.add(connection.getTenant());
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid target type");
        }

        return new ArrayList<>(recipients);
    }

    private void validateAndAddTenant(UUID tenantId, User manager, Set<User> recipients) {
        Optional<User> tenantOpt = userRepository.findById(tenantId);
        if (tenantOpt.isEmpty()) {
            throw new IllegalArgumentException("Tenant not found: " + tenantId);
        }

        User tenant = tenantOpt.get();

        // Verify tenant belongs to this manager
        List<TenantPropertyConnection> connections =
            tenantConnectionRepository.findByTenantAndManagerAndIsActive(tenant, manager, true);

        if (connections.isEmpty()) {
            throw new IllegalArgumentException("Tenant is not connected to you: " + tenantId);
        }

        recipients.add(tenant);
    }
}
