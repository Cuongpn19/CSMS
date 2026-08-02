package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.entity.Ingredient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IngredientDAO {

    private static final String BASE_SELECT = """
            SELECT
                id,
                name,
                unit,
                quantity,
                minimum_stock,
                import_price,
                status,
                created_at,
                updated_at
            FROM ingredients
            """;

    private static final String FIND_ALL_ACTIVE_SQL = BASE_SELECT + """
            WHERE status = 'ACTIVE'
            ORDER BY name
            """;

    private static final String FIND_BY_ID_SQL = BASE_SELECT + """
            WHERE id = ?
            """;

    public List<Ingredient> findAllActive() {
        String sql = """
                SELECT
                    id,
                    name,
                    unit,
                    quantity,
                    minimum_stock,
                    import_price,
                    status
                FROM ingredients
                WHERE status = 'ACTIVE'
                ORDER BY name
                """;

        List<Ingredient> ingredients = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql);

                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();

                ingredient.setId(
                        resultSet.getInt("id"));

                ingredient.setName(
                        resultSet.getString("name"));

                ingredient.setUnit(
                        resultSet.getString("unit"));

                ingredient.setQuantity(resultSet.getBigDecimal("quantity"));

                ingredient.setMinimumStock(
                        resultSet.getBigDecimal(
                                "minimum_stock"));

                ingredient.setStatus(resultSet.getString("status"));

                ingredients.add(ingredient);
            }

            return ingredients;

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể tải nguyên liệu: "
                            + exception.getMessage(),
                    exception);
        }
    }

    public Optional<Ingredient> findById(
            int ingredientId) {
        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        FIND_BY_ID_SQL)) {
            statement.setInt(
                    1,
                    ingredientId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        mapIngredient(resultSet));
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể tải nguyên liệu: "
                            + exception.getMessage(),
                    exception);
        }
    }

    private Ingredient mapIngredient(
            ResultSet resultSet) throws SQLException {

        Ingredient ingredient = new Ingredient();

        ingredient.setId(
                resultSet.getInt("id"));

        ingredient.setName(
                resultSet.getString("name"));

        ingredient.setUnit(
                resultSet.getString("unit"));

        ingredient.setQuantity(
                resultSet.getBigDecimal("quantity"));

        ingredient.setMinimumStock(
                resultSet.getBigDecimal(
                        "minimum_stock"));

        ingredient.setImportPrice(
                resultSet.getBigDecimal(
                        "import_price"));

        ingredient.setStatus(
                resultSet.getString("status"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");

        if (createdAt != null) {
            ingredient.setCreatedAt(
                    createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");

        if (updatedAt != null) {
            ingredient.setUpdatedAt(
                    updatedAt.toLocalDateTime());
        }

        return ingredient;
    }
}