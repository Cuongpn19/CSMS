package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.entity.CoffeeTable;
import com.csms.entity.OrderStatus;
import com.csms.entity.TableDashboardItem;
import com.csms.entity.TableStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CoffeeTableDAO {

    private static final String ACTIVE_ORDER_STATUSES = """
            'IN_PROGRESS',
            'PREPARING',
            'PREPARED',
            'SERVED',
            'WAITING_PAYMENT',
            'CANCEL_PENDING'
            """;

    private static final String FIND_ALL_DASHBOARD_SQL = """
            SELECT
                t.id AS table_id,
                t.table_number,
                t.status AS table_status,

                o.id AS order_id,
                o.order_code,
                o.status AS order_status,

                o.subtotal,
                o.vat_amount,
                o.total_amount,
                o.cashier_id AS waiter_id,
                u.full_name AS waiter_name,

                o.created_at AS order_created_at,

                COUNT(
                    CASE
                        WHEN od.status <> 'CANCELLED'
                        THEN od.id
                    END
                ) AS item_count,

                COALESCE(
                    SUM(
                        CASE
                            WHEN od.status <> 'CANCELLED'
                            THEN od.quantity
                            ELSE 0
                        END
                    ),
                    0
                ) AS total_quantity

            FROM coffee_tables t

            LEFT JOIN orders o
                ON o.table_id = t.id
               AND o.status IN (
            """
            + ACTIVE_ORDER_STATUSES
            + """
                       )

                    LEFT JOIN users u
                        ON u.id = o.cashier_id

                    LEFT JOIN order_details od
                        ON od.order_id = o.id

                    GROUP BY
                        t.id,
                        t.table_number,
                        t.status,

                        o.id,
                        o.order_code,
                        o.status,

                        o.subtotal,
                        o.vat_amount,
                        o.total_amount,

                        o.cashier_id,
                        u.full_name,

                        o.created_at

                    ORDER BY t.table_number
                    """;

    private static final String FIND_BY_ID_SQL = """
            SELECT
                id,
                table_number,
                status,
                created_at,
                updated_at
            FROM coffee_tables
            WHERE id = ?
            """;

    private static final String FIND_AVAILABLE_SQL = """
            SELECT
                t.id,
                t.table_number,
                t.status,
                t.created_at,
                t.updated_at
            FROM coffee_tables t
            WHERE t.status = 'AVAILABLE'
              AND NOT EXISTS (
                    SELECT 1
                    FROM orders o
                    WHERE o.table_id = t.id
                      AND o.status IN (
            """
            + ACTIVE_ORDER_STATUSES
            + """
                              )
                      )
                    ORDER BY t.table_number
                    """;

    private static final String UPDATE_STATUS_SQL = """
            UPDATE coffee_tables
            SET status = ?
            WHERE id = ?
            """;

    private static final String EXISTS_ACTIVE_ORDER_SQL = """
            SELECT COUNT(*) AS total
            FROM orders
            WHERE table_id = ?
              AND status IN (
            """
            + ACTIVE_ORDER_STATUSES
            + """
                      )
                    """;

    private static final String LOCK_TABLE_SQL = """
            SELECT
                id,
                table_number,
                status
            FROM coffee_tables
            WHERE id = ?
            FOR UPDATE
            """;

    public List<CoffeeTable> findAvailable() {
        String sql = """
                SELECT
                    id,
                    table_number,
                    capacity,
                    status,
                    created_at
                FROM coffee_tables
                WHERE status = 'AVAILABLE'
                ORDER BY table_number
                """;

        List<CoffeeTable> tables = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql);

                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                CoffeeTable table = new CoffeeTable();

                table.setId(
                        resultSet.getInt("id"));

                table.setTableNumber(
                        resultSet.getInt("table_number"));

                table.setCapacity(
                        resultSet.getInt("capacity"));

                table.setStatus(
                        TableStatus.valueOf(
                                resultSet.getString("status")));

                Timestamp createdAt = resultSet.getTimestamp("created_at");

                if (createdAt != null) {
                    table.setCreatedAt(
                            createdAt.toLocalDateTime());
                }

                tables.add(table);
            }

            return tables;

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể tải danh sách bàn trống: "
                            + exception.getMessage(),
                    exception);
        }
    }

    public List<TableDashboardItem> findAllDashboard() {
        List<TableDashboardItem> items = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        FIND_ALL_DASHBOARD_SQL);

                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                items.add(
                        mapDashboardItem(resultSet));
            }

            return items;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải Dashboard bàn",
                    exception);
        }
    }

    public Optional<CoffeeTable> findById(
            int tableId) {
        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        FIND_BY_ID_SQL)) {
            statement.setInt(1, tableId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        mapCoffeeTable(resultSet));
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải thông tin bàn",
                    exception);
        }
    }

    public List<CoffeeTable> findAvailableTables() {
        List<CoffeeTable> tables = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        FIND_AVAILABLE_SQL);

                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                tables.add(
                        mapCoffeeTable(resultSet));
            }

            return tables;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải danh sách bàn trống",
                    exception);
        }
    }

    public void updateStatus(
            int tableId,
            TableStatus status) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Trạng thái bàn không hợp lệ.");
        }

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        UPDATE_STATUS_SQL)) {
            statement.setString(
                    1,
                    status.name());

            statement.setInt(
                    2,
                    tableId);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Không tìm thấy bàn.");
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể cập nhật trạng thái bàn",
                    exception);
        }
    }

    public boolean hasActiveOrder(
            int tableId) {
        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        EXISTS_ACTIVE_ORDER_SQL)) {
            statement.setInt(
                    1,
                    tableId);

            try (ResultSet resultSet = statement.executeQuery()) {

                return resultSet.next()
                        && resultSet.getInt("total") > 0;
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể kiểm tra đơn đang hoạt động của bàn",
                    exception);
        }
    }

    /*
     * Dùng bên trong transaction tạo đơn.
     * Không tự đóng Connection được truyền vào.
     */
    public Optional<CoffeeTable> findByIdForUpdate(
            Connection connection,
            int tableId) throws SQLException {

        try (
                PreparedStatement statement = connection.prepareStatement(
                        LOCK_TABLE_SQL)) {
            statement.setInt(
                    1,
                    tableId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                CoffeeTable table = new CoffeeTable();

                table.setId(
                        resultSet.getInt("id"));

                table.setTableNumber(
                        resultSet.getInt(
                                "table_number"));

                table.setStatus(
                        TableStatus.valueOf(
                                resultSet.getString(
                                        "status")));

                return Optional.of(table);
            }
        }
    }

    public void updateStatus(
            Connection connection,
            int tableId,
            TableStatus status) throws SQLException {

        String sql = """
                UPDATE coffee_tables
                SET status = ?
                WHERE id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    status.name());

            statement.setInt(
                    2,
                    tableId);

            if (statement.executeUpdate() == 0) {
                throw new SQLException(
                        "Không tìm thấy bàn cần cập nhật.");
            }
        }
    }

    public boolean hasActiveOrder(
            Connection connection,
            int tableId) throws SQLException {

        try (
                PreparedStatement statement = connection.prepareStatement(
                        EXISTS_ACTIVE_ORDER_SQL)) {
            statement.setInt(
                    1,
                    tableId);

            try (ResultSet resultSet = statement.executeQuery()) {

                return resultSet.next()
                        && resultSet.getInt("total") > 0;
            }
        }
    }

    private CoffeeTable mapCoffeeTable(
            ResultSet resultSet) throws SQLException {

        CoffeeTable table = new CoffeeTable();

        table.setId(
                resultSet.getInt("id"));

        table.setTableNumber(
                resultSet.getInt(
                        "table_number"));

        table.setStatus(
                TableStatus.valueOf(
                        resultSet.getString(
                                "status")));

        Timestamp createdAt = getTimestampIfPresent(
                resultSet,
                "created_at");

        if (createdAt != null) {
            table.setCreatedAt(
                    createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = getTimestampIfPresent(
                resultSet,
                "updated_at");

        if (updatedAt != null) {
            table.setUpdatedAt(
                    updatedAt.toLocalDateTime());
        }

        return table;
    }

    private TableDashboardItem mapDashboardItem(
            ResultSet resultSet) throws SQLException {

        TableDashboardItem item = new TableDashboardItem();

        item.setTableId(
                resultSet.getInt(
                        "table_id"));

        item.setTableNumber(
                resultSet.getInt(
                        "table_number"));

        item.setTableStatus(
                TableStatus.valueOf(
                        resultSet.getString(
                                "table_status")));

        int orderId = resultSet.getInt(
                "order_id");

        if (!resultSet.wasNull()) {
            item.setOrderId(orderId);
        }

        item.setOrderCode(
                resultSet.getString(
                        "order_code"));

        String orderStatus = resultSet.getString(
                "order_status");

        if (orderStatus != null) {
            item.setOrderStatus(
                    OrderStatus.valueOf(
                            orderStatus));
        }

        item.setSubtotal(
                safeAmount(
                        resultSet.getBigDecimal(
                                "subtotal")));

        item.setVatAmount(
                safeAmount(
                        resultSet.getBigDecimal(
                                "vat_amount")));

        item.setTotalAmount(
                safeAmount(
                        resultSet.getBigDecimal(
                                "total_amount")));

        int waiterId = resultSet.getInt(
                "waiter_id");

        if (!resultSet.wasNull()) {
            item.setWaiterId(waiterId);
        }

        item.setWaiterName(
                resultSet.getString(
                        "waiter_name"));

        item.setItemCount(
                resultSet.getInt(
                        "item_count"));

        item.setTotalQuantity(
                resultSet.getInt(
                        "total_quantity"));

        Timestamp orderCreatedAt = resultSet.getTimestamp(
                "order_created_at");

        if (orderCreatedAt != null) {
            item.setOrderCreatedAt(
                    orderCreatedAt
                            .toLocalDateTime());
        }

        return item;
    }

    /*
     * Tránh lỗi khi một SELECT tối giản không có created_at/updated_at.
     */
    private Timestamp getTimestampIfPresent(
            ResultSet resultSet,
            String columnName) {
        try {
            return resultSet.getTimestamp(
                    columnName);

        } catch (SQLException ignored) {
            return null;
        }
    }

    private BigDecimal safeAmount(
            BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private IllegalStateException databaseException(
            String message,
            SQLException exception) {
        return new IllegalStateException(
                message
                        + ": "
                        + exception.getMessage(),
                exception);
    }
}