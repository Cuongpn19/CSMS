package com.csms.dto;

import java.math.BigDecimal;

public record RevenueReportRow(
        String periodLabel,
        int orderCount,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal vatAmount,
        BigDecimal revenue) {
}