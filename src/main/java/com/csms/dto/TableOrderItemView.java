package com.csms.dto;

import com.csms.entity.OrderItemStatus;

import java.math.BigDecimal;

public record TableOrderItemView(
        int orderDetailId,
        int productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        BigDecimal vatRate,
        BigDecimal vatAmount,
        OrderItemStatus status,
        String note) {
}