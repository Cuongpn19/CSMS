package com.csms.dto;

import com.csms.entity.IngredientStatus;
import com.csms.entity.IngredientUnit;

import java.math.BigDecimal;

public record IngredientFormData(
        String name,
        IngredientUnit unit,
        BigDecimal quantity,
        BigDecimal minimumStock,
        BigDecimal importPrice,
        IngredientStatus status) {
}