package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.entity.RoleName;
import com.csms.entity.User;
import com.csms.entity.UserStatus;

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

public class UserDAO {

    private static final String BASE_SELECT = """
            SELECT
                u.id,
                u.username,
                u.password,
                u.full_name,
                u.email,
                u.phone,
                u.role_id,
                r.name AS role_name,
                u.status,
                u.created_at,
                u.updated_at
            FROM users u
            JOIN roles r ON r.id = u.role_id
            """;

    private static final String FIND_ALL_SQL = BASE_SELECT + """
            ORDER BY u.id DESC
            """;

    private static final String FIND_BY_ID_SQL = BASE_SELECT + """
            WHERE u.id = ?
            """;

    private static final String FIND_BY_USERNAME_SQL = BASE_SELECT + """
            WHERE LOWER(u.username) = LOWER(?)
            """;

    private static final String SEARCH_SQL = BASE_SELECT + """
            WHERE (
                    ? = ''
                    OR LOWER(u.username) LIKE LOWER(?)
                    OR LOWER(u.full_name) LIKE LOWER(?)
                    OR LOWER(COALESCE(u.email, '')) LIKE LOWER(?)
                    OR LOWER(COALESCE(u.phone, '')) LIKE LOWER(?)
                  )
              AND (
                    ? IS NULL
                    OR r.name = ?
                  )
              AND (
                    ? IS NULL
                    OR u.status = ?
                  )
            ORDER BY u.id DESC
            """;

    private static final String INSERT_SQL = """
            INSERT INTO users(
                username,
                password,
                full_name,
                email,
                phone,
                role_id,
                status
            )
            VALUES (
                ?,
                ?,
                ?,
                ?,
                ?,
                (
                    SELECT id
                    FROM roles
                    WHERE name = ?
                ),
                ?
            )
            """;

    private static final String UPDATE_SQL = """
            UPDATE users
            SET
                username = ?,
                full_name = ?,
                email = ?,
                phone = ?,
                role_id = (
                    SELECT id
                    FROM roles
                    WHERE name = ?
                ),
                status = ?
            WHERE id = ?
            """;

    private static final String UPDATE_STATUS_SQL = """
            UPDATE users
            SET status = ?
            WHERE id = ?
            """;

    private static final String RESET_PASSWORD_SQL = """
            UPDATE users
            SET password = ?
            WHERE id = ?
            """;

    private static final String USERNAME_EXISTS_SQL = """
            SELECT COUNT(*) AS total
            FROM users
            WHERE LOWER(username) = LOWER(?)
              AND id <> ?
            """;

    private static final String EMAIL_EXISTS_SQL = """
            SELECT COUNT(*) AS total
            FROM users
            WHERE email IS NOT NULL
              AND email <> ''
              AND LOWER(email) = LOWER(?)
              AND id <> ?
            """;

    public List<User> findAll() {
        List<User> users = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        FIND_ALL_SQL);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }

            return users;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải danh sách người dùng",
                    exception);
        }
    }

    public List<User> search(
            String keyword,
            RoleName roleName,
            UserStatus status) {
        List<User> users = new ArrayList<>();

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

            statement.setString(
                    4,
                    likeKeyword);

            statement.setString(
                    5,
                    likeKeyword);

            setNullableEnum(
                    statement,
                    6,
                    roleName);

            setNullableEnum(
                    statement,
                    7,
                    roleName);

            setNullableEnum(
                    statement,
                    8,
                    status);

            setNullableEnum(
                    statement,
                    9,
                    status);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
            }

            return users;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tìm kiếm người dùng",
                    exception);
        }
    }

    public Optional<User> findById(
            int userId) {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        FIND_BY_ID_SQL)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        mapUser(resultSet));
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải thông tin người dùng",
                    exception);
        }
    }

    public Optional<User> findByUsername(
            String username) {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        FIND_BY_USERNAME_SQL)) {
            statement.setString(
                    1,
                    username);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        mapUser(resultSet));
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tìm tài khoản",
                    exception);
        }
    }

    public int insert(User user) {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        INSERT_SQL,
                        Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(
                    1,
                    user.getUsername());

            statement.setString(
                    2,
                    user.getPasswordHash());

            statement.setString(
                    3,
                    user.getFullName());

            setNullableString(
                    statement,
                    4,
                    user.getEmail());

            setNullableString(
                    statement,
                    5,
                    user.getPhone());

            statement.setString(
                    6,
                    user.getRoleName().name());

            statement.setString(
                    7,
                    user.getStatus().name());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException(
                        "Không có tài khoản nào được tạo.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

            throw new SQLException(
                    "Không lấy được ID tài khoản.");

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tạo tài khoản",
                    exception);
        }
    }

    public void update(User user) {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        UPDATE_SQL)) {
            statement.setString(
                    1,
                    user.getUsername());

            statement.setString(
                    2,
                    user.getFullName());

            setNullableString(
                    statement,
                    3,
                    user.getEmail());

            setNullableString(
                    statement,
                    4,
                    user.getPhone());

            statement.setString(
                    5,
                    user.getRoleName().name());

            statement.setString(
                    6,
                    user.getStatus().name());

            statement.setInt(
                    7,
                    user.getId());

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Không tìm thấy tài khoản cần cập nhật.");
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể cập nhật tài khoản",
                    exception);
        }
    }

    public void updateStatus(
            int userId,
            UserStatus status) {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        UPDATE_STATUS_SQL)) {
            statement.setString(
                    1,
                    status.name());

            statement.setInt(
                    2,
                    userId);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Không tìm thấy tài khoản.");
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể cập nhật trạng thái",
                    exception);
        }
    }

    public void resetPassword(
            int userId,
            String passwordHash) {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        RESET_PASSWORD_SQL)) {
            statement.setString(
                    1,
                    passwordHash);

            statement.setInt(
                    2,
                    userId);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Không tìm thấy tài khoản.");
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể đặt lại mật khẩu",
                    exception);
        }
    }

    public boolean usernameExists(
            String username,
            int excludedUserId) {
        return exists(
                USERNAME_EXISTS_SQL,
                username,
                excludedUserId);
    }

    public boolean emailExists(
            String email,
            int excludedUserId) {
        if (email == null || email.isBlank()) {
            return false;
        }

        return exists(
                EMAIL_EXISTS_SQL,
                email,
                excludedUserId);
    }

    private boolean exists(
            String sql,
            String value,
            int excludedUserId) {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(
                    1,
                    value);

            statement.setInt(
                    2,
                    excludedUserId);

            try (ResultSet resultSet = statement.executeQuery()) {

                return resultSet.next()
                        && resultSet.getInt("total") > 0;
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể kiểm tra dữ liệu tài khoản",
                    exception);
        }
    }

    private User mapUser(
            ResultSet resultSet) throws SQLException {

        User user = new User();

        user.setId(
                resultSet.getInt("id"));

        user.setUsername(
                resultSet.getString("username"));

        user.setPasswordHash(
                resultSet.getString("password"));

        user.setFullName(
                resultSet.getString("full_name"));

        user.setEmail(
                resultSet.getString("email"));

        user.setPhone(
                resultSet.getString("phone"));

        user.setRoleId(
                resultSet.getInt("role_id"));

        user.setRoleName(
                RoleName.valueOf(
                        resultSet.getString("role_name")));

        user.setStatus(
                UserStatus.valueOf(
                        resultSet.getString("status")));

        Timestamp createdAt = resultSet.getTimestamp("created_at");

        if (createdAt != null) {
            user.setCreatedAt(
                    createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");

        if (updatedAt != null) {
            user.setUpdatedAt(
                    updatedAt.toLocalDateTime());
        }

        return user;
    }

    private void setNullableString(
            PreparedStatement statement,
            int index,
            String value) throws SQLException {

        if (value == null || value.isBlank()) {
            statement.setNull(
                    index,
                    Types.VARCHAR);
        } else {
            statement.setString(
                    index,
                    value.trim());
        }
    }

    private void setNullableEnum(
            PreparedStatement statement,
            int index,
            Enum<?> value) throws SQLException {

        if (value == null) {
            statement.setNull(
                    index,
                    Types.VARCHAR);
        } else {
            statement.setString(
                    index,
                    value.name());
        }
    }

    private IllegalStateException databaseException(
            String message,
            SQLException exception) {
        return new IllegalStateException(
                message + ": " + exception.getMessage(),
                exception);
    }
}