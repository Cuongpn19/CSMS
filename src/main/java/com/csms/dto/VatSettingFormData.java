package com.csms.dto;

import com.csms.entity.VatScopeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VatSettingFormData(
        VatScopeType scopeType,
        Integer categoryId,
        Integer productId,
        BigDecimal vatRate,
        boolean enabled,
        LocalDateTime effectiveFrom,
        LocalDateTime effectiveTo) {
}