package com.csms.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class Branch {

    private int id;

    private String name;
    private String address;
    private String phone;

    private LocalTime openingTime;
    private LocalTime closingTime;

    private BranchStatus status;

    private int employeeCount;
    private int activeEmployeeCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Branch() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(
            String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(
            String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(
            String phone) {
        this.phone = phone;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(
            LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(
            LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    public BranchStatus getStatus() {
        return status;
    }

    public void setStatus(
            BranchStatus status) {
        this.status = status;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(
            int employeeCount) {
        this.employeeCount = employeeCount;
    }

    public int getActiveEmployeeCount() {
        return activeEmployeeCount;
    }

    public void setActiveEmployeeCount(
            int activeEmployeeCount) {
        this.activeEmployeeCount = activeEmployeeCount;
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

    @Override
    public String toString() {
        return name;
    }
}