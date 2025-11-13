package com.bms.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Paginated lease payment schedule response")
public class LeasePaymentScheduleResponse {

    @Schema(description = "Lease/Connection ID", example = "04fc37d0-e819-4488-9849-4f237f9b45c1")
    private UUID leaseId;

    @Schema(description = "Property name", example = "Sunset Apartments")
    private String propertyName;

    @Schema(description = "Tenant name", example = "John Doe")
    private String tenantName;

    @Schema(description = "List of payment schedule items for the requested period")
    private List<LeasePaymentScheduleDto> schedule;

    @Schema(description = "Total number of months in the lease period", example = "24")
    private int totalMonths;

    @Schema(description = "Current page (0-indexed)", example = "0")
    private int currentPage;

    @Schema(description = "Number of items returned", example = "3")
    private int itemsReturned;

    // Constructors
    public LeasePaymentScheduleResponse() {}

    // Getters and Setters
    public UUID getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(UUID leaseId) {
        this.leaseId = leaseId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public List<LeasePaymentScheduleDto> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<LeasePaymentScheduleDto> schedule) {
        this.schedule = schedule;
    }

    public int getTotalMonths() {
        return totalMonths;
    }

    public void setTotalMonths(int totalMonths) {
        this.totalMonths = totalMonths;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getItemsReturned() {
        return itemsReturned;
    }

    public void setItemsReturned(int itemsReturned) {
        this.itemsReturned = itemsReturned;
    }
}
