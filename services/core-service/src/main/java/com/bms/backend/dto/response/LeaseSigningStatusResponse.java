package com.bms.backend.dto.response;

import com.bms.backend.enums.LeaseSigningStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.UUID;

public class LeaseSigningStatusResponse {

    private UUID connectionId;
    private String leaseDocumentUrl;
    private String docusignEnvelopeId;
    private LeaseSigningStatus leaseSigningStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant docusignSentAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant docusignSignedAt;

    private String signedDocumentUrl;
    private String tenantName;
    private String tenantEmail;
    private String propertyName;

    public LeaseSigningStatusResponse() {}

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final LeaseSigningStatusResponse response = new LeaseSigningStatusResponse();

        public Builder connectionId(UUID connectionId) {
            response.connectionId = connectionId;
            return this;
        }

        public Builder leaseDocumentUrl(String leaseDocumentUrl) {
            response.leaseDocumentUrl = leaseDocumentUrl;
            return this;
        }

        public Builder docusignEnvelopeId(String docusignEnvelopeId) {
            response.docusignEnvelopeId = docusignEnvelopeId;
            return this;
        }

        public Builder leaseSigningStatus(LeaseSigningStatus leaseSigningStatus) {
            response.leaseSigningStatus = leaseSigningStatus;
            return this;
        }

        public Builder docusignSentAt(Instant docusignSentAt) {
            response.docusignSentAt = docusignSentAt;
            return this;
        }

        public Builder docusignSignedAt(Instant docusignSignedAt) {
            response.docusignSignedAt = docusignSignedAt;
            return this;
        }

        public Builder signedDocumentUrl(String signedDocumentUrl) {
            response.signedDocumentUrl = signedDocumentUrl;
            return this;
        }

        public Builder tenantName(String tenantName) {
            response.tenantName = tenantName;
            return this;
        }

        public Builder tenantEmail(String tenantEmail) {
            response.tenantEmail = tenantEmail;
            return this;
        }

        public Builder propertyName(String propertyName) {
            response.propertyName = propertyName;
            return this;
        }

        public LeaseSigningStatusResponse build() {
            return response;
        }
    }

    // Getters and Setters
    public UUID getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(UUID connectionId) {
        this.connectionId = connectionId;
    }

    public String getLeaseDocumentUrl() {
        return leaseDocumentUrl;
    }

    public void setLeaseDocumentUrl(String leaseDocumentUrl) {
        this.leaseDocumentUrl = leaseDocumentUrl;
    }

    public String getDocusignEnvelopeId() {
        return docusignEnvelopeId;
    }

    public void setDocusignEnvelopeId(String docusignEnvelopeId) {
        this.docusignEnvelopeId = docusignEnvelopeId;
    }

    public LeaseSigningStatus getLeaseSigningStatus() {
        return leaseSigningStatus;
    }

    public void setLeaseSigningStatus(LeaseSigningStatus leaseSigningStatus) {
        this.leaseSigningStatus = leaseSigningStatus;
    }

    public Instant getDocusignSentAt() {
        return docusignSentAt;
    }

    public void setDocusignSentAt(Instant docusignSentAt) {
        this.docusignSentAt = docusignSentAt;
    }

    public Instant getDocusignSignedAt() {
        return docusignSignedAt;
    }

    public void setDocusignSignedAt(Instant docusignSignedAt) {
        this.docusignSignedAt = docusignSignedAt;
    }

    public String getSignedDocumentUrl() {
        return signedDocumentUrl;
    }

    public void setSignedDocumentUrl(String signedDocumentUrl) {
        this.signedDocumentUrl = signedDocumentUrl;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantEmail() {
        return tenantEmail;
    }

    public void setTenantEmail(String tenantEmail) {
        this.tenantEmail = tenantEmail;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
