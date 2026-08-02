package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.dto.BranchEmployee;
import com.csms.entity.Branch;
import com.csms.entity.BranchStatus;
import com.csms.entity.RoleName;
import com.csms.entity.UserStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BranchDAO {

    private static final String BASE_SELECT = """
            SELECT
                b.id,
                b.name,
                b.address,
                b.phone,
                b.opening_time,
                b.closing_time,
                b.status,
                b.created_at,
                b.updated_at,

                COUNT(u.id) AS employee_count,

                COUNT(
                    CASE
                        WHEN u.status = 'ACTIVE'
                        THEN 1
                    END
                ) AS active_employee_count

            FROM branches b

            LEFT JOIN users u
                ON u.branch_id = b.id
            """;

    private static final String GROUP_BY = """
            GROUP BY
                b.id,
                b.name,
                b.address,
                b.phone,
                b.opening_time,
                b.closing_time,
                b.status,
                b.created_at,
                b.updated_at
            """;

    private static final String FIND_ALL_SQL = BASE_SELECT
            + GROUP_BY
            + """
                    ORDER BY b.id DESC
                    """;

    private static final String FIND_ALL_ACTIVE_SQL = """
            SELECT
                id,
                name,
                address,
                phone,
                opening_time,
                closing_time,
                status,
                created_at,
                updated_at
            FROM branches
            WHERE status = 'ACTIVE'
            ORDER BY name
            """;

    private static final String FIND_BY_ID_SQL = BASE_SELECT
            + """
                    WHERE b.id = ?
                    """
            + GROUP_BY;

    private static final String SEARCH_SQL = BASE_SELECT
            + """
                    WHERE (
                            ? = ''
                            OR LOWER(b.name) LIKE LOWER(?)
                            OR LOWER(b.address) LIKE LOWER(?)
                            OR LOWER(
                                COALESCE(b.phone, '')
                            ) LIKE LOWER(?)
                          )
                      AND (
                            ? IS NULL
                            OR b.status = ?
                          )
                    """
            + GROUP_BY
            + """
                    ORDER BY b.id DESC
                    """;

    private static final String INSERT_SQL = """
            INSERT INTO branches(
                name,
                address,
                phone,
                opening_time,
                closing_time,
                status
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE branches
            SET
                name = ?,
                address = ?,
                phone = ?,
                opening_time = ?,
                closing_time = ?,
                status = ?
            WHERE id = ?
            """;

    private static final String UPDATE_STATUS_SQL = """
            UPDATE branches
            SET status = ?
            WHERE id = ?
            """;

    private static final String DELETE_SQL = """
            DELETE FROM branches
            WHERE id = ?
            """;

    private static final String COUNT_EMPLOYEES_SQL = """
            SELECT COUNT(*) AS total
            FROM users
            WHERE branch_id = ?
            """;

    private static final String NAME_EXISTS_SQL = """
            SELECT COUNT(*) AS total
            FROM branches
            WHERE LOWER(name) = LOWER(?)
              AND id <> ?
            """;

    private static final String FIND_EMPLOYEES_SQL = """
            SELECT
                u.id,
                u.username,
                u.full_name,
                r.name AS role_name,
                u.status
            FROM users u
            JOIN roles r
                ON r.id = u.role_id
            WHERE u.branch_id = ?
            ORDER BY
                r.name,
                u.full_name
            """;

    public List<Branch> findAll() {
        List<Branch> branches = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection
                        .getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        FIND_ALL_SQL);

                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                branches.add(
                        mapBranch(resultSet));
            }

            return branches;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải danh sách chi nhánh",
                    exception);
        }
    }

    public List<Branch> findAllActive() {
        List<Branch> branches = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection
                        .getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        FIND_ALL_ACTIVE_SQL);

                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                branches.add(
                        mapBasicBranch(resultSet));
            }

            return branches;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải chi nhánh đang hoạt động",
                    exception);
        }
    }

    public Optional<Branch> findById(
            int branchId) {
        try (
                Connection connection = DatabaseConnection
                        .getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        FIND_BY_ID_SQL)) {
            statement.setInt(
                    1,
                    branchId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        mapBranch(resultSet));
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải chi nhánh",
                    exception);
        }
    }

    public List<Branch> search(
            String keyword,
            BranchStatus status) {
        List<Branch> branches = new ArrayList<>();

        String normalizedKeyword = keyword == null
                ? ""
                : keyword.trim();

        String likeKeyword = "%" + normalizedKeyword + "%";

        try (
                Connection connection = DatabaseConnection
                        .getConnection();

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

            statement.setString(
                    4,
                    likeKeyword);

            if (status == null) {
                statement.setNull(
                        5,
                        Types.VARCHAR);

                statement.setNull(
                        6,
                        Types.VARCHAR);
            } else {
                statement.setString(
                        5,
                        status.name());

                statement.setString(
                        6,
                        status.name());
            }

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    branches.add(
                            mapBranch(resultSet));
                }
            }

            return branches;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tìm kiếm chi nhánh",
                    exception);
        }
    }

    public int insert(
            Branch branch) {
        try (
                Connection connection = DatabaseConnection
                        .getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        INSERT_SQL,
                        Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(
                    1,
                    branch.getName());

            statement.setString(
                    2,
                    branch.getAddress());

            setNullableString(
                    statement,
                    3,
                    branch.getPhone());

            statement.setTime(
                    4,
                    Time.valueOf(
                            branch.getOpeningTime()));

            statement.setTime(
                    5,
                    Time.valueOf(
                            branch.getClosingTime()));

            statement.setString(
                    6,
                    branch.getStatus().name());

            if (statement.executeUpdate() == 0) {
                throw new SQLException(
                        "Không thể thêm chi nhánh.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

            throw new SQLException(
                    "Không lấy được ID chi nhánh.");

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể thêm chi nhánh",
                    exception);
        }
    }

    public void update(
            Branch branch) {
        try (
                Connection connection = DatabaseConnection
                        .getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        UPDATE_SQL)) {
            statement.setString(
                    1,
                    branch.getName());

            statement.setString(
                    2,
                    branch.getAddress());

            setNullableString(
                    statement,
                    3,
                    branch.getPhone());

            statement.setTime(
                    4,
                    Time.valueOf(
                            branch.getOpeningTime()));

            statement.setTime(
                    5,
                    Time.valueOf(
                            branch.getClosingTime()));

            statement.setString(
                    6,
                    branch.getStatus().name());

            statement.setInt(
                    7,
                    branch.getId());

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Không tìm thấy chi nhánh.");
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể cập nhật chi nhánh",
                    exception);
        }
    }

    public void updateStatus(
            int branchId,
            BranchStatus status) {
        try (
                Connection connection = DatabaseConnection
                        .getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        UPDATE_STATUS_SQL)) {
            statement.setString(
                    1,
                    status.name());

            statement.setInt(
                    2,
                    branchId);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Không tìm thấy chi nhánh.");
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể cập nhật trạng thái chi nhánh",
                    exception);
        }
    }

    public void delete(
            int branchId) {
        try (
                Connection connection = DatabaseConnection
                        .getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        DELETE_SQL)) {
            statement.setInt(
                    1,
                    branchId);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Không tìm thấy chi nhánh.");
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể xóa chi nhánh",
                    exception);
        }
    }

    public int countEmployees(
            int branchId) {
        try (
                Connection connection = DatabaseConnection
                        .getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        COUNT_EMPLOYEES_SQL)) {
            statement.setInt(
                    1,
                    branchId);

            try (ResultSet resultSet = statement.executeQuery()) {

                return resultSet.next()
                        ? resultSet.getInt("total")
                        : 0;
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể đếm nhân viên chi nhánh",
                    exception);
        }
    }

    public List<BranchEmployee> findEmployees(
            int branchId) {
        List<BranchEmployee> employees = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection
                        .getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        FIND_EMPLOYEES_SQL)) {
            statement.setInt(
                    1,
                    branchId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    employees.add(
                            new BranchEmployee(
                                    resultSet.getInt("id"),

                                    resultSet.getString(
                                            "username"),

                                    resultSet.getString(
                                            "full_name"),

                                    RoleName.valueOf(
                                            resultSet.getString(
                                                    "role_name")),

                                    UserStatus.valueOf(
                                            resultSet.getString(
                                                    "status"))));
                }
            }

            return employees;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải nhân viên chi nhánh",
                    exception);
        }
    }

    public boolean nameExists(
            String name,
            int excludedBranchId) {
        try (
                Connection connection = DatabaseConnection
                        .getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        NAME_EXISTS_SQL)) {
            statement.setString(
                    1,
                    name);

            statement.setInt(
                    2,
                    excludedBranchId);

            try (ResultSet resultSet = statement.executeQuery()) {

                return resultSet.next()
                        && resultSet.getInt("total") > 0;
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể kiểm tra tên chi nhánh",
                    exception);
        }
    }

    private Branch mapBranch(
            ResultSet resultSet) throws SQLException {

        Branch branch = mapBasicBranch(resultSet);

        branch.setEmployeeCount(
                resultSet.getInt(
                        "employee_count"));

        branch.setActiveEmployeeCount(
                resultSet.getInt(
                        "active_employee_count"));

        return branch;
    }

    private Branch mapBasicBranch(
            ResultSet resultSet) throws SQLException {

        Branch branch = new Branch();

        branch.setId(
                resultSet.getInt("id"));

        branch.setName(
                resultSet.getString("name"));

        branch.setAddress(
                resultSet.getString("address"));

        branch.setPhone(
                resultSet.getString("phone"));

        Time openingTime = resultSet.getTime(
                "opening_time");

        if (openingTime != null) {
            branch.setOpeningTime(
                    openingTime.toLocalTime());
        }

        Time closingTime = resultSet.getTime(
                "closing_time");

        if (closingTime != null) {
            branch.setClosingTime(
                    closingTime.toLocalTime());
        }

        branch.setStatus(
                BranchStatus.valueOf(
                        resultSet.getString("status")));

        Timestamp createdAt = resultSet.getTimestamp(
                "created_at");

        if (createdAt != null) {
            branch.setCreatedAt(
                    createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = resultSet.getTimestamp(
                "updated_at");

        if (updatedAt != null) {
            branch.setUpdatedAt(
                    updatedAt.toLocalDateTime());
        }

        return branch;
    }

    private void setNullableString(
            PreparedStatement statement,
            int index,
            String value) throws SQLException {

        if (value == null
                || value.isBlank()) {

            statement.setNull(
                    index,
                    Types.VARCHAR);

        } else {
            statement.setString(
                    index,
                    value.trim());
        }
    }

    private IllegalStateException databaseException(
            String message,
            SQLException exception) {
        return new IllegalStateException(
                message + ": "
                        + exception.getMessage(),
                exception);
    }
}