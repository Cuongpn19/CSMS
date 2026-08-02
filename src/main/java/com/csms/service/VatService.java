package com.csms.service;

import com.csms.dao.VatSettingDAO;
import com.csms.dto.VatSettingFormData;
import com.csms.entity.VatScopeType;
import com.csms.entity.VatSetting;
import com.csms.utils.SessionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class VatService {

    private final VatSettingDAO vatSettingDAO;

    public VatService() {
        vatSettingDAO = new VatSettingDAO();
    }

    public List<VatSetting> findAll() {
        return vatSettingDAO.findAll();
    }

    public VatSetting create(
            VatSettingFormData formData) {
        validate(formData, 0);

        VatSetting setting = new VatSetting();

        mapFormData(
                setting,
                formData);

        if (SessionManager.getCurrentUser() != null) {

            setting.setCreatedBy(
                    SessionManager
                            .getCurrentUser()
                            .getId());
        }

        int id = vatSettingDAO.insert(
                setting);

        setting.setId(id);

        return setting;
    }

    public void update(
            int settingId,
            VatSettingFormData formData) {
        VatSetting setting = vatSettingDAO
                .findById(settingId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy cấu hình VAT."));

        validate(
                formData,
                settingId);

        mapFormData(
                setting,
                formData);

        vatSettingDAO.update(setting);
    }

    public void toggleEnabled(int settingId) {
        VatSetting setting = vatSettingDAO
                .findById(settingId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy cấu hình VAT."));

        vatSettingDAO.updateEnabled(
                settingId,
                !setting.isEnabled());
    }

    public void delete(int settingId) {
        VatSetting setting = vatSettingDAO
                .findById(settingId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy cấu hình VAT."));

        if (setting.getScopeType() == VatScopeType.GLOBAL
                && setting.isEnabled()) {

            throw new IllegalStateException(
                    "Không nên xóa mức VAT chung đang hoạt động. "
                            + "Hãy vô hiệu hóa hoặc cập nhật mức thuế.");
        }

        vatSettingDAO.delete(settingId);
    }

    public BigDecimal resolveRateForProduct(
            int productId) {
        if (productId <= 0) {
            return BigDecimal.ZERO;
        }

        return vatSettingDAO
                .resolveVatRateForProduct(
                        productId);
    }

    public BigDecimal calculateVatAmount(
            int productId,
            BigDecimal taxableAmount) {
        if (taxableAmount == null
                || taxableAmount.compareTo(
                        BigDecimal.ZERO) <= 0) {

            return BigDecimal.ZERO;
        }

        BigDecimal rate = resolveRateForProduct(
                productId);

        return taxableAmount
                .multiply(rate)
                .divide(
                        BigDecimal.valueOf(100),
                        2,
                        RoundingMode.HALF_UP);
    }

    private void validate(
            VatSettingFormData formData,
            int excludedId) {
        if (formData == null) {
            throw new IllegalArgumentException(
                    "Thông tin VAT không hợp lệ.");
        }

        if (formData.scopeType() == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn phạm vi áp dụng.");
        }

        if (formData.vatRate() == null
                || formData.vatRate()
                        .compareTo(BigDecimal.ZERO) < 0
                || formData.vatRate()
                        .compareTo(
                                BigDecimal.valueOf(100)) > 0) {

            throw new IllegalArgumentException(
                    "Mức VAT phải từ 0 đến 100%.");
        }

        switch (formData.scopeType()) {
            case GLOBAL -> {
                if (formData.categoryId() != null
                        || formData.productId() != null) {

                    throw new IllegalArgumentException(
                            "VAT toàn hệ thống không được chọn danh mục hoặc món.");
                }
            }

            case CATEGORY -> {
                if (formData.categoryId() == null) {
                    throw new IllegalArgumentException(
                            "Vui lòng chọn danh mục.");
                }

                if (formData.productId() != null) {
                    throw new IllegalArgumentException(
                            "VAT theo danh mục không được chọn món.");
                }
            }

            case PRODUCT -> {
                if (formData.productId() == null) {
                    throw new IllegalArgumentException(
                            "Vui lòng chọn món.");
                }

                if (formData.categoryId() != null) {
                    throw new IllegalArgumentException(
                            "VAT theo món không được chọn danh mục.");
                }
            }
        }

        LocalDateTime effectiveFrom = formData.effectiveFrom();

        if (effectiveFrom == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn thời điểm áp dụng.");
        }

        if (formData.effectiveTo() != null
                && !formData.effectiveTo()
                        .isAfter(effectiveFrom)) {

            throw new IllegalArgumentException(
                    "Thời điểm kết thúc phải sau thời điểm bắt đầu.");
        }

        if (vatSettingDAO.duplicateScopeExists(
                formData.scopeType(),
                formData.categoryId(),
                formData.productId(),
                excludedId)) {
            throw new IllegalArgumentException(
                    "Đối tượng này đã có cấu hình VAT.");
        }
    }

    private void mapFormData(
            VatSetting setting,
            VatSettingFormData formData) {
        setting.setScopeType(
                formData.scopeType());

        setting.setCategoryId(
                formData.categoryId());

        setting.setProductId(
                formData.productId());

        setting.setVatRate(
                formData.vatRate());

        setting.setEnabled(
                formData.enabled());

        setting.setEffectiveFrom(
                formData.effectiveFrom());

        setting.setEffectiveTo(
                formData.effectiveTo());
    }
}