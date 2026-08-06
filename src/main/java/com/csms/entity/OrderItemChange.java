package com.csms.entity;

import java.time.LocalDateTime;

public class OrderItemChange {

    private long id;

    private int orderId;
    private Integer orderDetailId;

    private int changedBy;
    private String changedByName;

    private OrderItemChangeType changeType;

    private Integer oldQuantity;
    private Integer newQuantity;

    private String reason;

    private LocalDateTime createdAt;

    public OrderItemChange() {
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

    public Integer getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(
            Integer orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public int getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(int changedBy) {
        this.changedBy = changedBy;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(
            String changedByName) {
        this.changedByName = changedByName;
    }

    public OrderItemChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(
            OrderItemChangeType changeType) {
        this.changeType = changeType;
    }

    public Integer getOldQuantity() {
        return oldQuantity;
    }

    public void setOldQuantity(
            Integer oldQuantity) {
        this.oldQuantity = oldQuantity;
    }

    public Integer getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(
            Integer newQuantity) {
        this.newQuantity = newQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}