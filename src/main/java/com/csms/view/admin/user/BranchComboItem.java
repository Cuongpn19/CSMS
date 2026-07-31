package com.csms.view.admin.user;

public record BranchComboItem(
        Integer id,
        String name) {
    @Override
    public String toString() {
        return name;
    }
}