package com.csms.dto;

import com.csms.entity.OrderItemStatus;

public record BaristaOrderItem(
        int orderDetailId,
        int productId,
        String productName,
        int quantity,
        String note,
        OrderItemStatus status) {
}