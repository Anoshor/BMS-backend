package com.bms.backend.service;

import com.bms.backend.dto.request.MaintenanceRequestCreateRequest;
import com.bms.backend.dto.request.MaintenanceRequestUpdateRequest;
import com.bms.backend.dto.request.MaintenanceUpdateRequest;
import com.bms.backend.dto.request.MaintenanceStatusUpdateRequest;
import com.bms.backend.dto.request.BulkMaintenanceRequestCreateRequest;
import com.bms.backend.dto.response.MaintenanceDetailsResponse;
import com.bms.backend.dto.response.MaintenanceProgressResponse;
import com.bms.backend.dto.response.MaintenancePhotoResponse;
import com.bms.backend.dto.response.BulkMaintenanceRequestResponse;
import com.bms.backend.entity.*;
import com.bms.backend.enums.UserRole;
import com.bms.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MaintenanceRequestService {

    @Autowired
    private MaintenanceRequestRepository maintenanceRequestRepository;

    @Autowired
    private ApartmentRepository apartmentRepository;

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MaintenanceUpdateRepository maintenanceUpdateRepository;

    @Autowired
    private MaintenanceRequestPhotoRepository maintenanceRequestPhotoRepository;

    @Autowired
    private S3Service s3Service;

    public MaintenanceRequest createMaintenanceRequest(MaintenanceRequestCreateRequest request, User requester) {
        Optional<Apartment> apartment = apartmentRepository.findById(request.getApartmentId());
        Optional<ServiceCategory> serviceCategory = serviceCategoryRepository.findById(request.getServiceCategoryId());

        if (apartment.isEmpty()) {
            throw new RuntimeException("Apartment not found");
        }
        if (serviceCategory.isEmpty()) {
            throw new RuntimeException("Service category not found");
        }

        MaintenanceRequest maintenanceRequest = new MaintenanceRequest();
        maintenanceRequest.setApartment(apartment.get());
        maintenanceRequest.setServiceCategory(serviceCategory.get());
        maintenanceRequest.setTitle(request.getTitle());
        maintenanceRequest.setDescription(request.getDescription());
        maintenanceRequest.setPriority(request.getPriority());
        maintenanceRequest.setStatus(MaintenanceRequest.Status.OPEN);
        maintenanceRequest.setRequester(requester);
        
        // Set tenant if apartment has tenant info
        if (apartment.get().getTenantEmail() != null && !apartment.get().getTenantEmail().trim().isEmpty()) {
            // For now, set tenant same as requester if requester is a tenant
            // In a more complex system, you might look up tenant by email
            maintenanceRequest.setTenant(requester);
        }
        
        maintenanceRequest.setCreatedAt(Instant.now());
        maintenanceRequest.setUpdatedAt(Instant.now());

        MaintenanceRequest savedRequest = maintenanceRequestRepository.save(maintenanceRequest);

        // Save photos if provided
        if (request.getPhotos() != null && !request.getPhotos().isEmpty()) {
            for (String photoData : request.getPhotos()) {
                MaintenanceRequestPhoto photo = new MaintenanceRequestPhoto();
                photo.setMaintenanceRequest(savedRequest);
                photo.setPhotoData(photoData);
                photo.setCreatedAt(Instant.now());
                maintenanceRequestPhotoRepository.save(photo);
            }
        }

        return savedRequest;
    }

    public List<MaintenanceRequest> getMaintenanceRequestsByManager(User manager) {
        return maintenanceRequestRepository.findByManagerWithDetails(manager);
    }

    public List<MaintenanceRequest> getMaintenanceRequestsByTenant(String tenantEmail) {
        return maintenanceRequestRepository.findByTenantEmail(tenantEmail);
    }

    public List<MaintenanceRequest> getMaintenanceRequestsByStatus(MaintenanceRequest.Status status, User manager) {
        return maintenanceRequestRepository.findByStatusAndApartmentPropertyManager(status, manager);
    }

    public List<MaintenanceRequest> getMaintenanceRequestsByPriority(MaintenanceRequest.Priority priority, User manager) {
        return maintenanceRequestRepository.findByPriorityAndApartmentPropertyManager(priority, manager);
    }

    public List<MaintenanceRequest> getMaintenanceRequestsByServiceCategory(UUID serviceCategoryId, User manager) {
        return maintenanceRequestRepository.findByServiceCategoryIdAndApartmentPropertyManager(serviceCategoryId, manager);
    }

    public List<MaintenanceRequest> getMaintenanceRequestsByApartment(UUID apartmentId, User manager) {
        return maintenanceRequestRepository.findByApartmentIdAndApartmentPropertyManager(apartmentId, manager);
    }

    public List<MaintenanceRequest> getMaintenanceRequestsByAssignee(User assignee) {
        return maintenanceRequestRepository.findByAssignedTo(assignee);
    }

    public Optional<MaintenanceRequest> getMaintenanceRequestById(UUID id) {
        return maintenanceRequestRepository.findByIdWithDetails(id);
    }

    public MaintenanceRequest updateMaintenanceRequest(UUID id, MaintenanceRequestUpdateRequest request, User updater) {
        Optional<MaintenanceRequest> existingRequest = maintenanceRequestRepository.findById(id);

        if (existingRequest.isEmpty()) {
            throw new RuntimeException("Maintenance request not found");
        }

        MaintenanceRequest maintenanceRequest = existingRequest.get();

        // Check authorization (only manager can update status)
        boolean isAuthorized = maintenanceRequest.getApartment().getProperty().getManager().getId().equals(updater.getId());

        if (!isAuthorized) {
            throw new RuntimeException("Not authorized to update this maintenance request");
        }

        // Update status
        maintenanceRequest.setStatus(request.getStatus());
        maintenanceRequest.setUpdatedAt(Instant.now());
        
        MaintenanceRequest updatedRequest = maintenanceRequestRepository.save(maintenanceRequest);
        
        // Add description as maintenance update if provided
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            MaintenanceUpdate update = new MaintenanceUpdate();
            update.setMaintenanceRequest(updatedRequest);
            update.setMessage(request.getDescription());
            update.setUpdateType(MaintenanceUpdate.UpdateType.STATUS_CHANGE);
            update.setUpdatedBy(updater);
            update.setCreatedAt(Instant.now());
            maintenanceUpdateRepository.save(update);
        }

        return updatedRequest;
    }

    public MaintenanceUpdate addUpdateToMaintenanceRequest(UUID maintenanceRequestId, MaintenanceUpdateRequest request, User updater) {
        Optional<MaintenanceRequest> maintenanceRequest = maintenanceRequestRepository.findById(maintenanceRequestId);

        if (maintenanceRequest.isEmpty()) {
            throw new RuntimeException("Maintenance request not found");
        }

        MaintenanceRequest request1 = maintenanceRequest.get();

        // Check authorization
        boolean isAuthorized = request1.getApartment().getProperty().getManager().getId().equals(updater.getId()) ||
                              (request1.getAssignedTo() != null && request1.getAssignedTo().getId().equals(updater.getId())) ||
                              request1.getRequester().getId().equals(updater.getId());

        if (!isAuthorized) {
            throw new RuntimeException("Not authorized to update this maintenance request");
        }

        MaintenanceUpdate update = new MaintenanceUpdate();
        update.setMaintenanceRequest(request1);
        update.setMessage(request.getMessage());
        update.setUpdateType(request.getUpdateType());
        update.setUpdatedBy(updater);
        update.setCreatedAt(Instant.now());

        return maintenanceUpdateRepository.save(update);
    }

    public List<MaintenanceUpdate> getUpdatesForMaintenanceRequest(UUID maintenanceRequestId) {
        return maintenanceUpdateRepository.findByMaintenanceRequestIdOrderByCreatedAtDesc(maintenanceRequestId);
    }

    public List<MaintenanceRequestPhoto> getPhotosForMaintenanceRequest(UUID maintenanceRequestId) {
        return maintenanceRequestPhotoRepository.findByMaintenanceRequestId(maintenanceRequestId);
    }

    public void deleteMaintenanceRequest(UUID id, User deleter) {
        Optional<MaintenanceRequest> maintenanceRequest = maintenanceRequestRepository.findById(id);

        if (maintenanceRequest.isPresent()) {
            MaintenanceRequest request = maintenanceRequest.get();

            // Only manager can delete
            if (request.getApartment().getProperty().getManager().getId().equals(deleter.getId())) {
                // Delete all maintenance request photos from S3 before deleting the request
                List<MaintenanceRequestPhoto> photos = maintenanceRequestPhotoRepository.findByMaintenanceRequestId(id);

                for (MaintenanceRequestPhoto photo : photos) {
                    if (photo.getPhotoUrl() != null && !photo.getPhotoUrl().trim().isEmpty()) {
                        try {
                            s3Service.deleteFile(photo.getPhotoUrl());
                            System.out.println("✅ Deleted maintenance photo from S3: " + photo.getPhotoUrl());
                        } catch (Exception e) {
                            // Log error but continue - photo might not exist in S3
                            System.err.println("Failed to delete maintenance photo from S3: " + photo.getPhotoUrl() + " - " + e.getMessage());
                        }
                    }
                }

                // Delete the maintenance request (cascade will delete photos and updates)
                maintenanceRequestRepository.deleteById(id);
            } else {
                throw new RuntimeException("Not authorized to delete this maintenance request");
            }
        } else {
            throw new RuntimeException("Maintenance request not found");
        }
    }

    public List<MaintenanceRequest> searchMaintenanceRequests(String searchText, User manager) {
        return maintenanceRequestRepository.findBySearchTextAndManager(searchText, manager);
    }

    // Tenant-specific methods for dashboard
    public List<MaintenanceRequest> getMaintenanceRequestsByTenantAndStatus(String tenantEmail, MaintenanceRequest.Status status) {
        return maintenanceRequestRepository.findByTenantEmailAndStatus(tenantEmail, status);
    }

    public List<MaintenanceRequest> getMaintenanceRequestsByTenantAndPriority(String tenantEmail, MaintenanceRequest.Priority priority) {
        return maintenanceRequestRepository.findByTenantEmailAndPriority(tenantEmail, priority);
    }

    public MaintenanceRequest getMaintenanceRequestByIdAndTenant(UUID id, String tenantEmail) {
        Optional<MaintenanceRequest> request = maintenanceRequestRepository.findById(id);
        if (request.isPresent() && 
            (request.get().getTenant() != null && request.get().getTenant().getEmail().equals(tenantEmail) ||
             request.get().getRequester().getEmail().equals(tenantEmail))) {
            return request.get();
        }
        return null;
    }

    public List<MaintenanceUpdate> getRecentUpdatesByTenant(String tenantEmail, int limit) {
        return maintenanceUpdateRepository.findRecentUpdatesByTenantEmail(tenantEmail, PageRequest.of(0, limit));
    }

    // NEW ENHANCED MAINTENANCE MANAGEMENT METHODS

    public MaintenanceDetailsResponse getMaintenanceRequestDetails(UUID id, User user) {
        Optional<MaintenanceRequest> requestOpt = maintenanceRequestRepository.findByIdWithDetails(id);

        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Maintenance request not found");
        }

        MaintenanceRequest request = requestOpt.get();

        // Check access permissions
        boolean hasAccess = false;
        if (user.getRole() == UserRole.PROPERTY_MANAGER) {
            // Manager can see requests for their properties
            hasAccess = request.getApartment().getProperty().getManager().getId().equals(user.getId());
        } else if (user.getRole() == UserRole.TENANT) {
            // Tenant can see their own requests
            hasAccess = (request.getTenant() != null && request.getTenant().getId().equals(user.getId())) ||
                       request.getRequester().getId().equals(user.getId());
        }

        if (!hasAccess) {
            throw new IllegalArgumentException("You don't have permission to view this maintenance request");
        }

        // Create response
        MaintenanceDetailsResponse response = new MaintenanceDetailsResponse(request);

        // Add photos
        if (request.getPhotos() != null && !request.getPhotos().isEmpty()) {
            List<MaintenancePhotoResponse> photoResponses = request.getPhotos().stream()
                    .map(MaintenancePhotoResponse::new)
                    .toList();
            response.setPhotos(photoResponses);
        }

        // Add progress history
        List<MaintenanceUpdate> updates = maintenanceUpdateRepository.findByMaintenanceRequestOrderByCreatedAtDesc(request);
        List<MaintenanceProgressResponse> progressResponses = updates.stream()
                .map(MaintenanceProgressResponse::new)
                .toList();
        response.setProgressHistory(progressResponses);

        return response;
    }

    public List<MaintenanceProgressResponse> getMaintenanceRequestProgress(UUID id, User user) {
        Optional<MaintenanceRequest> requestOpt = maintenanceRequestRepository.findByIdWithDetails(id);

        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Maintenance request not found");
        }

        MaintenanceRequest request = requestOpt.get();

        // Check access permissions (same logic as above)
        boolean hasAccess = false;
        if (user.getRole() == UserRole.PROPERTY_MANAGER) {
            hasAccess = request.getApartment().getProperty().getManager().getId().equals(user.getId());
        } else if (user.getRole() == UserRole.TENANT) {
            hasAccess = (request.getTenant() != null && request.getTenant().getId().equals(user.getId())) ||
                       request.getRequester().getId().equals(user.getId());
        }

        if (!hasAccess) {
            throw new IllegalArgumentException("You don't have permission to view this maintenance request progress");
        }

        // Get progress updates
        List<MaintenanceUpdate> updates = maintenanceUpdateRepository.findByMaintenanceRequestOrderByCreatedAtDesc(request);
        return updates.stream()
                .map(MaintenanceProgressResponse::new)
                .toList();
    }

    public MaintenanceProgressResponse updateMaintenanceRequestStatus(UUID id, MaintenanceStatusUpdateRequest request, User manager) {
        Optional<MaintenanceRequest> requestOpt = maintenanceRequestRepository.findByIdWithDetails(id);

        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Maintenance request not found");
        }

        MaintenanceRequest maintenanceRequest = requestOpt.get();

        // Check if manager has access to this property
        if (!maintenanceRequest.getApartment().getProperty().getManager().getId().equals(manager.getId())) {
            throw new IllegalArgumentException("You don't have permission to update this maintenance request");
        }

        // Update the maintenance request status
        MaintenanceRequest.Status oldStatus = maintenanceRequest.getStatus();
        maintenanceRequest.setStatus(request.getStatus());

        // Set resolved timestamp if status is RESOLVED
        if (request.getStatus() == MaintenanceRequest.Status.RESOLVED) {
            maintenanceRequest.setResolvedAt(Instant.now());
        }

        // Set submitted timestamp if status is SUBMITTED
        if (request.getStatus() == MaintenanceRequest.Status.SUBMITTED && oldStatus != MaintenanceRequest.Status.SUBMITTED) {
            maintenanceRequest.setSubmittedAt(Instant.now());
        }

        maintenanceRequestRepository.save(maintenanceRequest);

        // Create progress update entry
        MaintenanceUpdate update = new MaintenanceUpdate();
        update.setMaintenanceRequest(maintenanceRequest);
        update.setUser(manager);
        update.setUpdatedBy(manager);
        update.setUpdateType(MaintenanceUpdate.UpdateType.STATUS_CHANGE);
        update.setCurrentStatus(request.getStatus());
        update.setMessage("Status changed from " + oldStatus + " to " + request.getStatus());
        update.setNotes(request.getNotes());

        MaintenanceUpdate savedUpdate = maintenanceUpdateRepository.save(update);

        return new MaintenanceProgressResponse(savedUpdate);
    }

    public BulkMaintenanceRequestResponse createBulkMaintenanceRequests(BulkMaintenanceRequestCreateRequest request, User manager) {
        if (manager.getRole() != UserRole.PROPERTY_MANAGER) {
            throw new IllegalArgumentException("Only property managers can create bulk maintenance requests");
        }

        List<UUID> createdRequestIds = new ArrayList<>();
        List<String> failureMessages = new ArrayList<>();
        int totalCreated = 0;
        int totalFailed = 0;

        Optional<ServiceCategory> serviceCategory = serviceCategoryRepository.findById(request.getServiceCategoryId());
        if (serviceCategory.isEmpty()) {
            throw new RuntimeException("Service category not found");
        }

        for (UUID apartmentId : request.getApartmentIds()) {
            try {
                Optional<Apartment> apartment = apartmentRepository.findById(apartmentId);

                if (apartment.isEmpty()) {
                    failureMessages.add("Apartment not found for ID: " + apartmentId);
                    totalFailed++;
                    continue;
                }

                // Verify manager owns this apartment
                if (!apartment.get().getProperty().getManager().getId().equals(manager.getId())) {
                    failureMessages.add("You don't have permission to create maintenance requests for apartment: " + apartmentId);
                    totalFailed++;
                    continue;
                }

                MaintenanceRequest maintenanceRequest = new MaintenanceRequest();
                maintenanceRequest.setApartment(apartment.get());
                maintenanceRequest.setServiceCategory(serviceCategory.get());
                maintenanceRequest.setTitle(request.getTitle());
                maintenanceRequest.setDescription(request.getDescription());
                maintenanceRequest.setPriority(request.getPriority());
                maintenanceRequest.setStatus(MaintenanceRequest.Status.OPEN);
                maintenanceRequest.setRequester(manager);
                maintenanceRequest.setManagerInitiated(true);

                // Set tenant if apartment has tenant info
                if (apartment.get().getTenantEmail() != null && !apartment.get().getTenantEmail().trim().isEmpty()) {
                    // Try to find the tenant user by email
                    Optional<User> tenantUser = userRepository.findByEmail(apartment.get().getTenantEmail());
                    if (tenantUser.isPresent()) {
                        maintenanceRequest.setTenant(tenantUser.get());
                    }
                }

                maintenanceRequest.setCreatedAt(Instant.now());
                maintenanceRequest.setUpdatedAt(Instant.now());

                MaintenanceRequest savedRequest = maintenanceRequestRepository.save(maintenanceRequest);

                // Save photos if provided
                if (request.getPhotos() != null && !request.getPhotos().isEmpty()) {
                    for (String photoData : request.getPhotos()) {
                        MaintenanceRequestPhoto photo = new MaintenanceRequestPhoto();
                        photo.setMaintenanceRequest(savedRequest);
                        photo.setPhotoData(photoData);
                        photo.setCreatedAt(Instant.now());
                        maintenanceRequestPhotoRepository.save(photo);
                    }
                }

                createdRequestIds.add(savedRequest.getId());
                totalCreated++;

            } catch (Exception e) {
                failureMessages.add("Failed to create maintenance request for apartment " + apartmentId + ": " + e.getMessage());
                totalFailed++;
            }
        }

        return new BulkMaintenanceRequestResponse(createdRequestIds, totalCreated, totalFailed, failureMessages);
    }
}