package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.entity.Branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BranchDAO {

    private static final String FIND_ALL_ACTIVE_SQL = """
            SELECT
                id,
                name,
                address
            FROM branches
            WHERE status = 'ACTIVE'
            ORDER BY name
            """;

    public List<Branch> findAllActive() {
        List<Branch> branches = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        FIND_ALL_ACTIVE_SQL);

                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Branch branch = new Branch();

                branch.setId(
                        resultSet.getInt("id"));

                branch.setName(
                        resultSet.getString("name"));

                branch.setAddress(
                        resultSet.getString("address"));

                branches.add(branch);
            }

            return branches;

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể tải danh sách chi nhánh: "
                            + exception.getMessage(),
                    exception);
        }
    }
}