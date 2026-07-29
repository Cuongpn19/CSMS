package com.csms.dto;

import java.math.BigDecimal;

public record DashboardStats(
        BigDecimal todayRevenue,
        int todayOrderCount,
        int occupiedTableCount,
        int lowStockProductCount) {
}