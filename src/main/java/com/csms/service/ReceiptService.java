package com.csms.service;

import com.csms.dao.OrderDAO;
import com.csms.dao.PaymentDAO;
import com.csms.dto.ReceiptData;
import com.csms.entity.Order;
import com.csms.entity.Payment;

public class ReceiptService {

    private final OrderDAO orderDAO;
    private final PaymentDAO paymentDAO;

    public ReceiptService() {
        this.orderDAO = new OrderDAO();
        this.paymentDAO = new PaymentDAO();
    }

    public ReceiptData getReceiptData(int orderId) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy đơn hàng."));

        Payment payment = paymentDAO
                .findByOrderId(orderId)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Đơn hàng chưa có thông tin thanh toán."));

        return new ReceiptData(order, payment);
    }
}