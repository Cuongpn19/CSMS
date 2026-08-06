package com.csms.service;

import com.csms.config.DatabaseConnection;
import com.csms.entity.RoleName;
import com.csms.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderCancelRequestService {

    private final RoleAuthorizationService authorizationService;

    public OrderCancelRequestService() {
        authorizationService = new RoleAuthorizationService();
    }

    public void requestCancellation(
            int orderId,
            String reason) {
        User waiter = authorizationService
                .requireRole(
                        RoleName.WAITER);

        validateInput(
                orderId,
                reason);

        Connection connection = null;

        try {
            connection = DatabaseConnection
                    .getConnection();

            connection.setAutoCommit(false);

            lockAndValidateOrder(
                    connection,
                    orderId);

            validateNoPendingRequest(
                    connection,
                    orderId);

            insertRequest(
                    connection,
                    orderId,
                    waiter.getId(),
                    reason.trim());

            updateOrderToCancelPending(
                    connection,
                    orderId);

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
                    "Không thể gửi yêu cầu hủy đơn: "
                            + exception.getMessage(),
                    exception);

        } finally {
            closeQuietly(connection);
        }
    }

    private void validateInput(
            int orderId,
            String reason) {
        if (orderId <= 0) {
            throw new IllegalArgumentException(
                    "Mã đơn hàng không hợp lệ.");
        }

        if (reason == null
                || reason.isBlank()) {

            throw new IllegalArgumentException(
                    "Vui lòng nhập lý do hủy đơn.");
        }

        if (reason.trim().length() > 500) {
            throw new IllegalArgumentException(
                    "Lý do hủy không được vượt quá 500 ký tự.");
        }
    }

    private void lockAndValidateOrder(
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

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    throw new IllegalArgumentException(
                            "Không tìm thấy đơn hàng.");
                }

                String status = resultSet.getString(
                        "status");

                if (!"IN_PROGRESS".equals(status)) {
                    throw new IllegalStateException(
                            "Chỉ đơn đang chờ pha mới được yêu cầu hủy.");
                }
            }
        }
    }

    private void validateNoPendingRequest(
            Connection connection,
            int orderId) throws SQLException {

        String sql = """
                SELECT COUNT(*) AS total
                FROM order_cancel_requests
                WHERE order_id = ?
                  AND status = 'PENDING'
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()
                        && resultSet.getInt(
                                "total") > 0) {

                    throw new IllegalStateException(
                            "Đơn đã có yêu cầu hủy đang chờ Manager xử lý.");
                }
            }
        }
    }

    private void insertRequest(
            Connection connection,
            int orderId,
            int requestedBy,
            String reason) throws SQLException {

        String sql = """
                INSERT INTO order_cancel_requests(
                    order_id,
                    requested_by,
                    reason,
                    status
                )
                VALUES (?, ?, ?, 'PENDING')
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql)) {

            statement.setInt(
                    1,
                    orderId);

            statement.setInt(
                    2,
                    requestedBy);

            statement.setString(
                    3,
                    reason);

            statement.executeUpdate();
        }
    }

    private void updateOrderToCancelPending(
            Connection connection,
            int orderId) throws SQLException {

        String sql = """
                UPDATE orders
                SET status = 'CANCEL_PENDING'
                WHERE id = ?
                  AND status = 'IN_PROGRESS'
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql)) {

            statement.setInt(
                    1,
                    orderId);

            if (statement.executeUpdate() == 0) {
                throw new SQLException(
                        "Trạng thái đơn đã thay đổi.");
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