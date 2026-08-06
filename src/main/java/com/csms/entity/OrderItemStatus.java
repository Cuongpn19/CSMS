package com.csms.entity;

public enum OrderItemStatus {

    IN_PROGRESS("Chờ pha"),

    PREPARING("Đang pha"),

    PREPARED("Đã pha xong"),

    SERVED("Đã phục vụ"),

    CANCELLED("Đã hủy");

    private final String displayName;

    OrderItemStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canEditQuantity() {
        return this == IN_PROGRESS;
    }

    public boolean canRemove() {
        return this == IN_PROGRESS;
    }

    @Override
    public String toString() {
        return displayName;
    }
}