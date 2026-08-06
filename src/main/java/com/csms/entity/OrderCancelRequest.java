package com.csms.entity;

import java.time.LocalDateTime;

public class OrderCancelRequest {

    private long id;

    private int orderId;
    private String orderCode;

    private Integer tableId;
    private Integer tableNumber;

    private int requestedBy;
    private String requesterName;

    private String reason;

    private CancelRequestStatus status;

    private Integer reviewedBy;
    private String reviewerName;

    private LocalDateTime reviewedAt;

    private String managerNote;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderCancelRequest() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public Integer getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(
            Integer tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(
            int requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(
            String requesterName) {
        this.requesterName = requesterName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public CancelRequestStatus getStatus() {
        return status;
    }

    public void setStatus(
            CancelRequestStatus status) {
        this.status = status;
    }

    public Integer getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(
            Integer reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(
            String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(
            LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getManagerNote() {
        return managerNote;
    }

    public void setManagerNote(
            String managerNote) {
        this.managerNote = managerNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isPending() {
        return status == CancelRequestStatus.PENDING;
    }
}