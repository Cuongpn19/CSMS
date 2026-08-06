package com.csms.entity;

public enum CancelRequestStatus {

    PENDING("Chờ xử lý"),

    APPROVED("Đã chấp nhận"),

    REJECTED("Đã từ chối");

    private final String displayName;

    CancelRequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}