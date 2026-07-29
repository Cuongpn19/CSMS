package com.csms.dto;

import java.math.BigDecimal;

public record LowStockProduct(
        int id,
        String name,
        String categoryName,
        BigDecimal price,
        int quantity) {
}