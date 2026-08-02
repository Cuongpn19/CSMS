package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.entity.Ingredient;
import com.csms.entity.IngredientStatus;
import com.csms.entity.IngredientUnit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
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

    private static final String FIND_ALL_SQL = BASE_SELECT + """
            ORDER BY id DESC
            """;

    private static final String FIND_ALL_ACTIVE_SQL = BASE_SELECT + """
            WHERE status = 'ACTIVE'
            ORDER BY name
            """;

    private static final String FIND_BY_ID_SQL = BASE_SELECT + """
            WHERE id = ?
            """;

    private static final String SEARCH_SQL = BASE_SELECT + """
            WHERE (
                    ? = ''
                    OR LOWER(name) LIKE LOWER(?)
                    OR LOWER(unit) LIKE LOWER(?)
                  )
              AND (
                    ? IS NULL
                    OR status = ?
                  )
              AND (
                    ? = FALSE
                    OR quantity <= minimum_stock
                  )
            ORDER BY
                CASE
                    WHEN quantity <= minimum_stock THEN 0
                    ELSE 1
                END,
                id DESC
            """;

    private static final String INSERT_SQL = """
            INSERT INTO ingredients(
                name,
                unit,
                quantity,
                minimum_stock,
                import_price,
                status
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE ingredients
            SET
                name = ?,
                unit = ?,
                quantity = ?,
                minimum_stock = ?,
                import_price = ?,
                status = ?
            WHERE id = ?
            """;

    private static final String UPDATE_STATUS_SQL = """
            UPDATE ingredients
            SET status = ?
            WHERE id = ?
            """;

    private static final String NAME_EXISTS_SQL = """
            SELECT COUNT(*) AS total
            FROM ingredients
            WHERE LOWER(name) = LOWER(?)
              AND id <> ?
            """;

    public List<Ingredient> findAll() {
        List<Ingredient> ingredients = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        FIND_ALL_SQL);

                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                ingredients.add(
                        mapIngredient(resultSet));
            }

            return ingredients;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải danh sách nguyên liệu",
                    exception);
        }
    }

    public List<Ingredient> findAllActive() {
        List<Ingredient> ingredients = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        FIND_ALL_ACTIVE_SQL);

                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                ingredients.add(
                        mapIngredient(resultSet));
            }

            return ingredients;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải nguyên liệu đang hoạt động",
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
            throw databaseException(
                    "Không thể tải nguyên liệu",
                    exception);
        }
    }

    public List<Ingredient> search(
            String keyword,
            IngredientStatus status,
            boolean lowStockOnly) {
        List<Ingredient> ingredients = new ArrayList<>();

        String normalizedKeyword = keyword == null
                ? ""
                : keyword.trim();

        String likeKeyword = "%" + normalizedKeyword + "%";

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        SEARCH_SQL)) {
            statement.setString(
                    1,
                    normalizedKeyword);

            statement.setString(
                    2,
                    likeKeyword);

            statement.setString(
                    3,
                    likeKeyword);

            if (status == null) {
                statement.setNull(
                        4,
                        Types.VARCHAR);

                statement.setNull(
                        5,
                        Types.VARCHAR);

            } else {
                statement.setString(
                        4,
                        status.name());

                statement.setString(
                        5,
                        status.name());
            }

            statement.setBoolean(
                    6,
                    lowStockOnly);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    ingredients.add(
                            mapIngredient(resultSet));
                }
            }

            return ingredients;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tìm kiếm nguyên liệu",
                    exception);
        }
    }

    public int insert(
            Ingredient ingredient) {
        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        INSERT_SQL,
                        Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(
                    1,
                    ingredient.getName());

            statement.setString(
                    2,
                    ingredient.getUnit().name());

            statement.setBigDecimal(
                    3,
                    ingredient.getQuantity());

            statement.setBigDecimal(
                    4,
                    ingredient.getMinimumStock());

            statement.setBigDecimal(
                    5,
                    ingredient.getImportPrice());

            statement.setString(
                    6,
                    ingredient.getStatus().name());

            if (statement.executeUpdate() == 0) {
                throw new SQLException(
                        "Không thể thêm nguyên liệu.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

            throw new SQLException(
                    "Không lấy được ID nguyên liệu.");

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể thêm nguyên liệu",
                    exception);
        }
    }

    public void update(
            Ingredient ingredient) {
        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        UPDATE_SQL)) {
            statement.setString(
                    1,
                    ingredient.getName());

            statement.setString(
                    2,
                    ingredient.getUnit().name());

            statement.setBigDecimal(
                    3,
                    ingredient.getQuantity());

            statement.setBigDecimal(
                    4,
                    ingredient.getMinimumStock());

            statement.setBigDecimal(
                    5,
                    ingredient.getImportPrice());

            statement.setString(
                    6,
                    ingredient.getStatus().name());

            statement.setInt(
                    7,
                    ingredient.getId());

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Không tìm thấy nguyên liệu.");
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể cập nhật nguyên liệu",
                    exception);
        }
    }

    public void updateStatus(
            int ingredientId,
            IngredientStatus status) {
        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        UPDATE_STATUS_SQL)) {
            statement.setString(
                    1,
                    status.name());

            statement.setInt(
                    2,
                    ingredientId);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Không tìm thấy nguyên liệu.");
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể cập nhật trạng thái nguyên liệu",
                    exception);
        }
    }

    public boolean nameExists(
            String name,
            int excludedId) {
        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        NAME_EXISTS_SQL)) {
            statement.setString(
                    1,
                    name);

            statement.setInt(
                    2,
                    excludedId);

            try (ResultSet resultSet = statement.executeQuery()) {

                return resultSet.next()
                        && resultSet.getInt("total") > 0;
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể kiểm tra tên nguyên liệu",
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
                IngredientUnit.valueOf(
                        resultSet.getString("unit")));

        ingredient.setQuantity(
                resultSet.getBigDecimal("quantity"));

        ingredient.setMinimumStock(
                resultSet.getBigDecimal(
                        "minimum_stock"));

        ingredient.setImportPrice(
                resultSet.getBigDecimal(
                        "import_price"));

        ingredient.setStatus(
                IngredientStatus.valueOf(
                        resultSet.getString("status")));

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

    private IllegalStateException databaseException(
            String message,
            SQLException exception) {
        return new IllegalStateException(
                message + ": " + exception.getMessage(),
                exception);
    }
}