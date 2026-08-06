package com.csms.dao;

import com.csms.entity.OrderDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class OrderDetailDAO {

    private static final String INSERT_SQL = """
            INSERT INTO order_details(
                order_id,
                product_id,
                quantity,
                unit_price,
                subtotal,
                vat_rate,
                vat_amount,
                status,
                note
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    public void insertBatch(
            Connection connection,
            List<OrderDetail> details) throws SQLException {

        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException(
                    "Đơn hàng chưa có chi tiết món.");
        }

        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_SQL)) {

            for (OrderDetail detail : details) {
                statement.setInt(
                        1,
                        detail.getOrderId());

                statement.setInt(
                        2,
                        detail.getProductId());

                statement.setString(
                        3,
                        detail.getProductName());

                statement.setInt(
                        4,
                        detail.getQuantity());

                statement.setBigDecimal(
                        5,
                        detail.getUnitPrice());

                statement.setBigDecimal(
                        6,
                        detail.getSubtotal());

                statement.setBigDecimal(
                        7,
                        detail.getVatRate());

                statement.setBigDecimal(
                        8,
                        detail.getVatAmount());

                if (detail.getNote() == null
                        || detail.getNote().isBlank()) {

                    statement.setNull(
                            9,
                            Types.VARCHAR);

                } else {
                    statement.setString(
                            9,
                            detail.getNote().trim());
                }

                statement.addBatch();
            }

            int[] results = statement.executeBatch();

            for (int result : results) {
                if (result == PreparedStatement.EXECUTE_FAILED) {
                    throw new SQLException(
                            "Không thể lưu chi tiết đơn hàng.");
                }
            }
        }
    }
}