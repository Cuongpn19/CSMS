package com.csms.entity;

public enum PaymentMethod {

        CASH("Tiền mặt"),
        CARD("Thẻ"),
        BANK_TRANSFER("Chuyển khoản"),
        E_WALLET("Ví điện tử");

        private final String displayName;

        PaymentMethod(String displayName) {
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