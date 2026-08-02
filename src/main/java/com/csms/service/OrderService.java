package com.csms.service;

import com.csms.dao.OrderDAO;
import com.csms.dto.OrderVatSummary;
import com.csms.entity.Order;

public class OrderService {

    private final OrderDAO orderDAO;
    private final OrderVatService orderVatService;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.orderVatService = new OrderVatService();
    }

    public int createOrder(Order order) {
        validateOrder(order);

        OrderVatSummary summary = orderVatService.applyVat(order);

        order.setSubtotal(
                summary.subtotal());

        order.setDiscount(
                summary.discount());

        order.setVatAmount(
                summary.vatAmount());

        order.setTotalAmount(
                summary.totalAmount());

        return orderDAO.create(order);
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException(
                    "Đơn hàng không hợp lệ.");
        }

        if (order.getDetails() == null
                || order.getDetails().isEmpty()) {

            throw new IllegalArgumentException(
                    "Đơn hàng chưa có sản phẩm.");
        }
    }
}