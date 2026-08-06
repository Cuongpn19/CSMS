package com.csms.dao;

import com.csms.dto.IngredientRequirement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class InventoryTransactionDAO {

    private static final String INSERT_SQL = """
            INSERT INTO inventory_transactions(
                ingredient_id,
                order_id,
                transaction_type,
                quantity,
                quantity_before,
                quantity_after,
                created_by,
                note
            )
            VALUES (?, ?, 'SALE', ?, ?, ?, ?, ?)
            """;

    public void insertSaleTransaction(
            Connection connection,
            int orderId,
            int userId,
            IngredientRequirement requirement) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_SQL)) {

            statement.setInt(
                    1,
                    requirement.ingredientId());

            statement.setInt(
                    2,
                    orderId);

            statement.setBigDecimal(
                    3,
                    requirement.requiredQuantity());

            statement.setBigDecimal(
                    4,
                    requirement.availableQuantity());

            statement.setBigDecimal(
                    5,
                    requirement.availableQuantity()
                            .subtract(
                                    requirement.requiredQuantity()));

            statement.setInt(
                    6,
                    userId);

            statement.setString(
                    7,
                    "Xuất nguyên liệu cho đơn #"
                            + orderId);

            statement.executeUpdate();
        }
    }
}