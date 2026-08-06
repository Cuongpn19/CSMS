package com.csms.dto;

public record OrderResult(
        int orderId,
        String orderCode,
        String message) {
}