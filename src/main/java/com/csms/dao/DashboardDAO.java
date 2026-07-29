package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.dto.DailyRevenue;
import com.csms.dto.DashboardStats;
import com.csms.dto.LowStockProduct;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardDAO {

    private static final int LOW_STOCK_THRESHOLD = 10;

    /*
     * Doanh thu được tính theo thời gian thanh toán,
     * không tính theo thời gian tạo đơn.
     */
    private static final String TODAY_REVENUE_SQL = """
            SELECT COALESCE(SUM(o.total_amount), 0) AS revenue
            FROM payments p
            JOIN orders o ON o.id = p.order_id
            WHERE DATE(p.paid_at) = CURRENT_DATE
              AND o.status IN ('PAID', 'COMPLETED')
            """;

    /*
     * Số đơn tạo hôm nay, không bao gồm đơn đã hủy.
     */
    private static final String TODAY_ORDER_COUNT_SQL = """
            SELECT COUNT(*) AS order_count
            FROM orders
            WHERE DATE(created_at) = CURRENT_DATE
              AND status <> 'CANCELLED'
            """;

    private static final String OCCUPIED_TABLE_COUNT_SQL = """
            SELECT COUNT(*) AS occupied_count
            FROM coffee_tables
            WHERE status = 'OCCUPIED'
            """;

    private static final String LOW_STOCK_COUNT_SQL = """
            SELECT COUNT(*) AS low_stock_count
            FROM products
            WHERE status = 'AVAILABLE'
              AND quantity <= ?
            """;

    /*
     * Lấy doanh thu có phát sinh trong 7 ngày gần nhất.
     * Những ngày không có doanh thu sẽ được bổ sung bằng 0
     * trong Java.
     */
    private static final String SEVEN_DAY_REVENUE_SQL = """
            SELECT
                DATE(p.paid_at) AS revenue_date,
                COALESCE(SUM(o.total_amount), 0) AS revenue
            FROM payments p
            JOIN orders o ON o.id = p.order_id
            WHERE p.paid_at >= CURRENT_DATE - INTERVAL 6 DAY
              AND p.paid_at < CURRENT_DATE + INTERVAL 1 DAY
              AND o.status IN ('PAID', 'COMPLETED')
            GROUP BY DATE(p.paid_at)
            ORDER BY revenue_date
            """;

    private static final String LOW_STOCK_PRODUCTS_SQL = """
            SELECT
                p.id,
                p.name,
                c.name AS category_name,
                p.price,
                p.quantity
            FROM products p
            JOIN categories c ON c.id = p.category_id
            WHERE p.status = 'AVAILABLE'
              AND p.quantity <= ?
            ORDER BY p.quantity ASC, p.name ASC
            LIMIT 20
            """;

    public DashboardStats getDashboardStats() {
        try (Connection connection = DatabaseConnection.getConnection()) {

            BigDecimal todayRevenue = getTodayRevenue(connection);

            int todayOrderCount = getTodayOrderCount(connection);

            int occupiedTableCount = getOccupiedTableCount(connection);

            int lowStockProductCount = getLowStockProductCount(connection);

            return new DashboardStats(
                    todayRevenue,
                    todayOrderCount,
                    occupiedTableCount,
                    lowStockProductCount);

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải thống kê Dashboard",
                    exception);
        }
    }

    public List<DailyRevenue> getSevenDayRevenue() {
        LocalDate today = LocalDate.now();

        /*
         * LinkedHashMap giữ đúng thứ tự từ ngày cũ
         * đến ngày hiện tại.
         */
        Map<LocalDate, BigDecimal> revenueMap = new LinkedHashMap<>();

        for (int index = 6; index >= 0; index--) {
            revenueMap.put(
                    today.minusDays(index),
                    BigDecimal.ZERO);
        }

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        SEVEN_DAY_REVENUE_SQL);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Date sqlDate = resultSet.getDate("revenue_date");

                if (sqlDate == null) {
                    continue;
                }

                LocalDate date = sqlDate.toLocalDate();

                BigDecimal revenue = resultSet.getBigDecimal("revenue");

                if (revenueMap.containsKey(date)) {
                    revenueMap.put(
                            date,
                            revenue == null
                                    ? BigDecimal.ZERO
                                    : revenue);
                }
            }

            List<DailyRevenue> result = new ArrayList<>();

            revenueMap.forEach(
                    (date, revenue) -> result.add(
                            new DailyRevenue(
                                    date,
                                    revenue)));

            return result;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải doanh thu 7 ngày",
                    exception);
        }
    }

    public List<LowStockProduct> getLowStockProducts() {
        List<LowStockProduct> products = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        LOW_STOCK_PRODUCTS_SQL)) {
            statement.setInt(
                    1,
                    LOW_STOCK_THRESHOLD);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    products.add(
                            new LowStockProduct(
                                    resultSet.getInt("id"),
                                    resultSet.getString("name"),
                                    resultSet.getString(
                                            "category_name"),
                                    resultSet.getBigDecimal(
                                            "price"),
                                    resultSet.getInt("quantity")));
                }
            }

            return products;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải sản phẩm sắp hết hàng",
                    exception);
        }
    }

    private BigDecimal getTodayRevenue(
            Connection connection) throws SQLException {

        try (
                PreparedStatement statement = connection.prepareStatement(
                        TODAY_REVENUE_SQL);
                ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                return BigDecimal.ZERO;
            }

            BigDecimal revenue = resultSet.getBigDecimal("revenue");

            return revenue == null
                    ? BigDecimal.ZERO
                    : revenue;
        }
    }

    private int getTodayOrderCount(
            Connection connection) throws SQLException {

        try (
                PreparedStatement statement = connection.prepareStatement(
                        TODAY_ORDER_COUNT_SQL);
                ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next()
                    ? resultSet.getInt("order_count")
                    : 0;
        }
    }

    private int getOccupiedTableCount(
            Connection connection) throws SQLException {

        try (
                PreparedStatement statement = connection.prepareStatement(
                        OCCUPIED_TABLE_COUNT_SQL);
                ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next()
                    ? resultSet.getInt("occupied_count")
                    : 0;
        }
    }

    private int getLowStockProductCount(
            Connection connection) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                LOW_STOCK_COUNT_SQL)) {

            statement.setInt(
                    1,
                    LOW_STOCK_THRESHOLD);

            try (ResultSet resultSet = statement.executeQuery()) {

                return resultSet.next()
                        ? resultSet.getInt(
                                "low_stock_count")
                        : 0;
            }
        }
    }

    private IllegalStateException databaseException(
            String message,
            SQLException exception) {
        return new IllegalStateException(
                message + ": " + exception.getMessage(),
                exception);
    }
}