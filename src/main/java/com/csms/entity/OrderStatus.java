package com.csms.entity;

public enum OrderStatus {

    DRAFT("Đơn nháp"),

    IN_PROGRESS("Chờ pha chế"),

    PREPARING("Đang pha chế"),

    PREPARED("Đã pha chế xong"),

    SERVED("Đã phục vụ"),

    WAITING_PAYMENT("Chờ thanh toán"),

    PAID("Đã thanh toán"),

    CANCEL_PENDING("Chờ duyệt hủy"),

    CANCELLED("Đã hủy");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActiveOrder() {
        return switch (this) {
            case IN_PROGRESS,
                    PREPARING,
                    PREPARED,
                    SERVED,
                    WAITING_PAYMENT,
                    CANCEL_PENDING ->
                true;

            default -> false;
        };
    }

    public boolean canWaiterEditQuantity() {
        return this == IN_PROGRESS;
    }

    public boolean canWaiterAddItem() {
        return switch (this) {
            case IN_PROGRESS,
                    PREPARING,
                    PREPARED,
                    SERVED ->
                true;

            default -> false;
        };
    }

    public boolean canRequestCancellation() {
        return this == IN_PROGRESS;
    }

    @Override
    public String toString() {
        return displayName;
    }
}