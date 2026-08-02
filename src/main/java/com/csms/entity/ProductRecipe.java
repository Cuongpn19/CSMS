package com.csms.entity;

import java.math.BigDecimal;

public class ProductRecipe {

    private int id;

    private int productId;
    private String productName;

    private int ingredientId;
    private String ingredientName;

    private BigDecimal quantityRequired;
    private String unit;

    public ProductRecipe() {
    }

    public int getId() {
        return id;
    }

    public void setId(
            int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(
            int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(
            String productName) {
        this.productName = productName;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(
            int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(
            String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public BigDecimal getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(
            BigDecimal quantityRequired) {
        this.quantityRequired = quantityRequired;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(
            String unit) {
        this.unit = unit;
    }
}