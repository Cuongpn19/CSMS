package com.csms.service;

import com.csms.dao.OrderDAO;
import com.csms.dto.ReceiptData;
import com.csms.entity.Order;

public class ReceiptService {

    private final OrderDAO orderDAO;

    public ReceiptService() {
        this.orderDAO = new OrderDAO();
    }

    public ReceiptData getReceiptData(int orderId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException(
                    "Mã đơn hàng không hợp lệ.");
        }

        Order order = orderDAO.findById(orderId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy đơn hàng."));

        /*
         * Chỗ này phải khớp constructor ReceiptData của bạn.
         */
        return convertToReceiptData(order);
    }

    private ReceiptData convertToReceiptData(Order order) {
        throw new UnsupportedOperationException(
                "Cần cung cấp class ReceiptData để hoàn thiện phần chuyển đổi.");
    }
}