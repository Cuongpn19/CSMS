package com.csms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private int id;
    private Integer tableId;
    private String tableName;
    private int cashierId;
    private String cashierName;
    private String orderCode;
    private OrderType orderType;
    private OrderStatus status;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal subtotalBeforeVat = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal vatAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String note;
    private LocalDateTime createdAt;
    private List<OrderDetail> details = new ArrayList<>();

    public void calculateTotals() {
        subtotal = details.stream()
                .map(OrderDetail::getSubtotal)
                .filter(value -> value != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        if (discount == null
                || discount.compareTo(BigDecimal.ZERO) < 0) {
            discount = BigDecimal.ZERO;
        }

        totalAmount = subtotal.subtract(discount);

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }
    }

    public void addDetail(OrderDetail detail) {
        details.add(detail);
        calculateTotals();
    }

    public void removeDetail(OrderDetail detail) {
        details.remove(detail);
        calculateTotals();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getCashierId() {
        return cashierId;
    }

    public void setCashierId(int cashierId) {
        this.cashierId = cashierId;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getSubtotalBeforeVat() {
        return subtotalBeforeVat;
    }

    public void setSubtotalBeforeVat(
            BigDecimal subtotalBeforeVat) {
        this.subtotalBeforeVat = subtotalBeforeVat;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderDetail> getDetails() {
        return details;
    }

    public void setDetails(List<OrderDetail> details) {
        this.details = details == null
                ? new ArrayList<>()
                : details;
    }
}