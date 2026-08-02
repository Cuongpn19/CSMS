package com.csms.dto;

import java.math.BigDecimal;

public record OrderVatSummary(
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal vatAmount,
        BigDecimal totalAmount) {
}