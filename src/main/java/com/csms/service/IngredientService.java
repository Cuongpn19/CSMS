package com.csms.service;

import com.csms.dao.IngredientDAO;
import com.csms.dto.IngredientFormData;
import com.csms.entity.Ingredient;
import com.csms.entity.IngredientStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class IngredientService {

    private final IngredientDAO ingredientDAO;

    public IngredientService() {
        ingredientDAO = new IngredientDAO();
    }

    public List<Ingredient> search(
            String keyword,
            IngredientStatus status,
            boolean lowStockOnly) {
        return ingredientDAO.search(
                keyword,
                status,
                lowStockOnly);
    }

    public Optional<Ingredient> findById(
            int ingredientId) {
        return ingredientDAO.findById(
                ingredientId);
    }

    public Ingredient create(
            IngredientFormData formData) {
        validate(
                formData,
                0);

        Ingredient ingredient = new Ingredient();

        mapFormData(
                ingredient,
                formData);

        int id = ingredientDAO.insert(
                ingredient);

        ingredient.setId(id);

        return ingredient;
    }

    public void update(
            int ingredientId,
            IngredientFormData formData) {
        Ingredient ingredient = ingredientDAO
                .findById(ingredientId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy nguyên liệu."));

        validate(
                formData,
                ingredientId);

        mapFormData(
                ingredient,
                formData);

        ingredientDAO.update(
                ingredient);
    }

    public void toggleStatus(
            int ingredientId) {
        Ingredient ingredient = ingredientDAO
                .findById(ingredientId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy nguyên liệu."));

        IngredientStatus newStatus = ingredient.getStatus() == IngredientStatus.ACTIVE
                ? IngredientStatus.INACTIVE
                : IngredientStatus.ACTIVE;

        ingredientDAO.updateStatus(
                ingredientId,
                newStatus);
    }

    private void validate(
            IngredientFormData formData,
            int excludedId) {
        if (formData == null) {
            throw new IllegalArgumentException(
                    "Thông tin nguyên liệu không hợp lệ.");
        }

        if (formData.name() == null
                || formData.name()
                        .trim()
                        .length() < 2) {

            throw new IllegalArgumentException(
                    "Tên nguyên liệu phải có ít nhất 2 ký tự.");
        }

        if (ingredientDAO.nameExists(
                formData.name().trim(),
                excludedId)) {
            throw new IllegalArgumentException(
                    "Tên nguyên liệu đã tồn tại.");
        }

        if (formData.unit() == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn đơn vị tính.");
        }

        validateNonNegative(
                formData.quantity(),
                "Số lượng tồn");

        validateNonNegative(
                formData.minimumStock(),
                "Mức tồn tối thiểu");

        validateNonNegative(
                formData.importPrice(),
                "Giá nhập");

        if (formData.status() == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn trạng thái.");
        }
    }

    private void validateNonNegative(
            BigDecimal value,
            String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(
                    fieldName
                            + " không được để trống.");
        }

        if (value.compareTo(
                BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    fieldName
                            + " không được nhỏ hơn 0.");
        }
    }

    private void mapFormData(
            Ingredient ingredient,
            IngredientFormData formData) {
        ingredient.setName(
                formData.name().trim());

        ingredient.setUnit(
                formData.unit());

        ingredient.setQuantity(
                formData.quantity());

        ingredient.setMinimumStock(
                formData.minimumStock());

        ingredient.setImportPrice(
                formData.importPrice());

        ingredient.setStatus(
                formData.status());
    }
}