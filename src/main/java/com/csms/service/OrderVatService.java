package com.csms.service;

import com.csms.dto.OrderVatSummary;
import com.csms.entity.Order;
import com.csms.entity.OrderDetail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class OrderVatService {

    private final VatService vatService;

    public OrderVatService() {
        this.vatService = new VatService();
    }

    public OrderVatSummary applyVat(Order order) {
        if (order == null) {
            throw new IllegalArgumentException(
                    "Đơn hàng không hợp lệ.");
        }

        List<OrderDetail> details = order.getDetails();

        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException(
                    "Đơn hàng chưa có sản phẩm.");
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        BigDecimal totalVat = BigDecimal.ZERO;

        for (OrderDetail detail : details) {
            validateDetail(detail);

            BigDecimal lineSubtotal = detail.getUnitPrice()
                    .multiply(
                            BigDecimal.valueOf(
                                    detail.getQuantity()))
                    .setScale(
                            2,
                            RoundingMode.HALF_UP);

            BigDecimal vatRate = vatService.resolveRateForProduct(
                    detail.getProductId());

            BigDecimal vatAmount = lineSubtotal
                    .multiply(vatRate)
                    .divide(
                            BigDecimal.valueOf(100),
                            2,
                            RoundingMode.HALF_UP);

            detail.setSubtotal(lineSubtotal);
            detail.setVatRate(vatRate);
            detail.setVatAmount(vatAmount);

            subtotal = subtotal.add(lineSubtotal);
            totalVat = totalVat.add(vatAmount);
        }

        BigDecimal discount = order.getDiscount() == null
                ? BigDecimal.ZERO
                : order.getDiscount();

        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Giảm giá không được nhỏ hơn 0.");
        }

        if (discount.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException(
                    "Giảm giá không được lớn hơn tạm tính.");
        }

        BigDecimal totalAmount = subtotal
                .subtract(discount)
                .add(totalVat)
                .setScale(
                        2,
                        RoundingMode.HALF_UP);

        order.setSubtotal(subtotal);
        order.setVatAmount(totalVat);
        order.setTotalAmount(totalAmount);

        return new OrderVatSummary(
                subtotal,
                discount,
                totalVat,
                totalAmount);
    }

    private void validateDetail(
            OrderDetail detail) {
        if (detail == null) {
            throw new IllegalArgumentException(
                    "Chi tiết đơn hàng không hợp lệ.");
        }

        if (detail.getProductId() <= 0) {
            throw new IllegalArgumentException(
                    "Sản phẩm trong đơn không hợp lệ.");
        }

        if (detail.getQuantity() <= 0) {
            throw new IllegalArgumentException(
                    "Số lượng sản phẩm phải lớn hơn 0.");
        }

        if (detail.getUnitPrice() == null
                || detail.getUnitPrice()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Đơn giá sản phẩm không hợp lệ.");
        }
    }
}