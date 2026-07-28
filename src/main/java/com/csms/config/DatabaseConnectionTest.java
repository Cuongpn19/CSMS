package com.csms.config;

import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseConnectionTest {

    private DatabaseConnectionTest() {
    }

    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection != null && !connection.isClosed()) {
                System.out.println(
                        "Kết nối MySQL thành công!");
            }

        } catch (SQLException exception) {
            System.err.println(
                    "Kết nối MySQL thất bại: "
                            + exception.getMessage());
        }
    }
}