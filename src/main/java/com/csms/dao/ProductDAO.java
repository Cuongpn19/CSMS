package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.entity.Product;
import com.csms.entity.ProductStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAO {

    private static final String FIND_ALL_SQL = """
            SELECT
                p.id,
                p.category_id,
                c.name AS category_name,
                p.name,
                p.price,
                p.quantity,
                p.image,
                p.description,
                p.status
            FROM products p
            JOIN categories c ON c.id = p.category_id
            ORDER BY p.id DESC
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT
                p.id,
                p.category_id,
                c.name AS category_name,
                p.name,
                p.price,
                p.quantity,
                p.image,
                p.description,
                p.status
            FROM products p
            JOIN categories c ON c.id = p.category_id
            WHERE p.id = ?
            """;

    private static final String SEARCH_SQL = """
            SELECT
                p.id,
                p.category_id,
                c.name AS category_name,
                p.name,
                p.price,
                p.quantity,
                p.image,
                p.description,
                p.status
            FROM products p
            JOIN categories c ON c.id = p.category_id
            WHERE LOWER(p.name) LIKE LOWER(?)
               OR LOWER(c.name) LIKE LOWER(?)
            ORDER BY p.id DESC
            """;

    private static final String INSERT_SQL = """
            INSERT INTO products(
                category_id,
                name,
                price,
                quantity,
                image,
                description,
                status
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE products
            SET category_id = ?,
                name = ?,
                price = ?,
                quantity = ?,
                image = ?,
                description = ?,
                status = ?
            WHERE id = ?
            """;

    private static final String DELETE_SQL = """
            DELETE FROM products
            WHERE id = ?
            """;

    private static final String FIND_AVAILABLE_SQL = """
            SELECT
                p.id,
                p.category_id,
                c.name AS category_name,
                p.name,
                p.price,
                p.quantity,
                p.image,
                p.description,
                p.status
            FROM products p
            JOIN categories c ON c.id = p.category_id
            WHERE p.status = 'AVAILABLE'
              AND p.quantity > 0
            ORDER BY c.name, p.name
            """;

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                products.add(mapProduct(resultSet));
            }

            return products;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải sản phẩm",
                    exception);
        }
    }

    public Optional<Product> findById(int id) {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(
                            mapProduct(resultSet));
                }

                return Optional.empty();
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tìm sản phẩm",
                    exception);
        }
    }

    public List<Product> search(String keyword) {
        String searchValue = "%"
                + (keyword == null ? "" : keyword.trim())
                + "%";

        List<Product> products = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SEARCH_SQL)) {
            statement.setString(1, searchValue);
            statement.setString(2, searchValue);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(mapProduct(resultSet));
                }
            }

            return products;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tìm kiếm sản phẩm",
                    exception);
        }
    }

    public List<Product> findAvailable() {
        List<Product> products = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        FIND_AVAILABLE_SQL);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                products.add(mapProduct(resultSet));
            }

            return products;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải sản phẩm đang bán",
                    exception);
        }
    }

    public int insert(Product product) {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        INSERT_SQL,
                        Statement.RETURN_GENERATED_KEYS)) {
            setProductParameters(statement, product);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new IllegalStateException(
                        "Không thể thêm sản phẩm.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

            throw new IllegalStateException(
                    "Không lấy được ID sản phẩm vừa thêm.");

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể thêm sản phẩm",
                    exception);
        }
    }

    public boolean update(Product product) {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            setProductParameters(statement, product);
            statement.setInt(8, product.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể cập nhật sản phẩm",
                    exception);
        }
    }

    public boolean delete(int id) {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể xóa sản phẩm",
                    exception);
        }
    }

    private void setProductParameters(
            PreparedStatement statement,
            Product product) throws SQLException {

        statement.setInt(1, product.getCategoryId());
        statement.setString(2, product.getName());
        statement.setBigDecimal(3, product.getPrice());
        statement.setInt(4, product.getQuantity());
        statement.setString(5, product.getImage());
        statement.setString(6, product.getDescription());
        statement.setString(
                7,
                product.getStatus().name());
    }

    private Product mapProduct(
            ResultSet resultSet) throws SQLException {

        Product product = new Product();

        product.setId(resultSet.getInt("id"));
        product.setCategoryId(
                resultSet.getInt("category_id"));
        product.setCategoryName(
                resultSet.getString("category_name"));
        product.setName(resultSet.getString("name"));
        product.setPrice(resultSet.getBigDecimal("price"));
        product.setQuantity(resultSet.getInt("quantity"));
        product.setImage(resultSet.getString("image"));
        product.setDescription(
                resultSet.getString("description"));
        product.setStatus(
                ProductStatus.valueOf(
                        resultSet.getString("status")));

        return product;
    }

    private IllegalStateException databaseException(
            String message,
            SQLException exception) {
        return new IllegalStateException(
                message + ": " + exception.getMessage(),
                exception);
    }
}