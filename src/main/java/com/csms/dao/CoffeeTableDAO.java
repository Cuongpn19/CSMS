package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.entity.CoffeeTable;
import com.csms.entity.TableStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CoffeeTableDAO {

    private static final String FIND_ALL_SQL = """
            SELECT id, name, capacity, status
            FROM coffee_tables
            WHERE status <> 'INACTIVE'
            ORDER BY id
            """;

    private static final String FIND_AVAILABLE_SQL = """
            SELECT id, name, capacity, status
            FROM coffee_tables
            WHERE status = 'AVAILABLE'
            ORDER BY id
            """;

    private static final String UPDATE_STATUS_SQL = """
            UPDATE coffee_tables
            SET status = ?
            WHERE id = ?
            """;

    public List<CoffeeTable> findAll() {
        return executeFind(FIND_ALL_SQL);
    }

    public List<CoffeeTable> findAvailable() {
        return executeFind(FIND_AVAILABLE_SQL);
    }

    public boolean updateStatus(
            int tableId,
            TableStatus status) {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        UPDATE_STATUS_SQL)) {
            statement.setString(1, status.name());
            statement.setInt(2, tableId);

            return statement.executeUpdate() > 0;

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể cập nhật trạng thái bàn: "
                            + exception.getMessage(),
                    exception);
        }
    }

    private List<CoffeeTable> executeFind(String sql) {
        List<CoffeeTable> tables = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                CoffeeTable table = new CoffeeTable();

                table.setId(resultSet.getInt("id"));
                table.setName(resultSet.getString("name"));
                table.setCapacity(
                        resultSet.getInt("capacity"));
                table.setStatus(
                        TableStatus.valueOf(
                                resultSet.getString("status")));

                tables.add(table);
            }

            return tables;

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể tải danh sách bàn: "
                            + exception.getMessage(),
                    exception);
        }
    }
}