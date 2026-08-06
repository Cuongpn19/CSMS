package com.csms.entity;

public enum TableStatus {

    AVAILABLE("Bàn trống"),
    OCCUPIED("Đang phục vụ"),
    INACTIVE("Ngừng sử dụng");

    private final String displayName;

    TableStatus(String displayName) {
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