package com.csms.dto;

import com.csms.entity.OrderStatus;
import com.csms.entity.TableStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TableOrderDetailView(
        int tableId,
        int tableNumber,
        TableStatus tableStatus,

        Integer orderId,
        String orderCode,
        OrderStatus orderStatus,

        Integer waiterId,
        String waiterName,

        LocalDateTime createdAt,

        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal vatAmount,
        BigDecimal totalAmount,

        String orderNote,

        List<TableOrderItemView> items) {
}