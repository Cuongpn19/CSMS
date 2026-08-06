package com.csms.dao;

import com.csms.dto.IngredientRequirement;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IngredientStockDAO {

    /*
     * Tổng hợp toàn bộ nguyên liệu cần dùng cho giỏ hàng.
     *
     * Ví dụ:
     * Latte x2 cần 300ml sữa
     * Matcha x1 cần 100ml sữa
     * → tổng nhu cầu sữa = 400ml
     *
     * FOR UPDATE khóa các dòng ingredients cho đến khi
     * transaction commit hoặc rollback.
     */
    private static final String FIND_REQUIREMENTS_FOR_UPDATE_SQL = """
            SELECT
                i.id AS ingredient_id,
                i.name AS ingredient_name,
                i.unit,
                i.stock_quantity AS available_quantity,
                SUM(
                    pr.quantity_required * cart.quantity
                ) AS required_quantity

            FROM ingredients i

            JOIN product_recipes pr
                ON pr.ingredient_id = i.id

            JOIN (
                %s
            ) cart
                ON cart.product_id = pr.product_id

            WHERE i.status = 'ACTIVE'

            GROUP BY
                i.id,
                i.name,
                i.unit,
                i.stock_quantity

            ORDER BY i.id

            FOR UPDATE
            """;

    private static final String DEDUCT_STOCK_SQL = """
            UPDATE ingredients
            SET
                stock_quantity = stock_quantity - ?
            WHERE id = ?
              AND stock_quantity >= ?
            """;

    public List<IngredientRequirement> findRequirementsForUpdate(
            Connection connection,
            List<ProductQuantity> productQuantities) throws SQLException {

        if (productQuantities == null
                || productQuantities.isEmpty()) {

            return List.of();
        }

        String cartSubquery = buildCartSubquery(
                productQuantities.size());

        String sql = FIND_REQUIREMENTS_FOR_UPDATE_SQL
                .formatted(cartSubquery);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            int parameterIndex = 1;

            for (ProductQuantity item : productQuantities) {

                statement.setInt(
                        parameterIndex++,
                        item.productId());

                statement.setInt(
                        parameterIndex++,
                        item.quantity());
            }

            List<IngredientRequirement> requirements = new ArrayList<>();

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    requirements.add(
                            new IngredientRequirement(
                                    resultSet.getInt(
                                            "ingredient_id"),

                                    resultSet.getString(
                                            "ingredient_name"),

                                    resultSet.getString(
                                            "unit"),

                                    safeAmount(
                                            resultSet.getBigDecimal(
                                                    "available_quantity")),

                                    safeAmount(
                                            resultSet.getBigDecimal(
                                                    "required_quantity"))));
                }
            }

            return requirements;
        }
    }

    public void deductStock(
            Connection connection,
            IngredientRequirement requirement) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                DEDUCT_STOCK_SQL)) {

            statement.setBigDecimal(
                    1,
                    requirement.requiredQuantity());

            statement.setInt(
                    2,
                    requirement.ingredientId());

            statement.setBigDecimal(
                    3,
                    requirement.requiredQuantity());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException(
                        "Tồn kho nguyên liệu đã thay đổi hoặc không đủ: "
                                + requirement.ingredientName());
            }
        }
    }

    /*
     * Sinh dạng:
     *
     * SELECT ? AS product_id, ? AS quantity
     * UNION ALL
     * SELECT ?, ?
     */
    private String buildCartSubquery(
            int itemCount) {
        StringBuilder sql = new StringBuilder();

        for (int index = 0; index < itemCount; index++) {

            if (index > 0) {
                sql.append(" UNION ALL ");
            }

            sql.append(
                    "SELECT ? AS product_id, ? AS quantity");
        }

        return sql.toString();
    }

    private BigDecimal safeAmount(
            BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    public record ProductQuantity(
            int productId,
            int quantity) {
    }
}