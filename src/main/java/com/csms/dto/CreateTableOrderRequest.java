package com.csms.dto;

import com.csms.view.waiter.model.CartItem;

import java.math.BigDecimal;
import java.util.List;

public record CreateTableOrderRequest(
        int tableId,
        int tableNumber,
        int waiterId,
        String orderNote,
        List<CartItem> items,
        BigDecimal subtotal,
        BigDecimal vatAmount,
        BigDecimal totalAmount) {
}