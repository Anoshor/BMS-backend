package com.bms.backend.dto.response;

public class DocuSignEmbeddedSigningResponse {

    private String signingUrl;
    private String envelopeId;
    private Long expiresAt;

    public DocuSignEmbeddedSigningResponse() {}

    public DocuSignEmbeddedSigningResponse(String signingUrl, String envelopeId, Long expiresAt) {
        this.signingUrl = signingUrl;
        this.envelopeId = envelopeId;
        this.expiresAt = expiresAt;
    }

    public static DocuSignEmbeddedSigningResponse of(String signingUrl, String envelopeId) {
        // DocuSign embedded signing URLs expire in 5 minutes (300 seconds)
        long expiresAt = System.currentTimeMillis() + (300 * 1000);
        return new DocuSignEmbeddedSigningResponse(signingUrl, envelopeId, expiresAt);
    }

    public String getSigningUrl() {
        return signingUrl;
    }

    public void setSigningUrl(String signingUrl) {
        this.signingUrl = signingUrl;
    }

    public String getEnvelopeId() {
        return envelopeId;
    }

    public void setEnvelopeId(String envelopeId) {
        this.envelopeId = envelopeId;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }
}
