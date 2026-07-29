package com.csms.entity;

public enum UserStatus {

    ACTIVE("Đang hoạt động"),
    INACTIVE("Ngừng hoạt động");

    private final String displayName;

    UserStatus(String displayName) {
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