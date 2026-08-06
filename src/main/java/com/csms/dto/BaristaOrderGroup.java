package com.csms.dto;

import com.csms.entity.OrderItemStatus;

import java.time.LocalDateTime;
import java.util.List;

public record BaristaOrderGroup(
        int orderId,
        String orderCode,
        int tableId,
        int tableNumber,
        String waiterName,
        LocalDateTime createdAt,
        OrderItemStatus queueStatus,
        List<BaristaOrderItem> items) {

    public int getTotalQuantity() {
        if (items == null) {
            return 0;
        }

        return items.stream()
                .mapToInt(BaristaOrderItem::quantity)
                .sum();
    }
}