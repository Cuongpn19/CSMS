package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.dto.RevenueReportRow;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RevenueReportDAO {

    /*
     * Báo cáo từng ngày trong một tháng.
     *
     * Dùng payments.amount làm doanh thu thực thu,
     * tránh cộng trùng orders.total_amount nếu một đơn
     * có nhiều bản ghi payment.
     */
    private static final String MONTHLY_REPORT_SQL = """
                SELECT
                payment_data.report_date,

                COUNT(o.id)
                    AS order_count,

                COALESCE(
                    SUM(o.subtotal),
                    0
                ) AS subtotal,

                COALESCE(
                    SUM(o.discount),
                    0
                ) AS discount_amount,

                COALESCE(
                    SUM(o.vat_amount),
                    0
                ) AS vat_amount,

                COALESCE(
                    SUM(payment_data.paid_amount),
                    0
                ) AS revenue

            FROM (
                SELECT
                    order_id,
                    DATE(MAX(paid_at))
                        AS report_date,
                    SUM(amount)
                        AS paid_amount
                FROM payments
                GROUP BY order_id
            ) payment_data

            JOIN orders o
                ON o.id = payment_data.order_id

            WHERE YEAR(payment_data.report_date) = ?
              AND MONTH(payment_data.report_date) = ?
              AND o.status IN (
                    'PAID',
                    'COMPLETED'
              )

            GROUP BY payment_data.report_date

            ORDER BY payment_data.report_date
            """;

    /*
     * Báo cáo từng tháng trong một năm.
     */
    private static final String YEARLY_REPORT_SQL = """
            SELECT
                MONTH(p.paid_at) AS report_month,

                COUNT(DISTINCT o.id)
                    AS order_count,

                COALESCE(
                    SUM(o.subtotal),
                    0
                ) AS subtotal,

                COALESCE(
                    SUM(o.discount),
                    0
                ) AS discount_amount,

                COALESCE(
                    SUM(o.vat_amount),
                    0
                ) AS vat_amount,

                COALESCE(
                    SUM(p.amount),
                    0
                ) AS revenue

            FROM payments p

            JOIN orders o
                ON o.id = p.order_id

            WHERE YEAR(p.paid_at) = ?
              AND o.status IN (
                    'PAID',
                    'COMPLETED'
              )

            GROUP BY MONTH(p.paid_at)

            ORDER BY report_month
            """;

    public List<RevenueReportRow> findMonthlyReport(
            int year,
            int month) {
        List<RevenueReportRow> rows = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        MONTHLY_REPORT_SQL)) {
            statement.setInt(1, year);
            statement.setInt(2, month);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    rows.add(
                            new RevenueReportRow(
                                    resultSet
                                            .getDate(
                                                    "report_date")
                                            .toLocalDate()
                                            .format(
                                                    java.time.format.DateTimeFormatter
                                                            .ofPattern(
                                                                    "dd/MM/yyyy")),

                                    resultSet.getInt(
                                            "order_count"),

                                    safeAmount(
                                            resultSet
                                                    .getBigDecimal(
                                                            "subtotal")),

                                    safeAmount(
                                            resultSet
                                                    .getBigDecimal(
                                                            "discount_amount")),

                                    safeAmount(
                                            resultSet
                                                    .getBigDecimal(
                                                            "vat_amount")),

                                    safeAmount(
                                            resultSet
                                                    .getBigDecimal(
                                                            "revenue"))));
                }
            }

            return rows;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải báo cáo doanh thu theo tháng",
                    exception);
        }
    }

    public List<RevenueReportRow> findYearlyReport(
            int year) {
        List<RevenueReportRow> rows = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        YEARLY_REPORT_SQL)) {
            statement.setInt(1, year);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    int monthNumber = resultSet.getInt(
                            "report_month");

                    String monthLabel = Month.of(monthNumber)
                            .getDisplayName(
                                    TextStyle.FULL,
                                    Locale.forLanguageTag(
                                            "vi-VN"));

                    rows.add(
                            new RevenueReportRow(
                                    capitalize(
                                            monthLabel),

                                    resultSet.getInt(
                                            "order_count"),

                                    safeAmount(
                                            resultSet
                                                    .getBigDecimal(
                                                            "subtotal")),

                                    safeAmount(
                                            resultSet
                                                    .getBigDecimal(
                                                            "discount_amount")),

                                    safeAmount(
                                            resultSet
                                                    .getBigDecimal(
                                                            "vat_amount")),

                                    safeAmount(
                                            resultSet
                                                    .getBigDecimal(
                                                            "revenue"))));
                }
            }

            return rows;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải báo cáo doanh thu theo năm",
                    exception);
        }
    }

    private BigDecimal safeAmount(
            BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private String capitalize(
            String value) {
        if (value == null
                || value.isBlank()) {
            return "";
        }

        return Character.toUpperCase(
                value.charAt(0)) + value.substring(1);
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