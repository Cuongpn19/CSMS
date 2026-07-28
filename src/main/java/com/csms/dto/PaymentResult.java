package com.csms.dto;

import com.csms.entity.Payment;

public record PaymentResult(
        boolean success,
        String message,
        Payment payment) {
}