package com.bms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class EmbeddedSigningRequest {

    @NotBlank(message = "Return URL is required")
    private String returnUrl;

    public EmbeddedSigningRequest() {}

    public EmbeddedSigningRequest(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
}
