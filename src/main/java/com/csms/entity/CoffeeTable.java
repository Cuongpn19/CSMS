package com.csms.entity;

import java.time.LocalDateTime;

public class CoffeeTable {

    private int id;

    private int tableNumber;

    private int capacity;
    private TableStatus status;

    // private Integer branchId;
    // private String branchName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CoffeeTable() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public TableStatus getStatus() {
        return status;
    }

    public void setStatus(TableStatus status) {
        this.status = status;
    }

    // public Integer getBranchId() {
    // return branchId;
    // }

    // public void setBranchId(Integer branchId) {
    // this.branchId = branchId;
    // }

    // public String getBranchName() {
    // return branchName;
    // }

    // public void setBranchName(String branchName) {
    // this.branchName = branchName;
    // }

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

    public boolean isAvailable() {
        return status == TableStatus.AVAILABLE;
    }

    public boolean isOccupied() {
        return status == TableStatus.OCCUPIED;
    }

    public boolean isInactive() {
        return status == TableStatus.INACTIVE;
    }

    @Override
    public String toString() {
        return "Bàn " + tableNumber;
    }
}