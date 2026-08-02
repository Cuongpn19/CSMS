package com.csms.dao;

import com.csms.config.DatabaseConnection;
import com.csms.entity.VatScopeType;
import com.csms.entity.VatSetting;

import java.math.BigDecimal;
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

public class VatSettingDAO {

    private static final String BASE_SELECT = """
            SELECT
                v.id,
                v.scope_type,
                v.category_id,
                c.name AS category_name,
                v.product_id,
                p.name AS product_name,
                v.vat_rate,
                v.enabled,
                v.effective_from,
                v.effective_to,
                v.created_by,
                u.full_name AS created_by_name,
                v.created_at,
                v.updated_at
            FROM vat_settings v
            LEFT JOIN categories c
                ON c.id = v.category_id
            LEFT JOIN products p
                ON p.id = v.product_id
            LEFT JOIN users u
                ON u.id = v.created_by
            """;

    private static final String FIND_ALL_SQL = BASE_SELECT + """
            ORDER BY
                v.enabled DESC,
                v.scope_type,
                v.id DESC
            """;

    private static final String FIND_BY_ID_SQL = BASE_SELECT + """
            WHERE v.id = ?
            """;

    private static final String INSERT_SQL = """
            INSERT INTO vat_settings(
                scope_type,
                category_id,
                product_id,
                vat_rate,
                enabled,
                effective_from,
                effective_to,
                created_by
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE vat_settings
            SET
                scope_type = ?,
                category_id = ?,
                product_id = ?,
                vat_rate = ?,
                enabled = ?,
                effective_from = ?,
                effective_to = ?
            WHERE id = ?
            """;

    private static final String UPDATE_ENABLED_SQL = """
            UPDATE vat_settings
            SET enabled = ?
            WHERE id = ?
            """;

    private static final String DELETE_SQL = """
            DELETE FROM vat_settings
            WHERE id = ?
            """;

    private static final String DUPLICATE_SCOPE_SQL = """
            SELECT COUNT(*) AS total
            FROM vat_settings
            WHERE scope_type = ?
              AND (
                    (? IS NULL AND category_id IS NULL)
                    OR category_id = ?
                  )
              AND (
                    (? IS NULL AND product_id IS NULL)
                    OR product_id = ?
                  )
              AND id <> ?
            """;

    /*
     * Thứ tự ưu tiên:
     * PRODUCT -> CATEGORY -> GLOBAL.
     */
    private static final String RESOLVE_PRODUCT_VAT_SQL = """
            SELECT vat_rate
            FROM (
                SELECT
                    v.vat_rate,
                    1 AS priority_level
                FROM vat_settings v
                WHERE v.scope_type = 'PRODUCT'
                  AND v.product_id = ?
                  AND v.enabled = TRUE
                  AND v.effective_from <= CURRENT_TIMESTAMP
                  AND (
                        v.effective_to IS NULL
                        OR v.effective_to >= CURRENT_TIMESTAMP
                      )

                UNION ALL

                SELECT
                    v.vat_rate,
                    2 AS priority_level
                FROM vat_settings v
                JOIN products p
                    ON p.category_id = v.category_id
                WHERE v.scope_type = 'CATEGORY'
                  AND p.id = ?
                  AND v.enabled = TRUE
                  AND v.effective_from <= CURRENT_TIMESTAMP
                  AND (
                        v.effective_to IS NULL
                        OR v.effective_to >= CURRENT_TIMESTAMP
                      )

                UNION ALL

                SELECT
                    v.vat_rate,
                    3 AS priority_level
                FROM vat_settings v
                WHERE v.scope_type = 'GLOBAL'
                  AND v.enabled = TRUE
                  AND v.effective_from <= CURRENT_TIMESTAMP
                  AND (
                        v.effective_to IS NULL
                        OR v.effective_to >= CURRENT_TIMESTAMP
                      )
            ) resolved_vat
            ORDER BY priority_level
            LIMIT 1
            """;

    public List<VatSetting> findAll() {
        List<VatSetting> settings = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        FIND_ALL_SQL);

                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                settings.add(
                        mapVatSetting(resultSet));
            }

            return settings;

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải cấu hình VAT",
                    exception);
        }
    }

    public Optional<VatSetting> findById(
            int id) {
        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        FIND_BY_ID_SQL)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        mapVatSetting(resultSet));
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể tải cấu hình VAT",
                    exception);
        }
    }

    public int insert(VatSetting setting) {
        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        INSERT_SQL,
                        Statement.RETURN_GENERATED_KEYS)) {
            bindFormFields(
                    statement,
                    setting);

            setNullableInteger(
                    statement,
                    8,
                    setting.getCreatedBy());

            if (statement.executeUpdate() == 0) {
                throw new SQLException(
                        "Không thể thêm cấu hình VAT.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

            throw new SQLException(
                    "Không lấy được ID cấu hình VAT.");

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể thêm cấu hình VAT",
                    exception);
        }
    }

    public void update(VatSetting setting) {
        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        UPDATE_SQL)) {
            bindFormFields(
                    statement,
                    setting);

            statement.setInt(
                    8,
                    setting.getId());

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Không tìm thấy cấu hình VAT.");
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể cập nhật cấu hình VAT",
                    exception);
        }
    }

    public void updateEnabled(
            int id,
            boolean enabled) {
        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        UPDATE_ENABLED_SQL)) {
            statement.setBoolean(
                    1,
                    enabled);

            statement.setInt(
                    2,
                    id);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Không tìm thấy cấu hình VAT.");
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể đổi trạng thái VAT",
                    exception);
        }
    }

    public void delete(int id) {
        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        DELETE_SQL)) {
            statement.setInt(1, id);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Không tìm thấy cấu hình VAT.");
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể xóa cấu hình VAT",
                    exception);
        }
    }

    public boolean duplicateScopeExists(
            VatScopeType scopeType,
            Integer categoryId,
            Integer productId,
            int excludedId) {
        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        DUPLICATE_SCOPE_SQL)) {
            statement.setString(
                    1,
                    scopeType.name());

            setNullableInteger(
                    statement,
                    2,
                    categoryId);

            setNullableInteger(
                    statement,
                    3,
                    categoryId);

            setNullableInteger(
                    statement,
                    4,
                    productId);

            setNullableInteger(
                    statement,
                    5,
                    productId);

            statement.setInt(
                    6,
                    excludedId);

            try (ResultSet resultSet = statement.executeQuery()) {

                return resultSet.next()
                        && resultSet.getInt("total") > 0;
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể kiểm tra cấu hình VAT",
                    exception);
        }
    }

    public BigDecimal resolveVatRateForProduct(
            int productId) {
        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(
                        RESOLVE_PRODUCT_VAT_SQL)) {
            statement.setInt(
                    1,
                    productId);

            statement.setInt(
                    2,
                    productId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return BigDecimal.ZERO;
                }

                BigDecimal vatRate = resultSet.getBigDecimal(
                        "vat_rate");

                return vatRate == null
                        ? BigDecimal.ZERO
                        : vatRate;
            }

        } catch (SQLException exception) {
            throw databaseException(
                    "Không thể xác định mức VAT",
                    exception);
        }
    }

    private void bindFormFields(
            PreparedStatement statement,
            VatSetting setting) throws SQLException {

        statement.setString(
                1,
                setting.getScopeType().name());

        setNullableInteger(
                statement,
                2,
                setting.getCategoryId());

        setNullableInteger(
                statement,
                3,
                setting.getProductId());

        statement.setBigDecimal(
                4,
                setting.getVatRate());

        statement.setBoolean(
                5,
                setting.isEnabled());

        statement.setTimestamp(
                6,
                Timestamp.valueOf(
                        setting.getEffectiveFrom()));

        if (setting.getEffectiveTo() == null) {
            statement.setNull(
                    7,
                    Types.TIMESTAMP);
        } else {
            statement.setTimestamp(
                    7,
                    Timestamp.valueOf(
                            setting.getEffectiveTo()));
        }
    }

    private VatSetting mapVatSetting(
            ResultSet resultSet) throws SQLException {

        VatSetting setting = new VatSetting();

        setting.setId(
                resultSet.getInt("id"));

        setting.setScopeType(
                VatScopeType.valueOf(
                        resultSet.getString(
                                "scope_type")));

        int categoryId = resultSet.getInt("category_id");

        setting.setCategoryId(
                resultSet.wasNull()
                        ? null
                        : categoryId);

        setting.setCategoryName(
                resultSet.getString(
                        "category_name"));

        int productId = resultSet.getInt("product_id");

        setting.setProductId(
                resultSet.wasNull()
                        ? null
                        : productId);

        setting.setProductName(
                resultSet.getString(
                        "product_name"));

        setting.setVatRate(
                resultSet.getBigDecimal(
                        "vat_rate"));

        setting.setEnabled(
                resultSet.getBoolean(
                        "enabled"));

        Timestamp effectiveFrom = resultSet.getTimestamp(
                "effective_from");

        if (effectiveFrom != null) {
            setting.setEffectiveFrom(
                    effectiveFrom.toLocalDateTime());
        }

        Timestamp effectiveTo = resultSet.getTimestamp(
                "effective_to");

        if (effectiveTo != null) {
            setting.setEffectiveTo(
                    effectiveTo.toLocalDateTime());
        }

        int createdBy = resultSet.getInt("created_by");

        setting.setCreatedBy(
                resultSet.wasNull()
                        ? null
                        : createdBy);

        setting.setCreatedByName(
                resultSet.getString(
                        "created_by_name"));

        Timestamp createdAt = resultSet.getTimestamp(
                "created_at");

        if (createdAt != null) {
            setting.setCreatedAt(
                    createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = resultSet.getTimestamp(
                "updated_at");

        if (updatedAt != null) {
            setting.setUpdatedAt(
                    updatedAt.toLocalDateTime());
        }

        return setting;
    }

    private void setNullableInteger(
            PreparedStatement statement,
            int index,
            Integer value) throws SQLException {

        if (value == null) {
            statement.setNull(
                    index,
                    Types.INTEGER);
        } else {
            statement.setInt(
                    index,
                    value);
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