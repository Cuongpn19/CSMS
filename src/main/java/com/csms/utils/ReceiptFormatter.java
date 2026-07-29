package com.csms.utils;

import com.csms.dto.ReceiptData;
import com.csms.entity.Order;
import com.csms.entity.OrderDetail;
import com.csms.entity.Payment;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class ReceiptFormatter {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("vi-VN"));

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(
            "dd/MM/yyyy HH:mm:ss");

    private ReceiptFormatter() {
    }

    public static String format(ReceiptData receiptData) {
        Order order = receiptData.order();
        Payment payment = receiptData.payment();

        StringBuilder content = new StringBuilder();

        appendCenter(content, "COFFEE SHOP MANAGEMENT");
        appendCenter(content, "HÓA ĐƠN THANH TOÁN");
        appendLine(content, 46);

        content.append("Mã đơn: ")
                .append(order.getOrderCode())
                .append("\n");

        content.append("Ngày tạo: ")
                .append(
                        order.getCreatedAt() == null
                                ? "-"
                                : order.getCreatedAt()
                                        .format(DATE_FORMATTER))
                .append("\n");

        content.append("Nhân viên: ")
                .append(
                        order.getCashierName() == null
                                ? "-"
                                : order.getCashierName())
                .append("\n");

        content.append("Loại đơn: ")
                .append(
                        switch (order.getOrderType()) {
                            case DINE_IN -> "Tại bàn";
                            case TAKE_AWAY -> "Mang đi";
                        })
                .append("\n");

        content.append("Bàn: ")
                .append(
                        order.getTableName() == null
                                ? "-"
                                : order.getTableName())
                .append("\n");

        appendLine(content, 46);

        content.append(
                String.format(
                        "%-24s %5s %14s%n",
                        "Sản phẩm",
                        "SL",
                        "Thành tiền"));

        appendLine(content, 46);

        for (OrderDetail detail : order.getDetails()) {
            String productName = shorten(detail.getProductName(), 24);

            content.append(
                    String.format(
                            "%-24s %5d %14s%n",
                            productName,
                            detail.getQuantity(),
                            formatMoney(
                                    detail.getSubtotal())));

            content.append(
                    String.format(
                            "  %d x %s%n",
                            detail.getQuantity(),
                            formatMoney(
                                    detail.getUnitPrice())));

            if (detail.getNote() != null
                    && !detail.getNote().isBlank()) {
                content.append("  Ghi chú: ")
                        .append(detail.getNote())
                        .append("\n");
            }
        }

        appendLine(content, 46);

        appendAmount(
                content,
                "Tạm tính",
                formatMoney(order.getSubtotal()));

        appendAmount(
                content,
                "Giảm giá",
                formatMoney(order.getDiscount()));

        appendAmount(
                content,
                "Tổng tiền",
                formatMoney(order.getTotalAmount()));

        appendLine(content, 46);

        content.append("Phương thức: ")
                .append(
                        payment.getPaymentMethod()
                                .getDisplayName())
                .append("\n");

        appendAmount(
                content,
                "Tiền khách đưa",
                formatMoney(
                        payment.getAmountReceived()));

        appendAmount(
                content,
                "Tiền thối",
                formatMoney(
                        payment.getChangeAmount()));

        content.append("Thanh toán lúc: ")
                .append(
                        payment.getPaidAt() == null
                                ? "-"
                                : payment.getPaidAt()
                                        .format(DATE_FORMATTER))
                .append("\n");

        if (order.getNote() != null
                && !order.getNote().isBlank()) {
            appendLine(content, 46);

            content.append("Ghi chú đơn: ")
                    .append(order.getNote())
                    .append("\n");
        }

        appendLine(content, 46);
        appendCenter(content, "Cảm ơn quý khách!");
        appendCenter(content, "Hẹn gặp lại!");

        return content.toString();
    }

    private static String formatMoney(
            java.math.BigDecimal amount) {
        return CURRENCY_FORMAT.format(
                amount == null
                        ? java.math.BigDecimal.ZERO
                        : amount);
    }

    private static void appendAmount(
            StringBuilder content,
            String label,
            String amount) {
        content.append(
                String.format(
                        "%-28s %17s%n",
                        label + ":",
                        amount));
    }

    private static void appendLine(
            StringBuilder content,
            int length) {
        content.append("-".repeat(length))
                .append("\n");
    }

    private static void appendCenter(
            StringBuilder content,
            String value) {
        int width = 46;

        if (value.length() >= width) {
            content.append(value).append("\n");
            return;
        }

        int padding = (width - value.length()) / 2;

        content.append(" ".repeat(padding))
                .append(value)
                .append("\n");
    }

    private static String shorten(
            String value,
            int maximumLength) {
        if (value == null) {
            return "";
        }

        if (value.length() <= maximumLength) {
            return value;
        }

        return value.substring(
                0,
                maximumLength - 3) + "...";
    }
}