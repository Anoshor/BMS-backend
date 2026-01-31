package com.bms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class SendLeaseForSigningRequest {

    @NotBlank(message = "Subject is required")
    private String emailSubject;

    private String emailBody;

    private Integer expirationDays;

    public SendLeaseForSigningRequest() {
        this.emailSubject = "Please sign your lease agreement";
        this.emailBody = "Please review and sign your lease agreement.";
        this.expirationDays = 14;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    public Integer getExpirationDays() {
        return expirationDays;
    }

    public void setExpirationDays(Integer expirationDays) {
        this.expirationDays = expirationDays;
    }
}
