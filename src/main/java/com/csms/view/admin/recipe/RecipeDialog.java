package com.csms.view.admin.recipe;

import com.csms.entity.Ingredient;
import com.csms.entity.ProductRecipe;
import com.csms.service.RecipeService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.util.List;

public class RecipeDialog extends JDialog {

    private final RecipeService recipeService;

    private final int productId;
    private final String productName;

    private final ProductRecipe editingRecipe;

    private final JComboBox<Ingredient> ingredientComboBox;

    private final JTextField quantityField;
    private final JTextField unitField;

    private boolean saved;

    public RecipeDialog(
            Window owner,
            int productId,
            String productName,
            List<Ingredient> ingredients,
            ProductRecipe editingRecipe) {
        super(
                owner,
                editingRecipe == null
                        ? "Thêm nguyên liệu vào công thức"
                        : "Cập nhật công thức",
                ModalityType.APPLICATION_MODAL);

        this.recipeService = new RecipeService();

        this.productId = productId;
        this.productName = productName;
        this.editingRecipe = editingRecipe;

        ingredientComboBox = new JComboBox<>();

        quantityField = new JTextField();

        unitField = new JTextField();

        unitField.setEditable(false);

        saved = false;

        for (Ingredient ingredient : ingredients) {
            ingredientComboBox.addItem(
                    ingredient);
        }

        initializeComponents();
        fillEditingData();
        registerEvents();
    }

    private void initializeComponents() {
        setDefaultCloseOperation(
                DISPOSE_ON_CLOSE);

        setResizable(false);

        JPanel rootPanel = new JPanel(
                new BorderLayout(
                        0,
                        18));

        rootPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        24,
                        20,
                        24));

        JLabel productLabel = new JLabel(
                "Món: " + productName);

        JPanel formPanel = new JPanel(
                new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.insets = new Insets(
                8,
                8,
                8,
                8);

        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.weightx = 1;

        addFormRow(
                formPanel,
                constraints,
                0,
                "Nguyên liệu:",
                ingredientComboBox);

        addFormRow(
                formPanel,
                constraints,
                1,
                "Định lượng:",
                quantityField);

        addFormRow(
                formPanel,
                constraints,
                2,
                "Đơn vị:",
                unitField);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT));

        JButton cancelButton = new JButton("Hủy");

        JButton saveButton = new JButton(
                editingRecipe == null
                        ? "Thêm vào công thức"
                        : "Lưu thay đổi");

        cancelButton.addActionListener(
                event -> dispose());

        saveButton.addActionListener(
                event -> saveRecipe());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        rootPanel.add(
                productLabel,
                BorderLayout.NORTH);

        rootPanel.add(
                formPanel,
                BorderLayout.CENTER);

        rootPanel.add(
                buttonPanel,
                BorderLayout.SOUTH);

        setContentPane(rootPanel);

        setPreferredSize(
                new Dimension(520, 320));

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String label,
            java.awt.Component component) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0.3;

        panel.add(
                new JLabel(label),
                constraints);

        constraints.gridx = 1;
        constraints.weightx = 0.7;

        component.setPreferredSize(
                new Dimension(270, 36));

        panel.add(
                component,
                constraints);
    }

    private void registerEvents() {
        ingredientComboBox.addActionListener(
                event -> updateUnitField());
    }

    private void fillEditingData() {
        if (editingRecipe == null) {
            updateUnitField();
            return;
        }

        for (int index = 0; index < ingredientComboBox.getItemCount(); index++) {

            Ingredient ingredient = ingredientComboBox
                    .getItemAt(index);

            if (ingredient.getId() == editingRecipe.getIngredientId()) {

                ingredientComboBox
                        .setSelectedIndex(index);

                break;
            }
        }

        quantityField.setText(
                editingRecipe
                        .getQuantityRequired()
                        .stripTrailingZeros()
                        .toPlainString());

        unitField.setText(
                editingRecipe.getUnit());
    }

    private void updateUnitField() {
        Ingredient ingredient = (Ingredient) ingredientComboBox
                .getSelectedItem();

        unitField.setText(
                ingredient == null
                        ? ""
                        : ingredient.getUnit());
    }

    private void saveRecipe() {
        Ingredient ingredient = (Ingredient) ingredientComboBox
                .getSelectedItem();

        if (ingredient == null) {
            showWarning(
                    "Vui lòng chọn nguyên liệu.");
            return;
        }

        BigDecimal quantityRequired;

        try {
            String input = quantityField
                    .getText()
                    .trim()
                    .replace(",", ".");

            quantityRequired = new BigDecimal(input);

        } catch (NumberFormatException exception) {
            showWarning(
                    "Định lượng không hợp lệ.");
            return;
        }

        ProductRecipe recipe = editingRecipe == null
                ? new ProductRecipe()
                : editingRecipe;

        recipe.setProductId(productId);

        recipe.setIngredientId(
                ingredient.getId());

        recipe.setIngredientName(
                ingredient.getName());

        recipe.setQuantityRequired(
                quantityRequired);

        recipe.setUnit(
                ingredient.getUnit());

        try {
            if (editingRecipe == null) {
                recipeService.create(recipe);
            } else {
                recipeService.update(recipe);
            }

            saved = true;

            JOptionPane.showMessageDialog(
                    this,
                    editingRecipe == null
                            ? "Thêm nguyên liệu vào công thức thành công."
                            : "Cập nhật công thức thành công.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            showWarning(
                    exception.getMessage());
        }
    }

    private void showWarning(
            String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
    }

    public boolean isSaved() {
        return saved;
    }
}