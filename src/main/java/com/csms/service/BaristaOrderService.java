package com.csms.service;

import com.csms.config.DatabaseConnection;
import com.csms.dao.BaristaOrderDAO;
import com.csms.dto.BaristaOrderGroup;
import com.csms.entity.OrderItemStatus;
import com.csms.entity.OrderStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class BaristaOrderService {

    private final BaristaOrderDAO baristaOrderDAO;

    public BaristaOrderService() {
        this.baristaOrderDAO = new BaristaOrderDAO();
    }

    public List<BaristaOrderGroup> getQueue() {
        return baristaOrderDAO.findQueue();
    }

    public void startPreparing(int orderId) {
        transitionItems(
                orderId,
                OrderItemStatus.IN_PROGRESS,
                OrderItemStatus.PREPARING);
    }

    public void markPrepared(int orderId) {
        transitionItems(
                orderId,
                OrderItemStatus.PREPARING,
                OrderItemStatus.PREPARED);
    }

    private void transitionItems(
            int orderId,
            OrderItemStatus fromStatus,
            OrderItemStatus toStatus) {
        if (orderId <= 0) {
            throw new IllegalArgumentException(
                    "Mã đơn hàng không hợp lệ.");
        }

        Connection connection = null;

        try {
            connection = DatabaseConnection.getConnection();

            connection.setAutoCommit(false);

            /*
             * Khóa order để hai Barista không cùng nhận một đơn.
             */
            baristaOrderDAO.lockOrder(
                    connection,
                    orderId);

            int affectedRows = baristaOrderDAO
                    .updateItemsByOrder(
                            connection,
                            orderId,
                            fromStatus,
                            toStatus);

            if (affectedRows == 0) {
                throw new IllegalStateException(
                        buildNoTransitionMessage(
                                fromStatus));
            }

            OrderStatus aggregateStatus = resolveOrderStatus(
                    connection,
                    orderId);

            baristaOrderDAO.updateOrderStatus(
                    connection,
                    orderId,
                    aggregateStatus);

            connection.commit();

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            rollbackQuietly(connection);
            throw exception;

        } catch (SQLException exception) {
            rollbackQuietly(connection);

            throw new IllegalStateException(
                    "Không thể cập nhật trạng thái pha chế: "
                            + exception.getMessage(),
                    exception);

        } finally {
            closeQuietly(connection);
        }
    }

    private OrderStatus resolveOrderStatus(
            Connection connection,
            int orderId) throws SQLException {

        Map<OrderItemStatus, Integer> counts = baristaOrderDAO.countItemStatuses(
                connection,
                orderId);

        /*
         * Ưu tiên trạng thái thấp nhất còn tồn tại.
         *
         * Có món mới IN_PROGRESS:
         * → order quay về IN_PROGRESS để Barista nhìn thấy.
         */
        if (hasItems(
                counts,
                OrderItemStatus.IN_PROGRESS)) {
            return OrderStatus.IN_PROGRESS;
        }

        if (hasItems(
                counts,
                OrderItemStatus.PREPARING)) {
            return OrderStatus.PREPARING;
        }

        if (hasItems(
                counts,
                OrderItemStatus.PREPARED)) {
            return OrderStatus.PREPARED;
        }

        if (hasItems(
                counts,
                OrderItemStatus.SERVED)) {
            return OrderStatus.SERVED;
        }

        throw new IllegalStateException(
                "Đơn hàng không còn món hợp lệ.");
    }

    private boolean hasItems(
            Map<OrderItemStatus, Integer> counts,
            OrderItemStatus status) {
        return counts.getOrDefault(
                status,
                0) > 0;
    }

    private String buildNoTransitionMessage(
            OrderItemStatus fromStatus) {
        return switch (fromStatus) {
            case IN_PROGRESS ->
                "Đơn không còn món đang chờ pha hoặc đã được Barista khác nhận.";

            case PREPARING ->
                "Đơn không còn món đang pha chế.";

            default ->
                "Không có món phù hợp để cập nhật.";
        };
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