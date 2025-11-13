package com.bms.backend.dto.request;

import java.time.Instant;

public class PaymentSearchRequest {

    private Instant startDate;
    private Instant endDate;
    private String status; // ALL, PAID, PENDING, OVERDUE
    private String searchQuery;

    // Constructors
    public PaymentSearchRequest() {}

    public PaymentSearchRequest(Instant startDate, Instant endDate, String status, String searchQuery) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.searchQuery = searchQuery;
    }

    // Getters and Setters
    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}
