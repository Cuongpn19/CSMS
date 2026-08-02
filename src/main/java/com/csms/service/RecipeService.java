package com.csms.service;

import com.csms.dao.RecipeDAO;
import com.csms.entity.ProductRecipe;

import java.math.BigDecimal;
import java.util.List;

public class RecipeService {

        private final RecipeDAO recipeDAO;

        public RecipeService() {
                recipeDAO = new RecipeDAO();
        }

        public List<ProductRecipe> findByProductId(
                        int productId) {
                return recipeDAO.findByProductId(
                                productId);
        }

        public void create(
                        ProductRecipe recipe) {
                validate(recipe, 0);

                recipeDAO.insert(recipe);
        }

        public void update(
                        ProductRecipe recipe) {
                if (recipe.getId() <= 0) {
                        throw new IllegalArgumentException(
                                        "Mã công thức không hợp lệ.");
                }

                validate(
                                recipe,
                                recipe.getId());

                recipeDAO.update(recipe);
        }

        public void delete(
                        int recipeId) {
                recipeDAO.delete(recipeId);
        }

        private void validate(
                        ProductRecipe recipe,
                        int excludedRecipeId) {
                if (recipe == null) {
                        throw new IllegalArgumentException(
                                        "Thông tin công thức không hợp lệ.");
                }

                if (recipe.getProductId() <= 0) {
                        throw new IllegalArgumentException(
                                        "Vui lòng chọn món.");
                }

                if (recipe.getIngredientId() <= 0) {
                        throw new IllegalArgumentException(
                                        "Vui lòng chọn nguyên liệu.");
                }

                if (recipe.getQuantityRequired() == null
                                || recipe.getQuantityRequired()
                                                .compareTo(BigDecimal.ZERO) <= 0) {

                        throw new IllegalArgumentException(
                                        "Định lượng phải lớn hơn 0.");
                }

                if (recipe.getUnit() == null
                                || recipe.getUnit().isBlank()) {

                        throw new IllegalArgumentException(
                                        "Đơn vị tính không được để trống.");
                }

                if (recipeDAO.exists(
                                recipe.getProductId(),
                                recipe.getIngredientId(),
                                excludedRecipeId)) {
                        throw new IllegalArgumentException(
                                        "Nguyên liệu này đã có trong công thức.");
                }
        }
}