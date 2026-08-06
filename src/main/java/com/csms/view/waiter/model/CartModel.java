package com.csms.view.waiter.model;

import com.csms.entity.Product;
import com.csms.service.VatService;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CartModel {

    private final List<CartItem> items;
    private final List<ChangeListener> listeners;

    private final VatService vatService;

    public CartModel() {
        items = new ArrayList<>();
        listeners = new ArrayList<>();

        vatService = new VatService();
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public CartItem getItemByProductId(
            int productId) {
        return items.stream()
                .filter(item -> item.getProduct().getId() == productId)
                .findFirst()
                .orElse(null);
    }

    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException(
                    "Sản phẩm không hợp lệ.");
        }

        CartItem existingItem = getItemByProductId(
                product.getId());

        if (existingItem == null) {
            items.add(
                    new CartItem(product));
        } else {
            existingItem.increaseQuantity();
        }

        recalculateVat();
        fireChanged();
    }

    public void increaseQuantity(int productId) {
        CartItem item = requireItem(productId);

        item.increaseQuantity();

        recalculateVat();
        fireChanged();
    }

    public void decreaseQuantity(int productId) {
        CartItem item = requireItem(productId);

        if (item.getQuantity() <= 1) {
            removeProduct(productId);
            return;
        }

        item.decreaseQuantity();

        recalculateVat();
        fireChanged();
    }

    public void updateQuantity(
            int productId,
            int quantity) {
        if (quantity <= 0) {
            removeProduct(productId);
            return;
        }

        CartItem item = requireItem(productId);

        item.setQuantity(quantity);

        recalculateVat();
        fireChanged();
    }

    public void updateNote(
            int productId,
            String note) {
        CartItem item = requireItem(productId);

        item.setNote(note);
        fireChanged();
    }

    public void removeProduct(int productId) {
        boolean removed = items.removeIf(item -> item.getProduct().getId() == productId);

        if (removed) {
            recalculateVat();
            fireChanged();
        }
    }

    public void clear() {
        if (items.isEmpty()) {
            return;
        }

        items.clear();
        fireChanged();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getTotalQuantity() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);
    }

    public BigDecimal getVatAmount() {
        return items.stream()
                .map(CartItem::getVatAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);
    }

    public BigDecimal getTotalAmount() {
        return getSubtotal()
                .add(getVatAmount())
                .setScale(
                        2,
                        RoundingMode.HALF_UP);
    }

    public void refreshVat() {
        recalculateVat();
        fireChanged();
    }

    private void recalculateVat() {
        for (CartItem item : items) {
            BigDecimal rate = vatService.resolveRateForProduct(
                    item.getProduct().getId());

            BigDecimal vatAmount = item.getSubtotal()
                    .multiply(rate)
                    .divide(
                            BigDecimal.valueOf(100),
                            2,
                            RoundingMode.HALF_UP);

            item.setVatRate(rate);
            item.setVatAmount(vatAmount);
        }
    }

    private CartItem requireItem(int productId) {
        CartItem item = getItemByProductId(productId);

        if (item == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy sản phẩm trong giỏ.");
        }

        return item;
    }

    public void addChangeListener(
            ChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeChangeListener(
            ChangeListener listener) {
        listeners.remove(listener);
    }

    private void fireChanged() {
        ChangeEvent event = new ChangeEvent(this);

        for (ChangeListener listener : new ArrayList<>(listeners)) {

            listener.stateChanged(event);
        }
    }
}