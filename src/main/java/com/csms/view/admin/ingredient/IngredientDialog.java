package com.csms.view.admin.ingredient;

import com.csms.dto.IngredientFormData;
import com.csms.entity.Ingredient;
import com.csms.entity.IngredientStatus;
import com.csms.entity.IngredientUnit;
import com.csms.service.IngredientService;

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

public class IngredientDialog extends JDialog {

        private final IngredientService ingredientService;
        private final Ingredient editingIngredient;

        private final JTextField nameField;
        private final JComboBox<IngredientUnit> unitComboBox;
        private final JTextField quantityField;
        private final JTextField minimumStockField;
        private final JTextField importPriceField;
        private final JComboBox<IngredientStatus> statusComboBox;

        private boolean saved;

        public IngredientDialog(
                        Window owner,
                        Ingredient editingIngredient) {
                super(
                                owner,
                                editingIngredient == null
                                                ? "Thêm nguyên liệu"
                                                : "Cập nhật nguyên liệu",
                                ModalityType.APPLICATION_MODAL);

                this.ingredientService = new IngredientService();

                this.editingIngredient = editingIngredient;

                nameField = new JTextField();

                unitComboBox = new JComboBox<>(
                                IngredientUnit.values());

                quantityField = new JTextField();

                minimumStockField = new JTextField();

                importPriceField = new JTextField();

                statusComboBox = new JComboBox<>(
                                IngredientStatus.values());

                saved = false;

                initializeComponents();
                fillEditingData();
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

                JPanel formPanel = new JPanel(
                                new GridBagLayout());

                GridBagConstraints constraints = new GridBagConstraints();

                constraints.insets = new Insets(
                                8,
                                8,
                                8,
                                8);

                constraints.fill = GridBagConstraints.HORIZONTAL;

                int row = 0;

                addFormRow(
                                formPanel,
                                constraints,
                                row++,
                                "Tên nguyên liệu:",
                                nameField);

                addFormRow(
                                formPanel,
                                constraints,
                                row++,
                                "Đơn vị tính:",
                                unitComboBox);

                addFormRow(
                                formPanel,
                                constraints,
                                row++,
                                "Số lượng tồn:",
                                quantityField);

                addFormRow(
                                formPanel,
                                constraints,
                                row++,
                                "Mức tồn tối thiểu:",
                                minimumStockField);

                addFormRow(
                                formPanel,
                                constraints,
                                row++,
                                "Giá nhập:",
                                importPriceField);

                addFormRow(
                                formPanel,
                                constraints,
                                row,
                                "Trạng thái:",
                                statusComboBox);

                JPanel buttonPanel = new JPanel(
                                new FlowLayout(
                                                FlowLayout.RIGHT));

                JButton cancelButton = new JButton("Hủy");

                JButton saveButton = new JButton(
                                editingIngredient == null
                                                ? "Thêm nguyên liệu"
                                                : "Lưu thay đổi");

                cancelButton.addActionListener(
                                event -> dispose());

                saveButton.addActionListener(
                                event -> saveIngredient());

                buttonPanel.add(cancelButton);
                buttonPanel.add(saveButton);

                rootPanel.add(
                                formPanel,
                                BorderLayout.CENTER);

                rootPanel.add(
                                buttonPanel,
                                BorderLayout.SOUTH);

                setContentPane(rootPanel);

                setPreferredSize(
                                new Dimension(560, 480));

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
                constraints.weightx = 0.35;

                panel.add(
                                new JLabel(label),
                                constraints);

                constraints.gridx = 1;
                constraints.weightx = 0.65;

                component.setPreferredSize(
                                new Dimension(280, 36));

                panel.add(
                                component,
                                constraints);
        }

        private void fillEditingData() {
                statusComboBox.setSelectedItem(
                                IngredientStatus.ACTIVE);

                if (editingIngredient == null) {
                        quantityField.setText("0");
                        minimumStockField.setText("0");
                        importPriceField.setText("0");
                        return;
                }

                nameField.setText(
                                editingIngredient.getName());

                unitComboBox.setSelectedItem(
                                editingIngredient.getUnit());

                quantityField.setText(
                                formatNumber(
                                                editingIngredient.getQuantity()));

                minimumStockField.setText(
                                formatNumber(
                                                editingIngredient.getMinimumStock()));

                importPriceField.setText(
                                formatNumber(
                                                editingIngredient.getImportPrice()));

                statusComboBox.setSelectedItem(
                                editingIngredient.getStatus());
        }

        private void saveIngredient() {
                try {
                        IngredientFormData formData = new IngredientFormData(
                                        nameField
                                                        .getText()
                                                        .trim(),

                                        (IngredientUnit) unitComboBox
                                                        .getSelectedItem(),

                                        parseNumber(
                                                        quantityField.getText(),
                                                        "Số lượng tồn"),

                                        parseNumber(
                                                        minimumStockField.getText(),
                                                        "Mức tồn tối thiểu"),

                                        parseNumber(
                                                        importPriceField.getText(),
                                                        "Giá nhập"),

                                        (IngredientStatus) statusComboBox
                                                        .getSelectedItem());

                        if (editingIngredient == null) {
                                ingredientService.create(
                                                formData);

                        } else {
                                ingredientService.update(
                                                editingIngredient.getId(),
                                                formData);
                        }

                        saved = true;

                        JOptionPane.showMessageDialog(
                                        this,
                                        editingIngredient == null
                                                        ? "Thêm nguyên liệu thành công."
                                                        : "Cập nhật nguyên liệu thành công.",
                                        "Thành công",
                                        JOptionPane.INFORMATION_MESSAGE);

                        dispose();

                } catch (
                                IllegalArgumentException
                                | IllegalStateException exception) {
                        JOptionPane.showMessageDialog(
                                        this,
                                        exception.getMessage(),
                                        "Không thể lưu nguyên liệu",
                                        JOptionPane.WARNING_MESSAGE);
                }
        }

        private BigDecimal parseNumber(
                        String input,
                        String fieldName) {
                if (input == null
                                || input.trim().isEmpty()) {

                        throw new IllegalArgumentException(
                                        fieldName
                                                        + " không được để trống.");
                }

                try {
                        String normalized = input.trim()
                                        .replace(" ", "")
                                        .replace(",", ".");

                        return new BigDecimal(normalized);

                } catch (NumberFormatException exception) {
                        throw new IllegalArgumentException(
                                        fieldName
                                                        + " không hợp lệ.");
                }
        }

        private String formatNumber(
                        BigDecimal value) {
                return value == null
                                ? "0"
                                : value.stripTrailingZeros()
                                                .toPlainString();
        }

        public boolean isSaved() {
                return saved;
        }
}