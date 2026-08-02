package com.csms.entity;

public enum IngredientStatus {

    ACTIVE("Đang sử dụng"),
    INACTIVE("Ngừng sử dụng");

    private final String displayName;

    IngredientStatus(String displayName) {
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