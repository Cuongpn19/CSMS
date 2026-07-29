package com.csms.dto;

import com.csms.entity.Order;
import com.csms.entity.Payment;

public record ReceiptData(
        Order order,
        Payment payment) {
}