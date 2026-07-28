package com.csms.utils;

import com.csms.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class AdminSeeder {

    private AdminSeeder() {
    }

    public static void main(String[] args) {
        String checkSql = """
                SELECT id
                FROM users
                WHERE username = ?
                """;

        String insertSql = """
                INSERT INTO users(
                    username,
                    password,
                    full_name,
                    role_id,
                    status
                )
                SELECT ?, ?, ?, id, 'ACTIVE'
                FROM roles
                WHERE name = 'ADMIN'
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (userExists(connection, checkSql, "admin")) {
                System.out.println(
                        "Tài khoản admin đã tồn tại.");
                return;
            }

            String hashedPassword = PasswordUtils.hashPassword("admin123");

            try (PreparedStatement statement = connection.prepareStatement(insertSql)) {

                statement.setString(1, "admin");
                statement.setString(2, hashedPassword);
                statement.setString(3, "Quản trị viên");

                int rows = statement.executeUpdate();

                if (rows > 0) {
                    System.out.println(
                            "Tạo tài khoản admin thành công.");
                    System.out.println("Username: admin");
                    System.out.println("Password: admin123");
                } else {
                    System.err.println(
                            "Không tìm thấy role ADMIN.");
                }
            }

        } catch (SQLException exception) {
            System.err.println(
                    "Không thể tạo tài khoản admin: "
                            + exception.getMessage());
        }
    }

    private static boolean userExists(
            Connection connection,
            String sql,
            String username) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {

                return resultSet.next();
            }
        }
    }
}