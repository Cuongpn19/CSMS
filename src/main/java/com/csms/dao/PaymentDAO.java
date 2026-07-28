package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.entity.OrderStatus;
import com.csms.entity.Payment;
import com.csms.entity.PaymentMethod;
import com.csms.entity.TableStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class PaymentDAO {

    private static final String FIND_ORDER_FOR_PAYMENT_SQL = """
            SELECT
                id,
                table_id,
                status,
                total_amount
            FROM orders
            WHERE id = ?
            FOR UPDATE
            """;

    private static final String FIND_PAYMENT_BY_ORDER_SQL = """
            SELECT
                id,
                order_id,
                payment_method,
                amount_received,
                change_amount,
                paid_at
            FROM payments
            WHERE order_id = ?
            """;

    private static final String INSERT_PAYMENT_SQL = """
            INSERT INTO payments(
                order_id,
                payment_method,
                amount_received,
                change_amount
            )
            VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE_ORDER_STATUS_SQL = """
            UPDATE orders
            SET status = ?
            WHERE id = ?
            """;

    private static final String UPDATE_TABLE_STATUS_SQL = """
            UPDATE coffee_tables
            SET status = ?
            WHERE id = ?
            """;

    public Payment createPayment(
            int orderId,
            PaymentMethod paymentMethod,
            BigDecimal amountReceived) {
        if (orderId <= 0) {
            throw new IllegalArgumentException(
                    "Mã đơn hàng không hợp lệ.");
        }

        if (paymentMethod == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn phương thức thanh toán.");
        }

        if (amountReceived == null
                || amountReceived.compareTo(
                        BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Số tiền khách đưa không hợp lệ.");
        }

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                if (paymentExists(
                        connection,
                        orderId)) {
                    throw new IllegalStateException(
                            "Đơn hàng này đã được thanh toán.");
                }

                OrderPaymentInfo orderInfo = findOrderForPayment(
                        connection,
                        orderId);

                if (orderInfo == null) {
                    throw new IllegalArgumentException(
                            "Không tìm thấy đơn hàng.");
                }

                if (orderInfo.status() != OrderStatus.PENDING_PAYMENT) {
                    throw new IllegalStateException(
                            "Chỉ có thể thanh toán đơn đang chờ thanh toán.");
                }

                BigDecimal requiredAmount = orderInfo.totalAmount();

                BigDecimal acceptedAmount = normalizeAmountReceived(
                        paymentMethod,
                        amountReceived,
                        requiredAmount);

                BigDecimal changeAmount = acceptedAmount.subtract(
                        requiredAmount);

                Payment payment = new Payment();

                payment.setOrderId(orderId);
                payment.setPaymentMethod(paymentMethod);
                payment.setAmountReceived(acceptedAmount);
                payment.setChangeAmount(changeAmount);

                int paymentId = insertPayment(
                        connection,
                        payment);

                updateOrderStatus(
                        connection,
                        orderId,
                        OrderStatus.PAID);

                if (orderInfo.tableId() != null) {
                    updateTableStatus(
                            connection,
                            orderInfo.tableId(),
                            TableStatus.AVAILABLE);
                }

                connection.commit();

                payment.setId(paymentId);

                return payment;

            } catch (
                    SQLException
                    | IllegalArgumentException
                    | IllegalStateException exception) {
                connection.rollback();
                throw exception;

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể thanh toán đơn hàng: "
                            + exception.getMessage(),
                    exception);
        }
    }

    public Optional<Payment> findByOrderId(
            int orderId) {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        FIND_PAYMENT_BY_ORDER_SQL)) {
            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        mapPayment(resultSet));
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể tải thông tin thanh toán: "
                            + exception.getMessage(),
                    exception);
        }
    }

    private boolean paymentExists(
            Connection connection,
            int orderId) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                FIND_PAYMENT_BY_ORDER_SQL)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {

                return resultSet.next();
            }
        }
    }

    private OrderPaymentInfo findOrderForPayment(
            Connection connection,
            int orderId) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                FIND_ORDER_FOR_PAYMENT_SQL)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                int tableId = resultSet.getInt("table_id");

                Integer nullableTableId = resultSet.wasNull()
                        ? null
                        : tableId;

                return new OrderPaymentInfo(
                        nullableTableId,
                        OrderStatus.valueOf(
                                resultSet.getString("status")),
                        resultSet.getBigDecimal(
                                "total_amount"));
            }
        }
    }

    private BigDecimal normalizeAmountReceived(
            PaymentMethod paymentMethod,
            BigDecimal amountReceived,
            BigDecimal totalAmount) {
        if (paymentMethod == PaymentMethod.CASH) {
            if (amountReceived.compareTo(totalAmount) < 0) {
                throw new IllegalArgumentException(
                        "Tiền khách đưa không đủ.");
            }

            return amountReceived;
        }

        /*
         * Chuyển khoản và thẻ thường thanh toán đúng
         * số tiền của đơn hàng.
         */
        return totalAmount;
    }

    private int insertPayment(
            Connection connection,
            Payment payment) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_PAYMENT_SQL,
                Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(
                    1,
                    payment.getOrderId());

            statement.setString(
                    2,
                    payment.getPaymentMethod().name());

            statement.setBigDecimal(
                    3,
                    payment.getAmountReceived());

            statement.setBigDecimal(
                    4,
                    payment.getChangeAmount());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException(
                        "Không thể lưu giao dịch thanh toán.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

            throw new SQLException(
                    "Không lấy được ID thanh toán.");
        }
    }

    private void updateOrderStatus(
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
                        "Không thể cập nhật trạng thái đơn hàng.");
            }
        }
    }

    private void updateTableStatus(
            Connection connection,
            int tableId,
            TableStatus status) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                UPDATE_TABLE_STATUS_SQL)) {

            statement.setString(
                    1,
                    status.name());

            statement.setInt(
                    2,
                    tableId);

            statement.executeUpdate();
        }
    }

    private Payment mapPayment(
            ResultSet resultSet) throws SQLException {

        Payment payment = new Payment();

        payment.setId(
                resultSet.getInt("id"));

        payment.setOrderId(
                resultSet.getInt("order_id"));

        payment.setPaymentMethod(
                PaymentMethod.valueOf(
                        resultSet.getString(
                                "payment_method")));

        payment.setAmountReceived(
                resultSet.getBigDecimal(
                        "amount_received"));

        payment.setChangeAmount(
                resultSet.getBigDecimal(
                        "change_amount"));

        if (resultSet.getTimestamp("paid_at") != null) {
            payment.setPaidAt(
                    resultSet.getTimestamp("paid_at")
                            .toLocalDateTime());
        }

        return payment;
    }

    private record OrderPaymentInfo(
            Integer tableId,
            OrderStatus status,
            BigDecimal totalAmount) {
    }
}