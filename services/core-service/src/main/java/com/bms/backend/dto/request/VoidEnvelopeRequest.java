package com.bms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class VoidEnvelopeRequest {

    @NotBlank(message = "Void reason is required")
    private String voidReason;

    public VoidEnvelopeRequest() {}

    public VoidEnvelopeRequest(String voidReason) {
        this.voidReason = voidReason;
    }

    public String getVoidReason() {
        return voidReason;
    }

    public void setVoidReason(String voidReason) {
        this.voidReason = voidReason;
    }
}
