package com.csms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Ingredient {

    private int id;
    private String name;
    private String unit;

    private BigDecimal quantity;
    private BigDecimal minimumStock;
    private BigDecimal importPrice;

    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Ingredient() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(
            String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(
            String unit) {
        this.unit = unit;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(
            BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(
            BigDecimal minimumStock) {
        this.minimumStock = minimumStock;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(
            BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(
            String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return name + " (" + unit + ")";
    }
}