package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.entity.Category;
import com.csms.entity.UserStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    private static final String FIND_ACTIVE_SQL = """
            SELECT id, name, description, status
            FROM categories
            WHERE status = 'ACTIVE'
            ORDER BY name
            """;

    public List<Category> findAllActive() {
        List<Category> categories = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_ACTIVE_SQL);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Category category = new Category();

                category.setId(resultSet.getInt("id"));
                category.setName(resultSet.getString("name"));
                category.setDescription(
                        resultSet.getString("description"));
                category.setStatus(
                        UserStatus.valueOf(
                                resultSet.getString("status")));

                categories.add(category);
            }

            return categories;

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể tải danh mục: "
                            + exception.getMessage(),
                    exception);
        }
    }
}