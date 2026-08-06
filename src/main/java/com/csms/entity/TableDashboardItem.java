package com.csms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TableDashboardItem {

    private int tableId;
    private int tableNumber;
    private TableStatus tableStatus;

    private Integer orderId;
    private String orderCode;
    private OrderStatus orderStatus;

    private int itemCount;
    private int totalQuantity;

    private BigDecimal subtotal;
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;

    private Integer waiterId;
    private String waiterName;

    private LocalDateTime orderCreatedAt;

    public TableDashboardItem() {
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public TableStatus getTableStatus() {
        return tableStatus;
    }

    public void setTableStatus(
            TableStatus tableStatus) {
        this.tableStatus = tableStatus;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(
            OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(
            int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(
            BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(
            BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(
            BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getWaiterId() {
        return waiterId;
    }

    public void setWaiterId(Integer waiterId) {
        this.waiterId = waiterId;
    }

    public String getWaiterName() {
        return waiterName;
    }

    public void setWaiterName(String waiterName) {
        this.waiterName = waiterName;
    }

    public LocalDateTime getOrderCreatedAt() {
        return orderCreatedAt;
    }

    public void setOrderCreatedAt(
            LocalDateTime orderCreatedAt) {
        this.orderCreatedAt = orderCreatedAt;
    }

    public boolean hasActiveOrder() {
        return orderId != null;
    }

    public boolean canCreateOrder() {
        return tableStatus == TableStatus.AVAILABLE
                && orderId == null;
    }

    public boolean canOpenOrderDetail() {
        return orderId != null;
    }

    public BigDecimal getSafeTotalAmount() {
        return totalAmount == null
                ? BigDecimal.ZERO
                : totalAmount;
    }

    public String getDisplayStatus() {
        if (tableStatus == TableStatus.INACTIVE) {
            return tableStatus.getDisplayName();
        }

        if (orderStatus != null) {
            return orderStatus.getDisplayName();
        }

        return tableStatus.getDisplayName();
    }
}