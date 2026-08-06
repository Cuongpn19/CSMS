package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.dto.TableOrderDetailView;
import com.csms.dto.TableOrderItemView;
import com.csms.entity.OrderItemStatus;
import com.csms.entity.OrderStatus;
import com.csms.entity.TableStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TableDetailDAO {

    private static final String FIND_ACTIVE_ORDER_SQL = """
            SELECT
                t.id AS table_id,
                t.table_number,
                t.status AS table_status,

                o.id AS order_id,
                o.order_code,
                o.status AS order_status,
                o.user_id AS waiter_id,
                u.full_name AS waiter_name,
                o.created_at,

                o.subtotal,
                o.discount,
                o.vat_amount,
                o.total_amount,
                o.note AS order_note

            FROM coffee_tables t

            LEFT JOIN orders o
                ON o.table_id = t.id
               AND o.status IN (
                    'IN_PROGRESS',
                    'PREPARING',
                    'PREPARED',
                    'SERVED',
                    'WAITING_PAYMENT',
                    'CANCEL_PENDING'
               )

            LEFT JOIN users u
                ON u.id = o.user_id

            WHERE t.id = ?
            """;

    private static final String FIND_ORDER_ITEMS_SQL = """
            SELECT
                od.id AS order_detail_id,
                od.product_id,
                p.name AS product_name,
                od.quantity,
                od.unit_price,
                od.subtotal,
                od.vat_rate,
                od.vat_amount,
                od.status,
                od.note

            FROM order_details od

            JOIN products p
                ON p.id = od.product_id

            WHERE od.order_id = ?
              AND od.status <> 'CANCELLED'

            ORDER BY od.id
            """;

    public Optional<TableOrderDetailView> findByTableId(
            int tableId) {
        try (Connection connection = DatabaseConnection.getConnection()) {

            return findByTableId(
                    connection,
                    tableId);

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể tải chi tiết bàn: "
                            + exception.getMessage(),
                    exception);
        }
    }

    private Optional<TableOrderDetailView> findByTableId(
            Connection connection,
            int tableId) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                FIND_ACTIVE_ORDER_SQL)) {

            statement.setInt(1, tableId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                Integer orderId = getNullableInteger(
                        resultSet,
                        "order_id");

                List<TableOrderItemView> items = orderId == null
                        ? List.of()
                        : findItems(
                                connection,
                                orderId);

                return Optional.of(
                        new TableOrderDetailView(
                                resultSet.getInt(
                                        "table_id"),

                                resultSet.getInt(
                                        "table_number"),

                                TableStatus.valueOf(
                                        resultSet.getString(
                                                "table_status")),

                                orderId,

                                resultSet.getString(
                                        "order_code"),

                                mapOrderStatus(
                                        resultSet.getString(
                                                "order_status")),

                                getNullableInteger(
                                        resultSet,
                                        "waiter_id"),

                                resultSet.getString(
                                        "waiter_name"),

                                resultSet
                                        .getTimestamp(
                                                "created_at") == null
                                                        ? null
                                                        : resultSet
                                                                .getTimestamp(
                                                                        "created_at")
                                                                .toLocalDateTime(),

                                resultSet.getBigDecimal(
                                        "subtotal"),

                                resultSet.getBigDecimal(
                                        "discount"),

                                resultSet.getBigDecimal(
                                        "vat_amount"),

                                resultSet.getBigDecimal(
                                        "total_amount"),

                                resultSet.getString(
                                        "order_note"),

                                items));
            }
        }
    }

    private List<TableOrderItemView> findItems(
            Connection connection,
            int orderId) throws SQLException {

        List<TableOrderItemView> items = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(
                FIND_ORDER_ITEMS_SQL)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    items.add(
                            new TableOrderItemView(
                                    resultSet.getInt(
                                            "order_detail_id"),

                                    resultSet.getInt(
                                            "product_id"),

                                    resultSet.getString(
                                            "product_name"),

                                    resultSet.getInt(
                                            "quantity"),

                                    resultSet.getBigDecimal(
                                            "unit_price"),

                                    resultSet.getBigDecimal(
                                            "subtotal"),

                                    resultSet.getBigDecimal(
                                            "vat_rate"),

                                    resultSet.getBigDecimal(
                                            "vat_amount"),

                                    OrderItemStatus.valueOf(
                                            resultSet.getString(
                                                    "status")),

                                    resultSet.getString(
                                            "note")));
                }
            }
        }

        return items;
    }

    private Integer getNullableInteger(
            ResultSet resultSet,
            String columnName) throws SQLException {

        int value = resultSet.getInt(columnName);

        return resultSet.wasNull()
                ? null
                : value;
    }

    private OrderStatus mapOrderStatus(
            String status) {
        return status == null
                ? null
                : OrderStatus.valueOf(status);
    }
}