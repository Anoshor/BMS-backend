package com.bms.backend.dto.response;

import com.bms.backend.enums.LeaseSigningStatus;

import java.util.UUID;

public class LeaseUploadResponse {

    private UUID connectionId;
    private String leaseDocumentUrl;
    private LeaseSigningStatus leaseSigningStatus;

    public LeaseUploadResponse() {}

    public LeaseUploadResponse(UUID connectionId, String leaseDocumentUrl, LeaseSigningStatus leaseSigningStatus) {
        this.connectionId = connectionId;
        this.leaseDocumentUrl = leaseDocumentUrl;
        this.leaseSigningStatus = leaseSigningStatus;
    }

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

    public LeaseSigningStatus getLeaseSigningStatus() {
        return leaseSigningStatus;
    }

    public void setLeaseSigningStatus(LeaseSigningStatus leaseSigningStatus) {
        this.leaseSigningStatus = leaseSigningStatus;
    }
}
