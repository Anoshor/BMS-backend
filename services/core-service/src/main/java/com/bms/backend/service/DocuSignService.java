package com.bms.backend.service;

import com.bms.backend.config.DocuSignConfig;
import com.bms.backend.dto.request.SendLeaseForSigningRequest;
import com.bms.backend.dto.response.DocuSignEmbeddedSigningResponse;
import com.bms.backend.dto.response.LeaseSigningStatusResponse;
import com.bms.backend.dto.response.LeaseUploadResponse;
import com.bms.backend.entity.TenantPropertyConnection;
import com.bms.backend.entity.User;
import com.bms.backend.enums.LeaseSigningStatus;
import com.bms.backend.repository.TenantPropertyConnectionRepository;
import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

@Service
public class DocuSignService {

    private static final Logger logger = LoggerFactory.getLogger(DocuSignService.class);

    @Autowired
    private DocuSignConfig docuSignConfig;

    @Autowired
    private TenantPropertyConnectionRepository connectionRepository;

    @Autowired
    private S3Service s3Service;

    @Transactional
    public LeaseUploadResponse uploadLeaseDocument(UUID connectionId, MultipartFile file, User manager) throws Exception {
        TenantPropertyConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Lease connection not found"));

        if (!connection.getManager().getId().equals(manager.getId())) {
            throw new SecurityException("You don't have permission to upload documents for this lease");
        }

        // Upload to S3
        String leaseUrl = s3Service.uploadFile(file, manager.getId(), S3Service.FileType.LEASE);

        // Update connection
        connection.setLeaseDocumentUrl(leaseUrl);
        connection.setLeaseSigningStatus(LeaseSigningStatus.DRAFT);
        connectionRepository.save(connection);

        return new LeaseUploadResponse(connectionId, leaseUrl, LeaseSigningStatus.DRAFT);
    }

    @Transactional
    public String sendLeaseForSigning(UUID connectionId, SendLeaseForSigningRequest request, User manager) throws Exception {
        if (!docuSignConfig.isEnabled()) {
            throw new IllegalStateException("DocuSign integration is not enabled");
        }

        TenantPropertyConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Lease connection not found"));

        if (!connection.getManager().getId().equals(manager.getId())) {
            throw new SecurityException("You don't have permission to send this lease for signing");
        }

        if (connection.getLeaseDocumentUrl() == null) {
            throw new IllegalStateException("No lease document uploaded. Please upload a document first.");
        }

        if (connection.getLeaseSigningStatus() != null &&
            connection.getLeaseSigningStatus() != LeaseSigningStatus.DRAFT &&
            connection.getLeaseSigningStatus() != LeaseSigningStatus.DECLINED &&
            connection.getLeaseSigningStatus() != LeaseSigningStatus.EXPIRED &&
            connection.getLeaseSigningStatus() != LeaseSigningStatus.VOIDED) {
            throw new IllegalStateException("Lease is already sent or signed. Current status: " +
                    connection.getLeaseSigningStatus().getDisplayName());
        }

        User tenant = connection.getTenant();
        ApiClient apiClient = docuSignConfig.getApiClient();
        EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);

        // Download lease document from S3
        byte[] documentBytes = downloadDocumentFromS3(connection.getLeaseDocumentUrl());

        // Create envelope definition
        EnvelopeDefinition envelopeDefinition = createEnvelopeDefinition(
                documentBytes,
                tenant,
                request.getEmailSubject(),
                request.getEmailBody()
        );

        // Create and send envelope
        EnvelopeSummary envelopeSummary = envelopesApi.createEnvelope(
                docuSignConfig.getAccountId(),
                envelopeDefinition
        );

        String envelopeId = envelopeSummary.getEnvelopeId();

        // Update connection
        connection.setDocusignEnvelopeId(envelopeId);
        connection.setLeaseSigningStatus(LeaseSigningStatus.SENT);
        connection.setDocusignSentAt(Instant.now());
        connectionRepository.save(connection);

        logger.info("Lease envelope created and sent. EnvelopeId: {}, ConnectionId: {}", envelopeId, connectionId);

        return envelopeId;
    }

    public DocuSignEmbeddedSigningResponse getEmbeddedSigningUrl(UUID connectionId, User tenant, String returnUrl) throws Exception {
        if (!docuSignConfig.isEnabled()) {
            throw new IllegalStateException("DocuSign integration is not enabled");
        }

        TenantPropertyConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Lease connection not found"));

        if (!connection.getTenant().getId().equals(tenant.getId())) {
            throw new SecurityException("You don't have permission to sign this lease");
        }

        if (connection.getDocusignEnvelopeId() == null) {
            throw new IllegalStateException("Lease has not been sent for signing yet");
        }

        LeaseSigningStatus status = connection.getLeaseSigningStatus();
        if (status == LeaseSigningStatus.SIGNED) {
            throw new IllegalStateException("Lease has already been signed");
        }

        if (status == LeaseSigningStatus.VOIDED || status == LeaseSigningStatus.EXPIRED) {
            throw new IllegalStateException("Lease envelope is no longer valid. Status: " + status.getDisplayName());
        }

        ApiClient apiClient = docuSignConfig.getApiClient();
        EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);

        RecipientViewRequest viewRequest = new RecipientViewRequest();
        viewRequest.setReturnUrl(returnUrl);
        viewRequest.setAuthenticationMethod("none");
        viewRequest.setEmail(tenant.getEmail());
        viewRequest.setUserName(tenant.getFirstName() + " " + tenant.getLastName());
        viewRequest.setClientUserId(tenant.getId().toString());

        ViewUrl viewUrl = envelopesApi.createRecipientView(
                docuSignConfig.getAccountId(),
                connection.getDocusignEnvelopeId(),
                viewRequest
        );

        return DocuSignEmbeddedSigningResponse.of(viewUrl.getUrl(), connection.getDocusignEnvelopeId());
    }

    public LeaseSigningStatusResponse getLeaseSigningStatus(UUID connectionId, User user) {
        TenantPropertyConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Lease connection not found"));

        // Check if user is either the manager or tenant
        boolean isManager = connection.getManager().getId().equals(user.getId());
        boolean isTenant = connection.getTenant().getId().equals(user.getId());

        if (!isManager && !isTenant) {
            throw new SecurityException("You don't have permission to view this lease status");
        }

        User tenantUser = connection.getTenant();

        return LeaseSigningStatusResponse.builder()
                .connectionId(connectionId)
                .leaseDocumentUrl(connection.getLeaseDocumentUrl())
                .docusignEnvelopeId(connection.getDocusignEnvelopeId())
                .leaseSigningStatus(connection.getLeaseSigningStatus())
                .docusignSentAt(connection.getDocusignSentAt())
                .docusignSignedAt(connection.getDocusignSignedAt())
                .signedDocumentUrl(connection.getSignedDocumentUrl())
                .tenantName(tenantUser.getFirstName() + " " + tenantUser.getLastName())
                .tenantEmail(tenantUser.getEmail())
                .propertyName(connection.getPropertyName())
                .build();
    }

    @Transactional
    public void voidEnvelope(UUID connectionId, User manager, String voidReason) throws Exception {
        if (!docuSignConfig.isEnabled()) {
            throw new IllegalStateException("DocuSign integration is not enabled");
        }

        TenantPropertyConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Lease connection not found"));

        if (!connection.getManager().getId().equals(manager.getId())) {
            throw new SecurityException("You don't have permission to void this envelope");
        }

        if (connection.getDocusignEnvelopeId() == null) {
            throw new IllegalStateException("No envelope exists for this lease");
        }

        LeaseSigningStatus status = connection.getLeaseSigningStatus();
        if (status == LeaseSigningStatus.SIGNED) {
            throw new IllegalStateException("Cannot void a signed lease");
        }

        if (status == LeaseSigningStatus.VOIDED) {
            throw new IllegalStateException("Envelope is already voided");
        }

        ApiClient apiClient = docuSignConfig.getApiClient();
        EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);

        Envelope envelope = new Envelope();
        envelope.setStatus("voided");
        envelope.setVoidedReason(voidReason);

        envelopesApi.update(docuSignConfig.getAccountId(), connection.getDocusignEnvelopeId(), envelope);

        connection.setLeaseSigningStatus(LeaseSigningStatus.VOIDED);
        connectionRepository.save(connection);

        logger.info("Envelope voided. EnvelopeId: {}, ConnectionId: {}, Reason: {}",
                connection.getDocusignEnvelopeId(), connectionId, voidReason);
    }

    @Transactional
    public String downloadSignedDocument(UUID connectionId, User manager) throws Exception {
        if (!docuSignConfig.isEnabled()) {
            throw new IllegalStateException("DocuSign integration is not enabled");
        }

        TenantPropertyConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Lease connection not found"));

        if (!connection.getManager().getId().equals(manager.getId())) {
            throw new SecurityException("You don't have permission to download this document");
        }

        if (connection.getLeaseSigningStatus() != LeaseSigningStatus.SIGNED) {
            throw new IllegalStateException("Lease has not been signed yet");
        }

        // If already downloaded and stored, return the existing URL
        if (connection.getSignedDocumentUrl() != null) {
            return connection.getSignedDocumentUrl();
        }

        ApiClient apiClient = docuSignConfig.getApiClient();
        EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);

        // Download the signed document
        byte[] documentBytes = envelopesApi.getDocument(
                docuSignConfig.getAccountId(),
                connection.getDocusignEnvelopeId(),
                "combined"
        );

        // Upload to S3
        String signedDocUrl = uploadSignedDocumentToS3(documentBytes, connectionId, manager.getId());

        connection.setSignedDocumentUrl(signedDocUrl);
        connectionRepository.save(connection);

        logger.info("Signed document downloaded and stored. ConnectionId: {}", connectionId);

        return signedDocUrl;
    }

    @Transactional
    public void processWebhookEvent(String envelopeId, String status, String eventTimestamp) {
        Optional<TenantPropertyConnection> connectionOpt = connectionRepository.findByDocusignEnvelopeId(envelopeId);

        if (connectionOpt.isEmpty()) {
            logger.warn("Received webhook for unknown envelope: {}", envelopeId);
            return;
        }

        TenantPropertyConnection connection = connectionOpt.get();
        LeaseSigningStatus newStatus = LeaseSigningStatus.fromDocuSignStatus(status);

        logger.info("Processing webhook event. EnvelopeId: {}, Status: {} -> {}, Timestamp: {}",
                envelopeId, connection.getLeaseSigningStatus(), newStatus, eventTimestamp);

        connection.setLeaseSigningStatus(newStatus);

        if (newStatus == LeaseSigningStatus.SIGNED && eventTimestamp != null) {
            connection.setDocusignSignedAt(Instant.parse(eventTimestamp));
        }

        connectionRepository.save(connection);
    }

    public boolean verifyWebhookSignature(String payload, String signature) {
        if (signature == null || signature.isEmpty()) {
            return false;
        }

        String webhookSecret = docuSignConfig.getWebhookSecret();
        if (webhookSecret == null || webhookSecret.isEmpty()) {
            logger.warn("Webhook secret not configured, skipping signature verification");
            return true; // Allow if not configured (for development)
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computedSignature = Base64.getEncoder().encodeToString(hmacBytes);
            return computedSignature.equals(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Error verifying webhook signature", e);
            return false;
        }
    }

    public TenantPropertyConnection findPendingLeaseForTenant(User tenant) {
        java.util.List<LeaseSigningStatus> pendingStatuses = Arrays.asList(
                LeaseSigningStatus.SENT,
                LeaseSigningStatus.DELIVERED
        );

        return connectionRepository.findByTenantAndIsActiveAndLeaseSigningStatusIn(
                tenant, true, pendingStatuses
        ).orElse(null);
    }

    private EnvelopeDefinition createEnvelopeDefinition(byte[] documentBytes, User tenant,
                                                         String emailSubject, String emailBody) {
        EnvelopeDefinition envelopeDefinition = new EnvelopeDefinition();
        envelopeDefinition.setEmailSubject(emailSubject);
        envelopeDefinition.setEmailBlurb(emailBody);
        envelopeDefinition.setStatus("sent");

        // Create document
        Document document = new Document();
        document.setDocumentBase64(Base64.getEncoder().encodeToString(documentBytes));
        document.setName("Lease Agreement");
        document.setFileExtension("pdf");
        document.setDocumentId("1");

        envelopeDefinition.setDocuments(Collections.singletonList(document));

        // Create signer
        Signer signer = new Signer();
        signer.setEmail(tenant.getEmail());
        signer.setName(tenant.getFirstName() + " " + tenant.getLastName());
        signer.setRecipientId("1");
        signer.setRoutingOrder("1");
        signer.setClientUserId(tenant.getId().toString()); // For embedded signing

        // Create signature tab (placed at bottom of last page)
        SignHere signHere = new SignHere();
        signHere.setDocumentId("1");
        signHere.setPageNumber("1");
        signHere.setRecipientId("1");
        signHere.setAnchorString("/sig1/"); // Look for anchor text or use coordinates
        signHere.setAnchorUnits("pixels");
        signHere.setAnchorXOffset("20");
        signHere.setAnchorYOffset("10");

        // Date signed tab
        DateSigned dateSigned = new DateSigned();
        dateSigned.setDocumentId("1");
        dateSigned.setPageNumber("1");
        dateSigned.setRecipientId("1");
        dateSigned.setAnchorString("/date1/");
        dateSigned.setAnchorUnits("pixels");
        dateSigned.setAnchorXOffset("20");
        dateSigned.setAnchorYOffset("10");

        Tabs tabs = new Tabs();
        tabs.setSignHereTabs(Collections.singletonList(signHere));
        tabs.setDateSignedTabs(Collections.singletonList(dateSigned));
        signer.setTabs(tabs);

        Recipients recipients = new Recipients();
        recipients.setSigners(Collections.singletonList(signer));
        envelopeDefinition.setRecipients(recipients);

        return envelopeDefinition;
    }

    private byte[] downloadDocumentFromS3(String documentUrl) throws Exception {
        try (InputStream inputStream = s3Service.downloadFile(documentUrl);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    private String uploadSignedDocumentToS3(byte[] documentBytes, UUID connectionId, UUID managerId) throws Exception {
        String fileName = "signed_lease_" + connectionId.toString() + "_" + System.currentTimeMillis() + ".pdf";
        return s3Service.uploadBytes(documentBytes, fileName, "application/pdf", managerId, S3Service.FileType.LEASE);
    }
}
