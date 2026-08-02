package com.csms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VatSetting {

    private int id;

    private VatScopeType scopeType;

    private Integer categoryId;
    private String categoryName;

    private Integer productId;
    private String productName;

    private BigDecimal vatRate;
    private boolean enabled;

    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;

    private Integer createdBy;
    private String createdByName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public VatSetting() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public VatScopeType getScopeType() {
        return scopeType;
    }

    public void setScopeType(
            VatScopeType scopeType) {
        this.scopeType = scopeType;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(
            Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(
            String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(
            Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(
            String productName) {
        this.productName = productName;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public void setVatRate(
            BigDecimal vatRate) {
        this.vatRate = vatRate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(
            boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(
            LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDateTime getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(
            LocalDateTime effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(
            Integer createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(
            String createdByName) {
        this.createdByName = createdByName;
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

    public String getTargetName() {
        return switch (scopeType) {
            case GLOBAL -> "Toàn hệ thống";

            case CATEGORY ->
                categoryName == null
                        ? "Danh mục"
                        : categoryName;

            case PRODUCT ->
                productName == null
                        ? "Món"
                        : productName;
        };
    }
}