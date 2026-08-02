package com.csms.entity;

public enum VatScopeType {

    GLOBAL("Toàn hệ thống"),
    CATEGORY("Theo danh mục"),
    PRODUCT("Theo món");

    private final String displayName;

    VatScopeType(String displayName) {
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