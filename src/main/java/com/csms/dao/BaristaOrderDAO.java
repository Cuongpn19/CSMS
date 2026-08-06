package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.dto.BaristaOrderGroup;
import com.csms.dto.BaristaOrderItem;
import com.csms.entity.OrderItemStatus;
import com.csms.entity.OrderStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BaristaOrderDAO {

    private static final String FIND_QUEUE_SQL = """
            SELECT
                o.id AS order_id,
                o.order_code,
                o.table_id,
                t.table_number,
                o.created_at,

                u.full_name AS waiter_name,

                od.id AS order_detail_id,
                od.product_id,
                p.name AS product_name,
                od.quantity,
                od.note,
                od.status AS item_status

            FROM orders o

            JOIN coffee_tables t
                ON t.id = o.table_id

            LEFT JOIN users u
                ON u.id = o.cashier_id

            JOIN order_details od
                ON od.order_id = o.id

            JOIN products p
                ON p.id = od.product_id

            WHERE o.status NOT IN (
                'CANCELLED',
                'PAID'
            )
              AND od.status IN (
                'IN_PROGRESS',
                'PREPARING',
                'PREPARED'
              )

            ORDER BY
                o.created_at,
                o.id,
                od.id
            """;

    private static final String LOCK_ORDER_SQL = """
            SELECT id, status
            FROM orders
            WHERE id = ?
            FOR UPDATE
            """;

    private static final String UPDATE_ITEMS_BY_ORDER_SQL = """
            UPDATE order_details
            SET
                status = ?,
                preparing_at =
                    CASE
                        WHEN ? = 'PREPARING'
                        THEN CURRENT_TIMESTAMP
                        ELSE preparing_at
                    END,
                prepared_at =
                    CASE
                        WHEN ? = 'PREPARED'
                        THEN CURRENT_TIMESTAMP
                        ELSE prepared_at
                    END
            WHERE order_id = ?
              AND status = ?
            """;

    private static final String COUNT_ITEMS_BY_STATUS_SQL = """
            SELECT
                status,
                COUNT(*) AS total
            FROM order_details
            WHERE order_id = ?
              AND status <> 'CANCELLED'
            GROUP BY status
            """;

    private static final String UPDATE_ORDER_STATUS_SQL = """
            UPDATE orders
            SET status = ?
            WHERE id = ?
            """;

    public List<BaristaOrderGroup> findQueue() {
        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        FIND_QUEUE_SQL);

                ResultSet resultSet = statement.executeQuery()) {
            return mapGroupedOrders(resultSet);

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải hàng đợi pha chế",
                    exception);
        }
    }

    public void lockOrder(
            Connection connection,
            int orderId) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                LOCK_ORDER_SQL)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    throw new IllegalArgumentException(
                            "Không tìm thấy đơn hàng.");
                }

                String status = resultSet.getString("status");

                if ("CANCELLED".equals(status)
                        || "PAID".equals(status)) {

                    throw new IllegalStateException(
                            "Đơn hàng đã kết thúc hoặc đã bị hủy.");
                }
            }
        }
    }

    public int updateItemsByOrder(
            Connection connection,
            int orderId,
            OrderItemStatus fromStatus,
            OrderItemStatus toStatus) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                UPDATE_ITEMS_BY_ORDER_SQL)) {

            statement.setString(
                    1,
                    toStatus.name());

            statement.setString(
                    2,
                    toStatus.name());

            statement.setString(
                    3,
                    toStatus.name());

            statement.setInt(
                    4,
                    orderId);

            statement.setString(
                    5,
                    fromStatus.name());

            return statement.executeUpdate();
        }
    }

    public Map<OrderItemStatus, Integer> countItemStatuses(
            Connection connection,
            int orderId) throws SQLException {

        Map<OrderItemStatus, Integer> counts = new LinkedHashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(
                COUNT_ITEMS_BY_STATUS_SQL)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    OrderItemStatus status = OrderItemStatus.valueOf(
                            resultSet.getString(
                                    "status"));

                    counts.put(
                            status,
                            resultSet.getInt("total"));
                }
            }
        }

        return counts;
    }

    public void updateOrderStatus(
            Connection connection,
            int orderId,
            OrderStatus status) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                UPDATE_ORDER_STATUS_SQL)) {

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

    private List<BaristaOrderGroup> mapGroupedOrders(
            ResultSet resultSet) throws SQLException {

        Map<GroupKey, GroupBuilder> groups = new LinkedHashMap<>();

        while (resultSet.next()) {
            int orderId = resultSet.getInt("order_id");

            OrderItemStatus itemStatus = OrderItemStatus.valueOf(
                    resultSet.getString(
                            "item_status"));

            /*
             * Một order có thể xuất hiện ở nhiều cột nếu:
             * - Món cũ đã PREPARED
             * - Món mới thêm đang IN_PROGRESS
             */
            GroupKey key = new GroupKey(
                    orderId,
                    itemStatus);

            GroupBuilder builder = groups.computeIfAbsent(
                    key,
                    ignored -> createGroupBuilder(
                            resultSet,
                            itemStatus));

            builder.items.add(
                    new BaristaOrderItem(
                            resultSet.getInt(
                                    "order_detail_id"),

                            resultSet.getInt(
                                    "product_id"),

                            resultSet.getString(
                                    "product_name"),

                            resultSet.getInt(
                                    "quantity"),

                            resultSet.getString(
                                    "note"),

                            itemStatus));
        }

        List<BaristaOrderGroup> result = new ArrayList<>();

        for (GroupBuilder builder : groups.values()) {

            result.add(builder.build());
        }

        return result;
    }

    private GroupBuilder createGroupBuilder(
            ResultSet resultSet,
            OrderItemStatus status) {
        try {
            Timestamp createdAt = resultSet.getTimestamp(
                    "created_at");

            return new GroupBuilder(
                    resultSet.getInt("order_id"),
                    resultSet.getString("order_code"),
                    resultSet.getInt("table_id"),
                    resultSet.getInt("table_number"),
                    resultSet.getString("waiter_name"),
                    createdAt == null
                            ? null
                            : createdAt.toLocalDateTime(),
                    status);

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể ánh xạ dữ liệu Barista.",
                    exception);
        }
    }

    private IllegalStateException databaseException(
            String message,
            SQLException exception) {
        return new IllegalStateException(
                message + ": "
                        + exception.getMessage(),
                exception);
    }

    private record GroupKey(
            int orderId,
            OrderItemStatus status) {
    }

    private static class GroupBuilder {

        private final int orderId;
        private final String orderCode;
        private final int tableId;
        private final int tableNumber;
        private final String waiterName;
        private final java.time.LocalDateTime createdAt;
        private final OrderItemStatus status;

        private final List<BaristaOrderItem> items = new ArrayList<>();

        private GroupBuilder(
                int orderId,
                String orderCode,
                int tableId,
                int tableNumber,
                String waiterName,
                java.time.LocalDateTime createdAt,
                OrderItemStatus status) {
            this.orderId = orderId;
            this.orderCode = orderCode;
            this.tableId = tableId;
            this.tableNumber = tableNumber;
            this.waiterName = waiterName;
            this.createdAt = createdAt;
            this.status = status;
        }

        private BaristaOrderGroup build() {
            return new BaristaOrderGroup(
                    orderId,
                    orderCode,
                    tableId,
                    tableNumber,
                    waiterName,
                    createdAt,
                    status,
                    List.copyOf(items));
        }
    }
}