package com.csms.entity;

public enum RoleName {

    ADMIN("Quản trị viên"),
    MANAGER("Quản lý"),
    WAITER("Phục vụ"),
    BARISTA("Pha chế"),
    CASHIER("Thu ngân");

    private final String displayName;

    RoleName(String displayName) {
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