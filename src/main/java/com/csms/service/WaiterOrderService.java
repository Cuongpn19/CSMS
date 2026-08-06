package com.csms.service;

import com.csms.config.DatabaseConnection;
import com.csms.entity.OrderItemStatus;
import com.csms.entity.OrderStatus;
import com.csms.entity.RoleName;
import com.csms.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WaiterOrderService {

    private final RoleAuthorizationService authorizationService;

    public WaiterOrderService() {
        authorizationService = new RoleAuthorizationService();
    }

    public void markOrderServed(
            int orderId) {
        User waiter = authorizationService
                .requireRole(
                        RoleName.WAITER);

        validateOrderId(orderId);

        Connection connection = null;

        try {
            connection = DatabaseConnection
                    .getConnection();

            connection.setAutoCommit(false);

            lockOrder(
                    connection,
                    orderId);

            validatePreparedOrder(
                    connection,
                    orderId);

            int updatedItems = updatePreparedItemsToServed(
                    connection,
                    orderId);

            if (updatedItems == 0) {
                throw new IllegalStateException(
                        "Đơn không còn món đã pha xong để phục vụ.");
            }

            updateOrderStatus(
                    connection,
                    orderId,
                    OrderStatus.SERVED);

            connection.commit();

        } catch (
                IllegalArgumentException
                | IllegalStateException
                | SecurityException exception) {
            rollbackQuietly(connection);
            throw exception;

        } catch (SQLException exception) {
            rollbackQuietly(connection);

            throw new IllegalStateException(
                    "Không thể cập nhật trạng thái phục vụ: "
                            + exception.getMessage(),
                    exception);

        } finally {
            closeQuietly(connection);
        }
    }

    private void validateOrderId(
            int orderId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException(
                    "Mã đơn hàng không hợp lệ.");
        }
    }

    private void lockOrder(
            Connection connection,
            int orderId) throws SQLException {

        String sql = """
                SELECT id, status
                FROM orders
                WHERE id = ?
                FOR UPDATE
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql)) {

            statement.setInt(
                    1,
                    orderId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    throw new IllegalArgumentException(
                            "Không tìm thấy đơn hàng.");
                }

                String status = resultSet.getString(
                        "status");

                if ("CANCELLED".equals(status)
                        || "PAID".equals(status)) {

                    throw new IllegalStateException(
                            "Đơn hàng đã kết thúc hoặc đã bị hủy.");
                }
            }
        }
    }

    private void validatePreparedOrder(
            Connection connection,
            int orderId) throws SQLException {

        String sql = """
                SELECT COUNT(*) AS total
                FROM order_details
                WHERE order_id = ?
                  AND status = 'PREPARED'
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql)) {

            statement.setInt(
                    1,
                    orderId);

            try (ResultSet resultSet = statement.executeQuery()) {

                boolean hasPreparedItems = resultSet.next()
                        && resultSet.getInt(
                                "total") > 0;

                if (!hasPreparedItems) {
                    throw new IllegalStateException(
                            "Đơn chưa có món nào pha chế xong.");
                }
            }
        }
    }

    private int updatePreparedItemsToServed(
            Connection connection,
            int orderId) throws SQLException {

        String sql = """
                UPDATE order_details
                SET
                    status = 'SERVED',
                    served_at = CURRENT_TIMESTAMP
                WHERE order_id = ?
                  AND status = 'PREPARED'
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql)) {

            statement.setInt(
                    1,
                    orderId);

            return statement.executeUpdate();
        }
    }

    private void updateOrderStatus(
            Connection connection,
            int orderId,
            OrderStatus status) throws SQLException {

        String sql = """
                UPDATE orders
                SET status = ?
                WHERE id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql)) {

            statement.setString(
                    1,
                    status.name());

            statement.setInt(
                    2,
                    orderId);

            if (statement.executeUpdate() == 0) {
                throw new SQLException(
                        "Không thể cập nhật trạng thái đơn.");
            }
        }
    }

    private void rollbackQuietly(
            Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void closeQuietly(
            Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.setAutoCommit(true);
        } catch (SQLException ignored) {
        }

        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}