package com.csms.entity;

public enum OrderItemChangeType {

    ADD("Thêm món"),

    UPDATE_QUANTITY("Cập nhật số lượng"),

    REMOVE("Xóa món");

    private final String displayName;

    OrderItemChangeType(String displayName) {
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