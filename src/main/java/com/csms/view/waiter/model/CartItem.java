package com.csms.view.waiter.model;

import com.csms.entity.Product;

import java.math.BigDecimal;
import java.util.Objects;

public class CartItem {

    private final Product product;
    private int quantity;
    private String note;

    private BigDecimal vatRate;
    private BigDecimal vatAmount;

    public CartItem(Product product) {
        this(product, 1);
    }

    public CartItem(
            Product product,
            int quantity) {
        this.product = Objects.requireNonNull(
                product,
                "Sản phẩm không được null.");

        this.quantity = Math.max(1, quantity);
        this.note = "";
        this.vatRate = BigDecimal.ZERO;
        this.vatAmount = BigDecimal.ZERO;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Số lượng phải lớn hơn 0.");
        }

        this.quantity = quantity;
    }

    public void increaseQuantity() {
        quantity++;
    }

    public void decreaseQuantity() {
        if (quantity > 1) {
            quantity--;
        }
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note == null
                ? ""
                : note.trim();
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public void setVatRate(BigDecimal vatRate) {
        this.vatRate = vatRate == null
                ? BigDecimal.ZERO
                : vatRate;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount == null
                ? BigDecimal.ZERO
                : vatAmount;
    }

    public BigDecimal getUnitPrice() {
        return product.getPrice() == null
                ? BigDecimal.ZERO
                : product.getPrice();
    }

    public BigDecimal getSubtotal() {
        return getUnitPrice().multiply(
                BigDecimal.valueOf(quantity));
    }

    public BigDecimal getTotalWithVat() {
        return getSubtotal().add(
                vatAmount == null
                        ? BigDecimal.ZERO
                        : vatAmount);
    }
}