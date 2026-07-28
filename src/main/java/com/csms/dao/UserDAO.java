package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.entity.RoleName;
import com.csms.entity.User;
import com.csms.entity.UserStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDAO {

    private static final String FIND_BY_USERNAME_SQL = """
            SELECT
                u.id,
                u.username,
                u.password,
                u.full_name,
                u.status,
                r.name AS role_name
            FROM users u
            JOIN roles r ON r.id = u.role_id
            WHERE u.username = ?
            LIMIT 1
            """;

    public Optional<User> findByUsername(String username) {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_USERNAME_SQL)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setUsername(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                user.setFullName(resultSet.getString("full_name"));
                user.setRole(
                        RoleName.valueOf(
                                resultSet.getString("role_name")));
                user.setStatus(
                        UserStatus.valueOf(
                                resultSet.getString("status")));

                return Optional.of(user);
            }

        } catch (SQLException | IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Không thể tìm tài khoản: "
                            + exception.getMessage(),
                    exception);
        }
    }
}