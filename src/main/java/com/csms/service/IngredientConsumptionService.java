package com.csms.service;

import com.csms.dao.IngredientStockDAO;
import com.csms.dao.InventoryTransactionDAO;
import com.csms.dao.RecipeDAO;
import com.csms.dto.IngredientRequirement;
import com.csms.exception.InsufficientIngredientException;
import com.csms.view.waiter.model.CartItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IngredientConsumptionService {

    private final IngredientStockDAO ingredientStockDAO;
    private final RecipeDAO recipeDAO;
    private final InventoryTransactionDAO inventoryTransactionDAO;

    public IngredientConsumptionService() {
        ingredientStockDAO = new IngredientStockDAO();
        recipeDAO = new RecipeDAO();
        inventoryTransactionDAO = new InventoryTransactionDAO();
    }

    public void validateAndConsume(
            Connection connection,
            List<CartItem> cartItems) throws SQLException {

        List<IngredientStockDAO.ProductQuantity> productQuantities = convertProductQuantities(cartItems);

        List<IngredientRequirement> requirements = ingredientStockDAO
                .findRequirementsForUpdate(
                        connection,
                        productQuantities);

        validateRecipeCoverage(
                cartItems,
                requirements);

        validateEnoughStock(
                requirements);

        for (IngredientRequirement requirement : requirements) {

            ingredientStockDAO.deductStock(
                    connection,
                    requirement);
        }
    }

    private List<IngredientStockDAO.ProductQuantity> convertProductQuantities(
            List<CartItem> cartItems) {
        if (cartItems == null
                || cartItems.isEmpty()) {

            throw new IllegalArgumentException(
                    "Đơn hàng chưa có sản phẩm.");
        }

        List<IngredientStockDAO.ProductQuantity> quantities = new ArrayList<>();

        for (CartItem item : cartItems) {
            quantities.add(
                    new IngredientStockDAO.ProductQuantity(
                            item.getProduct().getId(),
                            item.getQuantity()));
        }

        return quantities;
    }

    private void validateEnoughStock(
            List<IngredientRequirement> requirements) {
        List<String> shortageMessages = new ArrayList<>();

        for (IngredientRequirement requirement : requirements) {

            if (!requirement.isEnough()) {
                shortageMessages.add(
                        "- "
                                + requirement.ingredientName()
                                + ": thiếu "
                                + formatQuantity(
                                        requirement.shortageQuantity())
                                + " "
                                + safeUnit(requirement.unit())
                                + " (còn "
                                + formatQuantity(
                                        requirement.availableQuantity())
                                + ")");
            }
        }

        if (!shortageMessages.isEmpty()) {
            throw new InsufficientIngredientException(
                    "Không đủ nguyên liệu để tạo đơn:\n"
                            + String.join(
                                    "\n",
                                    shortageMessages));
        }
    }

    /*
     * Bản kiểm tra đơn giản.
     *
     * Nếu dự án cho phép một sản phẩm không cần công thức,
     * có thể bỏ method này.
     *
     * Muốn kiểm tra chính xác từng product có recipe hay không,
     * nên bổ sung RecipeDAO.existsByProductId().
     */
    private void validateRecipeCoverage(
            List<CartItem> cartItems,
            List<IngredientRequirement> requirements) {
        if (!cartItems.isEmpty()
                && requirements.isEmpty()) {

            throw new IllegalStateException(
                    "Các sản phẩm trong đơn chưa được thiết lập công thức.");
        }
    }

    public List<IngredientRequirement> lockAndValidateRequirements(
            Connection connection,
            List<CartItem> cartItems) throws SQLException {

        validateRecipes(
                connection,
                cartItems);

        List<IngredientStockDAO.ProductQuantity> quantities = convertProductQuantities(
                cartItems);

        List<IngredientRequirement> requirements = ingredientStockDAO
                .findRequirementsForUpdate(
                        connection,
                        quantities);

        validateEnoughStock(
                requirements);

        return requirements;
    }

    public void consumeLockedRequirements(
            Connection connection,
            int orderId,
            int waiterId,
            List<IngredientRequirement> requirements) throws SQLException {

        for (IngredientRequirement requirement : requirements) {

            ingredientStockDAO.deductStock(
                    connection,
                    requirement);
            inventoryTransactionDAO
                    .insertSaleTransaction(
                            connection,
                            orderId,
                            waiterId,
                            requirement);
        }
    }

    private void validateRecipes(
            Connection connection,
            List<CartItem> cartItems) throws SQLException {

        List<String> missingProducts = new ArrayList<>();

        for (CartItem item : cartItems) {
            int productId = item.getProduct().getId();

            if (!recipeDAO.existsByProductId(
                    connection,
                    productId)) {
                missingProducts.add(
                        item.getProduct().getName());
            }
        }

        if (!missingProducts.isEmpty()) {
            throw new IllegalStateException(
                    "Các sản phẩm chưa có công thức:\n- "
                            + String.join(
                                    "\n- ",
                                    missingProducts));
        }
    }

    private String formatQuantity(
            BigDecimal value) {
        if (value == null) {
            return "0";
        }

        return value
                .stripTrailingZeros()
                .toPlainString();
    }

    private String safeUnit(
            String unit) {
        return unit == null
                ? ""
                : unit;
    }
}