package com.bms.backend.enums;

public enum LeaseSigningStatus {
    DRAFT("draft", "Lease uploaded but not sent"),
    SENT("sent", "Envelope sent to tenant"),
    DELIVERED("delivered", "Tenant opened the email"),
    SIGNED("signed", "All parties signed"),
    DECLINED("declined", "Tenant declined to sign"),
    EXPIRED("expired", "Signing deadline passed"),
    VOIDED("voided", "Manager cancelled the envelope");

    private final String code;
    private final String displayName;

    LeaseSigningStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static LeaseSigningStatus fromCode(String code) {
        for (LeaseSigningStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown lease signing status code: " + code);
    }

    public static LeaseSigningStatus fromDocuSignStatus(String docuSignStatus) {
        if (docuSignStatus == null) {
            return DRAFT;
        }
        switch (docuSignStatus.toLowerCase()) {
            case "sent":
                return SENT;
            case "delivered":
                return DELIVERED;
            case "completed":
                return SIGNED;
            case "declined":
                return DECLINED;
            case "voided":
                return VOIDED;
            case "timedout":
            case "expired":
                return EXPIRED;
            default:
                return DRAFT;
        }
    }
}
