package com.csms.view.operation.common;

public enum DashboardMode {

    WAITER(
            true,
            true,
            true,
            true),

    MANAGER(
            false,
            false,
            false,
            false);

    private final boolean canCreateOrder;
    private final boolean canUpdateOrder;
    private final boolean canServeOrder;
    private final boolean canRequestCancel;

    DashboardMode(
            boolean canCreateOrder,
            boolean canUpdateOrder,
            boolean canServeOrder,
            boolean canRequestCancel) {
        this.canCreateOrder = canCreateOrder;
        this.canUpdateOrder = canUpdateOrder;
        this.canServeOrder = canServeOrder;
        this.canRequestCancel = canRequestCancel;
    }

    public boolean canCreateOrder() {
        return canCreateOrder;
    }

    public boolean canUpdateOrder() {
        return canUpdateOrder;
    }

    public boolean canServeOrder() {
        return canServeOrder;
    }

    public boolean canRequestCancel() {
        return canRequestCancel;
    }

    public boolean isReadOnly() {
        return this == MANAGER;
    }
}