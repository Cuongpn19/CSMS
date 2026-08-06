package com.csms.dto;

import java.math.BigDecimal;

public record IngredientRequirement(
        int ingredientId,
        String ingredientName,
        String unit,
        BigDecimal availableQuantity,
        BigDecimal requiredQuantity) {

    public boolean isEnough() {
        return availableQuantity != null
                && requiredQuantity != null
                && availableQuantity.compareTo(requiredQuantity) >= 0;
    }

    public BigDecimal shortageQuantity() {
        if (availableQuantity == null || requiredQuantity == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal shortage = requiredQuantity.subtract(availableQuantity);

        return shortage.compareTo(BigDecimal.ZERO) > 0
                ? shortage
                : BigDecimal.ZERO;
    }
}