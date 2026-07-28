package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.entity.Order;
import com.csms.entity.OrderDetail;
import com.csms.entity.OrderStatus;
import com.csms.entity.OrderType;
import com.csms.entity.TableStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDAO {

    private static final String INSERT_ORDER_SQL = """
            INSERT INTO orders(
                table_id,
                cashier_id,
                order_code,
                order_type,
                status,
                subtotal,
                discount,
                total_amount,
                note
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String INSERT_DETAIL_SQL = """
            INSERT INTO order_details(
                order_id,
                product_id,
                product_name,
                unit_price,
                quantity,
                subtotal,
                note
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String FIND_ALL_SQL = """
            SELECT
                o.id,
                o.table_id,
                t.name AS table_name,
                o.cashier_id,
                u.full_name AS cashier_name,
                o.order_code,
                o.order_type,
                o.status,
                o.subtotal,
                o.discount,
                o.total_amount,
                o.note,
                o.created_at
            FROM orders o
            LEFT JOIN coffee_tables t
                ON t.id = o.table_id
            JOIN users u
                ON u.id = o.cashier_id
            ORDER BY o.id DESC
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT
                o.id,
                o.table_id,
                t.name AS table_name,
                o.cashier_id,
                u.full_name AS cashier_name,
                o.order_code,
                o.order_type,
                o.status,
                o.subtotal,
                o.discount,
                o.total_amount,
                o.note,
                o.created_at
            FROM orders o
            LEFT JOIN coffee_tables t
                ON t.id = o.table_id
            JOIN users u
                ON u.id = o.cashier_id
            WHERE o.id = ?
            """;

    private static final String FIND_DETAILS_SQL = """
            SELECT
                id,
                order_id,
                product_id,
                product_name,
                unit_price,
                quantity,
                subtotal,
                note
            FROM order_details
            WHERE order_id = ?
            ORDER BY id
            """;

    private static final String UPDATE_STATUS_SQL = """
            UPDATE orders
            SET status = ?
            WHERE id = ?
            """;

    private static final String UPDATE_TABLE_STATUS_SQL = """
            UPDATE coffee_tables
            SET status = ?
            WHERE id = ?
            """;

    private static final String FIND_PRODUCT_FOR_UPDATE_SQL = """
            SELECT name, quantity, status
            FROM products
            WHERE id = ?
            FOR UPDATE
            """;

    private static final String DECREASE_PRODUCT_QUANTITY_SQL = """
            UPDATE products
            SET quantity = quantity - ?
            WHERE id = ?
              AND quantity >= ?
            """;

    private static final String RESTORE_PRODUCT_QUANTITY_SQL = """
            UPDATE products
            SET quantity = quantity + ?
            WHERE id = ?
            """;

    private static final String FIND_ORDER_STATUS_SQL = """
            SELECT status, table_id
            FROM orders
            WHERE id = ?
            FOR UPDATE
            """;

    public int create(Order order) {
        if (order == null) {
            throw new IllegalArgumentException(
                    "Thông tin đơn hàng không hợp lệ.");
        }

        if (order.getDetails() == null
                || order.getDetails().isEmpty()) {
            throw new IllegalArgumentException(
                    "Đơn hàng phải có ít nhất một sản phẩm.");
        }

        order.calculateTotals();

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                validateAndDecreaseStock(
                        connection,
                        order.getDetails());

                int orderId = insertOrder(
                        connection,
                        order);

                insertDetails(
                        connection,
                        orderId,
                        order.getDetails());

                if (order.getTableId() != null) {
                    updateTableStatus(
                            connection,
                            order.getTableId(),
                            TableStatus.OCCUPIED);
                }

                connection.commit();

                return orderId;

            } catch (SQLException
                    | IllegalArgumentException exception) {

                connection.rollback();
                throw exception;

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tạo đơn hàng",
                    exception);
        }
    }

    private void validateAndDecreaseStock(
            Connection connection,
            List<OrderDetail> details) throws SQLException {

        for (OrderDetail detail : details) {
            if (detail.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "Số lượng sản phẩm phải lớn hơn 0.");
            }

            validateProductStock(
                    connection,
                    detail);

            decreaseProductStock(
                    connection,
                    detail);
        }
    }

    private void validateProductStock(
            Connection connection,
            OrderDetail detail) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                FIND_PRODUCT_FOR_UPDATE_SQL)) {

            statement.setInt(
                    1,
                    detail.getProductId());

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    throw new IllegalArgumentException(
                            "Sản phẩm không còn tồn tại: "
                                    + detail.getProductName());
                }

                String productName = resultSet.getString("name");

                int stockQuantity = resultSet.getInt("quantity");

                String status = resultSet.getString("status");

                if (!"AVAILABLE".equals(status)) {
                    throw new IllegalArgumentException(
                            "Sản phẩm đã ngừng bán: "
                                    + productName);
                }

                if (stockQuantity < detail.getQuantity()) {
                    throw new IllegalArgumentException(
                            "Sản phẩm "
                                    + productName
                                    + " chỉ còn "
                                    + stockQuantity
                                    + " sản phẩm.");
                }

                detail.setProductName(productName);
            }
        }
    }

    private void decreaseProductStock(
            Connection connection,
            OrderDetail detail) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                DECREASE_PRODUCT_QUANTITY_SQL)) {

            statement.setInt(
                    1,
                    detail.getQuantity());

            statement.setInt(
                    2,
                    detail.getProductId());

            statement.setInt(
                    3,
                    detail.getQuantity());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new IllegalArgumentException(
                        "Tồn kho sản phẩm đã thay đổi. "
                                + "Vui lòng tải lại dữ liệu.");
            }
        }
    }

    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        FIND_ALL_SQL);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                orders.add(mapOrder(resultSet));
            }

            return orders;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải danh sách đơn hàng",
                    exception);
        }
    }

    public Optional<Order> findById(int orderId) {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        FIND_BY_ID_SQL)) {
            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                Order order = mapOrder(resultSet);
                order.setDetails(
                        findDetails(
                                connection,
                                orderId));

                return Optional.of(order);
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tìm đơn hàng",
                    exception);
        }
    }

    public boolean updateStatus(
            int orderId,
            OrderStatus status) {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        UPDATE_STATUS_SQL)) {
            statement.setString(1, status.name());
            statement.setInt(2, orderId);

            return statement.executeUpdate() > 0;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể cập nhật trạng thái đơn hàng",
                    exception);
        }
    }

    private int insertOrder(
            Connection connection,
            Order order) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_ORDER_SQL,
                Statement.RETURN_GENERATED_KEYS)) {

            if (order.getTableId() == null) {
                statement.setNull(1, Types.INTEGER);
            } else {
                statement.setInt(
                        1,
                        order.getTableId());
            }

            statement.setInt(
                    2,
                    order.getCashierId());
            statement.setString(
                    3,
                    order.getOrderCode());
            statement.setString(
                    4,
                    order.getOrderType().name());
            statement.setString(
                    5,
                    order.getStatus().name());
            statement.setBigDecimal(
                    6,
                    order.getSubtotal());
            statement.setBigDecimal(
                    7,
                    order.getDiscount());
            statement.setBigDecimal(
                    8,
                    order.getTotalAmount());
            statement.setString(
                    9,
                    order.getNote());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException(
                        "Không thể thêm đơn hàng.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

            throw new SQLException(
                    "Không lấy được ID đơn hàng.");
        }
    }

    private void insertDetails(
            Connection connection,
            int orderId,
            List<OrderDetail> details) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_DETAIL_SQL)) {

            for (OrderDetail detail : details) {
                detail.calculateSubtotal();

                statement.setInt(1, orderId);
                statement.setInt(
                        2,
                        detail.getProductId());
                statement.setString(
                        3,
                        detail.getProductName());
                statement.setBigDecimal(
                        4,
                        detail.getUnitPrice());
                statement.setInt(
                        5,
                        detail.getQuantity());
                statement.setBigDecimal(
                        6,
                        detail.getSubtotal());
                statement.setString(
                        7,
                        detail.getNote());

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    public boolean cancelOrder(int orderId) {
        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                OrderCancellationInfo cancellationInfo = findOrderCancellationInfo(
                        connection,
                        orderId);

                if (cancellationInfo == null) {
                    connection.rollback();
                    return false;
                }

                if (cancellationInfo.status() == OrderStatus.CANCELLED) {
                    throw new IllegalStateException(
                            "Đơn hàng đã được hủy trước đó.");
                }

                if (cancellationInfo.status() == OrderStatus.SERVED) {
                    throw new IllegalStateException(
                            "Không thể hủy đơn hàng đã hoàn thành.");
                }

                if (cancellationInfo.status() == OrderStatus.PAID) {
                    throw new IllegalStateException(
                            "Không thể hủy đơn hàng đã thanh toán.");
                }

                List<OrderDetail> details = findDetails(
                        connection,
                        orderId);

                restoreStock(
                        connection,
                        details);

                updateOrderStatus(
                        connection,
                        orderId,
                        OrderStatus.CANCELLED);

                if (cancellationInfo.tableId() != null) {
                    updateTableStatus(
                            connection,
                            cancellationInfo.tableId(),
                            TableStatus.AVAILABLE);
                }

                connection.commit();
                return true;

            } catch (SQLException
                    | IllegalStateException exception) {

                connection.rollback();
                throw exception;

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể hủy đơn hàng",
                    exception);
        }
    }

    private record OrderCancellationInfo(
            OrderStatus status,
            Integer tableId) {
    }

    private OrderCancellationInfo findOrderCancellationInfo(
            Connection connection,
            int orderId) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                FIND_ORDER_STATUS_SQL)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                int tableId = resultSet.getInt("table_id");

                Integer nullableTableId = resultSet.wasNull()
                        ? null
                        : tableId;

                return new OrderCancellationInfo(
                        OrderStatus.valueOf(
                                resultSet.getString("status")),
                        nullableTableId);
            }
        }
    }

    private void restoreStock(
            Connection connection,
            List<OrderDetail> details) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                RESTORE_PRODUCT_QUANTITY_SQL)) {

            for (OrderDetail detail : details) {
                statement.setInt(
                        1,
                        detail.getQuantity());

                statement.setInt(
                        2,
                        detail.getProductId());

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void updateOrderStatus(
            Connection connection,
            int orderId,
            OrderStatus status) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                UPDATE_STATUS_SQL)) {

            statement.setString(
                    1,
                    status.name());

            statement.setInt(
                    2,
                    orderId);

            statement.executeUpdate();
        }
    }

    private List<OrderDetail> findDetails(
            Connection connection,
            int orderId) throws SQLException {

        List<OrderDetail> details = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(
                FIND_DETAILS_SQL)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    OrderDetail detail = new OrderDetail();

                    detail.setId(
                            resultSet.getInt("id"));
                    detail.setOrderId(
                            resultSet.getInt("order_id"));
                    detail.setProductId(
                            resultSet.getInt("product_id"));
                    detail.setProductName(
                            resultSet.getString(
                                    "product_name"));
                    detail.setUnitPrice(
                            resultSet.getBigDecimal(
                                    "unit_price"));
                    detail.setQuantity(
                            resultSet.getInt("quantity"));
                    detail.setSubtotal(
                            resultSet.getBigDecimal(
                                    "subtotal"));
                    detail.setNote(
                            resultSet.getString("note"));

                    details.add(detail);
                }
            }
        }

        return details;
    }

    private void updateTableStatus(
            Connection connection,
            int tableId,
            TableStatus status) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                UPDATE_TABLE_STATUS_SQL)) {

            statement.setString(1, status.name());
            statement.setInt(2, tableId);
            statement.executeUpdate();
        }
    }

    private Order mapOrder(
            ResultSet resultSet) throws SQLException {

        Order order = new Order();

        order.setId(resultSet.getInt("id"));

        int tableId = resultSet.getInt("table_id");

        if (resultSet.wasNull()) {
            order.setTableId(null);
        } else {
            order.setTableId(tableId);
        }

        order.setTableName(
                resultSet.getString("table_name"));
        order.setCashierId(
                resultSet.getInt("cashier_id"));
        order.setCashierName(
                resultSet.getString("cashier_name"));
        order.setOrderCode(
                resultSet.getString("order_code"));
        order.setOrderType(
                OrderType.valueOf(
                        resultSet.getString("order_type")));
        order.setStatus(
                OrderStatus.valueOf(
                        resultSet.getString("status")));
        order.setSubtotal(
                resultSet.getBigDecimal("subtotal"));
        order.setDiscount(
                resultSet.getBigDecimal("discount"));
        order.setTotalAmount(
                resultSet.getBigDecimal("total_amount"));
        order.setNote(
                resultSet.getString("note"));
        order.setCreatedAt(
                resultSet.getTimestamp("created_at")
                        .toLocalDateTime());

        return order;
    }

    private IllegalStateException databaseException(
            String message,
            SQLException exception) {
        return new IllegalStateException(
                message + ": " + exception.getMessage(),
                exception);
    }
}