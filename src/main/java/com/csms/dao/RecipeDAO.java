package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.entity.ProductRecipe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeDAO {

        private static final String FIND_BY_PRODUCT_SQL = """
                        SELECT
                            pr.id,
                            pr.product_id,
                            p.name AS product_name,
                            pr.ingredient_id,
                            i.name AS ingredient_name,
                            pr.quantity_required,
                            pr.unit
                        FROM product_recipes pr
                        JOIN products p
                            ON p.id = pr.product_id
                        JOIN ingredients i
                            ON i.id = pr.ingredient_id
                        WHERE pr.product_id = ?
                        ORDER BY i.name
                        """;

        private static final String FIND_BY_ID_SQL = """
                        SELECT
                            pr.id,
                            pr.product_id,
                            p.name AS product_name,
                            pr.ingredient_id,
                            i.name AS ingredient_name,
                            pr.quantity_required,
                            pr.unit
                        FROM product_recipes pr
                        JOIN products p
                            ON p.id = pr.product_id
                        JOIN ingredients i
                            ON i.id = pr.ingredient_id
                        WHERE pr.id = ?
                        """;

        private static final String EXISTS_BY_PRODUCT_ID_SQL = """
                        SELECT EXISTS (
                            SELECT 1
                            FROM product_recipes
                            WHERE product_id = ?
                        ) AS recipe_exists
                        """;

        private static final String INSERT_SQL = """
                        INSERT INTO product_recipes(
                            product_id,
                            ingredient_id,
                            quantity_required,
                            unit
                        )
                        VALUES (?, ?, ?, ?)
                        """;

        private static final String UPDATE_SQL = """
                        UPDATE product_recipes
                        SET ingredient_id = ?,
                            quantity_required = ?,
                            unit = ?
                        WHERE id = ?
                        """;

        private static final String DELETE_SQL = """
                        DELETE FROM product_recipes
                        WHERE id = ?
                        """;

        private static final String EXISTS_SQL = """
                        SELECT COUNT(*) AS total
                        FROM product_recipes
                        WHERE product_id = ?
                          AND ingredient_id = ?
                          AND id <> ?
                        """;

        public List<ProductRecipe> findByProductId(
                        int productId) {
                List<ProductRecipe> recipes = new ArrayList<>();

                try (
                                Connection connection = DatabaseConnection.getConnection();

                                PreparedStatement statement = connection.prepareStatement(
                                                FIND_BY_PRODUCT_SQL)) {
                        statement.setInt(1, productId);

                        try (ResultSet resultSet = statement.executeQuery()) {

                                while (resultSet.next()) {
                                        recipes.add(
                                                        mapRecipe(resultSet));
                                }
                        }

                        return recipes;

                } catch (SQLException exception) {
                        throw databaseException(
                                        "Không thể tải công thức",
                                        exception);
                }
        }

        public Optional<ProductRecipe> findById(
                        int recipeId) {
                try (
                                Connection connection = DatabaseConnection.getConnection();

                                PreparedStatement statement = connection.prepareStatement(
                                                FIND_BY_ID_SQL)) {
                        statement.setInt(1, recipeId);

                        try (ResultSet resultSet = statement.executeQuery()) {

                                if (!resultSet.next()) {
                                        return Optional.empty();
                                }

                                return Optional.of(
                                                mapRecipe(resultSet));
                        }

                } catch (SQLException exception) {
                        throw databaseException(
                                        "Không thể tìm công thức",
                                        exception);
                }
        }

        public int insert(
                        ProductRecipe recipe) {
                try (
                                Connection connection = DatabaseConnection.getConnection();

                                PreparedStatement statement = connection.prepareStatement(
                                                INSERT_SQL,
                                                Statement.RETURN_GENERATED_KEYS)) {
                        statement.setInt(
                                        1,
                                        recipe.getProductId());

                        statement.setInt(
                                        2,
                                        recipe.getIngredientId());

                        statement.setBigDecimal(
                                        3,
                                        recipe.getQuantityRequired());

                        statement.setString(
                                        4,
                                        recipe.getUnit());

                        if (statement.executeUpdate() == 0) {
                                throw new SQLException(
                                                "Không thể thêm nguyên liệu vào công thức.");
                        }

                        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                                if (generatedKeys.next()) {
                                        return generatedKeys.getInt(1);
                                }
                        }

                        throw new SQLException(
                                        "Không lấy được ID công thức.");

                } catch (SQLException exception) {
                        throw databaseException(
                                        "Không thể thêm công thức",
                                        exception);
                }
        }

        public void update(
                        ProductRecipe recipe) {
                try (
                                Connection connection = DatabaseConnection.getConnection();

                                PreparedStatement statement = connection.prepareStatement(
                                                UPDATE_SQL)) {
                        statement.setInt(
                                        1,
                                        recipe.getIngredientId());

                        statement.setBigDecimal(
                                        2,
                                        recipe.getQuantityRequired());

                        statement.setString(
                                        3,
                                        recipe.getUnit());

                        statement.setInt(
                                        4,
                                        recipe.getId());

                        if (statement.executeUpdate() == 0) {
                                throw new IllegalArgumentException(
                                                "Không tìm thấy công thức.");
                        }

                } catch (SQLException exception) {
                        throw databaseException(
                                        "Không thể cập nhật công thức",
                                        exception);
                }
        }

        public void delete(
                        int recipeId) {
                try (
                                Connection connection = DatabaseConnection.getConnection();

                                PreparedStatement statement = connection.prepareStatement(
                                                DELETE_SQL)) {
                        statement.setInt(1, recipeId);

                        if (statement.executeUpdate() == 0) {
                                throw new IllegalArgumentException(
                                                "Không tìm thấy công thức.");
                        }

                } catch (SQLException exception) {
                        throw databaseException(
                                        "Không thể xóa công thức",
                                        exception);
                }
        }

        public boolean exists(
                        int productId,
                        int ingredientId,
                        int excludedRecipeId) {
                try (
                                Connection connection = DatabaseConnection.getConnection();

                                PreparedStatement statement = connection.prepareStatement(
                                                EXISTS_SQL)) {
                        statement.setInt(1, productId);
                        statement.setInt(2, ingredientId);
                        statement.setInt(3, excludedRecipeId);

                        try (ResultSet resultSet = statement.executeQuery()) {

                                return resultSet.next()
                                                && resultSet.getInt("total") > 0;
                        }

                } catch (SQLException exception) {
                        throw databaseException(
                                        "Không thể kiểm tra công thức",
                                        exception);
                }
        }

        public boolean existsByProductId(
                        Connection connection,
                        int productId) throws SQLException {

                try (
                                PreparedStatement statement = connection.prepareStatement(
                                                EXISTS_BY_PRODUCT_ID_SQL)) {
                        statement.setInt(
                                        1,
                                        productId);

                        try (
                                        ResultSet resultSet = statement.executeQuery()) {
                                return resultSet.next()
                                                && resultSet.getBoolean(
                                                                "recipe_exists");
                        }
                }
        }

        private ProductRecipe mapRecipe(
                        ResultSet resultSet) throws SQLException {

                ProductRecipe recipe = new ProductRecipe();

                recipe.setId(
                                resultSet.getInt("id"));

                recipe.setProductId(
                                resultSet.getInt("product_id"));

                recipe.setProductName(
                                resultSet.getString("product_name"));

                recipe.setIngredientId(
                                resultSet.getInt("ingredient_id"));

                recipe.setIngredientName(
                                resultSet.getString("ingredient_name"));

                recipe.setQuantityRequired(
                                resultSet.getBigDecimal(
                                                "quantity_required"));

                recipe.setUnit(
                                resultSet.getString("unit"));

                return recipe;
        }

        private IllegalStateException databaseException(
                        String message,
                        SQLException exception) {
                return new IllegalStateException(
                                message + ": " + exception.getMessage(),
                                exception);
        }
}