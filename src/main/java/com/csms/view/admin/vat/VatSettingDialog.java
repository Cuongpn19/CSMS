package com.csms.view.admin.vat;

import com.csms.dao.CategoryDAO;
import com.csms.dao.ProductDAO;
import com.csms.dto.VatSettingFormData;
import com.csms.entity.Category;
import com.csms.entity.Product;
import com.csms.entity.VatScopeType;
import com.csms.entity.VatSetting;
import com.csms.service.VatService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class VatSettingDialog extends JDialog {

    private final VatService vatService;
    private final CategoryDAO categoryDAO;
    private final ProductDAO productDAO;

    private final VatSetting editingSetting;

    private final JComboBox<VatScopeType> scopeTypeComboBox;

    private final JComboBox<Category> categoryComboBox;

    private final JComboBox<Product> productComboBox;

    private final JTextField vatRateField;

    private final JSpinner effectiveFromSpinner;
    private final JSpinner effectiveToSpinner;

    private final JCheckBox noEndDateCheckBox;
    private final JCheckBox enabledCheckBox;

    private final JLabel categoryLabel;
    private final JLabel productLabel;
    private final JLabel effectiveToLabel;

    private boolean saved;

    public VatSettingDialog(
            Window owner,
            VatSetting editingSetting) {
        super(
                owner,
                editingSetting == null
                        ? "Thêm cấu hình VAT"
                        : "Cập nhật cấu hình VAT",
                ModalityType.APPLICATION_MODAL);

        this.vatService = new VatService();
        this.categoryDAO = new CategoryDAO();
        this.productDAO = new ProductDAO();

        this.editingSetting = editingSetting;

        scopeTypeComboBox = new JComboBox<>(
                VatScopeType.values());

        categoryComboBox = new JComboBox<>();

        productComboBox = new JComboBox<>();

        vatRateField = new JTextField();

        effectiveFromSpinner = createDateTimeSpinner(
                new Date());

        effectiveToSpinner = createDateTimeSpinner(
                createDefaultEndDate());

        noEndDateCheckBox = new JCheckBox(
                "Không có thời điểm kết thúc");

        enabledCheckBox = new JCheckBox(
                "Đang áp dụng");

        categoryLabel = new JLabel("Danh mục:");

        productLabel = new JLabel("Món:");

        effectiveToLabel = new JLabel("Hiệu lực đến:");

        saved = false;

        loadCategories();
        loadProducts();

        initializeComponents();
        registerEvents();
        fillEditingData();
        updateScopeComponents();
        updateEndDateComponents();
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
                        22,
                        24,
                        22,
                        24));

        JLabel titleLabel = new JLabel(
                editingSetting == null
                        ? "THÊM CẤU HÌNH VAT"
                        : "CẬP NHẬT CẤU HÌNH VAT");

        titleLabel.setFont(
                titleLabel.getFont()
                        .deriveFont(
                                java.awt.Font.BOLD,
                                20F));

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
                new JLabel("Phạm vi áp dụng:"),
                scopeTypeComboBox);

        addFormRow(
                formPanel,
                constraints,
                row++,
                categoryLabel,
                categoryComboBox);

        addFormRow(
                formPanel,
                constraints,
                row++,
                productLabel,
                productComboBox);

        addFormRow(
                formPanel,
                constraints,
                row++,
                new JLabel("Mức VAT (%):"),
                vatRateField);

        addFormRow(
                formPanel,
                constraints,
                row++,
                new JLabel("Hiệu lực từ:"),
                effectiveFromSpinner);

        addFormRow(
                formPanel,
                constraints,
                row++,
                effectiveToLabel,
                effectiveToSpinner);

        constraints.gridx = 1;
        constraints.gridy = row++;
        constraints.weightx = 0.7;

        formPanel.add(
                noEndDateCheckBox,
                constraints);

        constraints.gridx = 1;
        constraints.gridy = row;
        constraints.weightx = 0.7;

        formPanel.add(
                enabledCheckBox,
                constraints);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT));

        JButton cancelButton = new JButton("Hủy");

        JButton saveButton = new JButton(
                editingSetting == null
                        ? "Thêm cấu hình"
                        : "Lưu thay đổi");

        cancelButton.addActionListener(
                event -> dispose());

        saveButton.addActionListener(
                event -> saveSetting());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        rootPanel.add(
                titleLabel,
                BorderLayout.NORTH);

        rootPanel.add(
                formPanel,
                BorderLayout.CENTER);

        rootPanel.add(
                buttonPanel,
                BorderLayout.SOUTH);

        setContentPane(rootPanel);

        setPreferredSize(
                new Dimension(
                        590,
                        570));

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            JLabel label,
            java.awt.Component component) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0.3;

        panel.add(
                label,
                constraints);

        constraints.gridx = 1;
        constraints.weightx = 0.7;

        component.setPreferredSize(
                new Dimension(
                        310,
                        36));

        panel.add(
                component,
                constraints);
    }

    private void registerEvents() {
        scopeTypeComboBox.addActionListener(
                event -> updateScopeComponents());

        noEndDateCheckBox.addActionListener(
                event -> updateEndDateComponents());
    }

    private void loadCategories() {
        categoryComboBox.removeAllItems();

        try {
            List<Category> categories = categoryDAO.findAllActive();

            for (Category category : categories) {
                categoryComboBox.addItem(category);
            }

        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Lỗi tải danh mục",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProducts() {
        productComboBox.removeAllItems();

        try {
            List<Product> products = productDAO.findAll();

            for (Product product : products) {
                productComboBox.addItem(product);
            }

        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Lỗi tải sản phẩm",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillEditingData() {
        enabledCheckBox.setSelected(true);
        vatRateField.setText("8");

        if (editingSetting == null) {
            scopeTypeComboBox.setSelectedItem(
                    VatScopeType.GLOBAL);

            noEndDateCheckBox.setSelected(true);
            return;
        }

        scopeTypeComboBox.setSelectedItem(
                editingSetting.getScopeType());

        vatRateField.setText(
                editingSetting
                        .getVatRate()
                        .stripTrailingZeros()
                        .toPlainString());

        enabledCheckBox.setSelected(
                editingSetting.isEnabled());

        if (editingSetting.getEffectiveFrom() != null) {

            effectiveFromSpinner.setValue(
                    convertLocalDateTimeToDate(
                            editingSetting
                                    .getEffectiveFrom()));
        }

        if (editingSetting.getEffectiveTo() == null) {

            noEndDateCheckBox.setSelected(true);

        } else {
            noEndDateCheckBox.setSelected(false);

            effectiveToSpinner.setValue(
                    convertLocalDateTimeToDate(
                            editingSetting
                                    .getEffectiveTo()));
        }

        selectCategory(
                editingSetting.getCategoryId());

        selectProduct(
                editingSetting.getProductId());
    }

    private void selectCategory(
            Integer categoryId) {
        if (categoryId == null) {
            return;
        }

        for (int index = 0; index < categoryComboBox.getItemCount(); index++) {

            Category category = categoryComboBox
                    .getItemAt(index);

            if (category.getId() == categoryId) {

                categoryComboBox
                        .setSelectedIndex(index);

                return;
            }
        }
    }

    private void selectProduct(
            Integer productId) {
        if (productId == null) {
            return;
        }

        for (int index = 0; index < productComboBox.getItemCount(); index++) {

            Product product = productComboBox
                    .getItemAt(index);

            if (product.getId() == productId) {

                productComboBox
                        .setSelectedIndex(index);

                return;
            }
        }
    }

    private void updateScopeComponents() {
        VatScopeType scopeType = (VatScopeType) scopeTypeComboBox
                .getSelectedItem();

        boolean categoryScope = scopeType == VatScopeType.CATEGORY;

        boolean productScope = scopeType == VatScopeType.PRODUCT;

        categoryLabel.setVisible(categoryScope);
        categoryComboBox.setVisible(categoryScope);

        productLabel.setVisible(productScope);
        productComboBox.setVisible(productScope);

        revalidate();
        repaint();
    }

    private void updateEndDateComponents() {
        boolean hasEndDate = !noEndDateCheckBox.isSelected();

        effectiveToLabel.setEnabled(
                hasEndDate);

        effectiveToSpinner.setEnabled(
                hasEndDate);
    }

    private void saveSetting() {
        VatScopeType scopeType = (VatScopeType) scopeTypeComboBox
                .getSelectedItem();

        Integer categoryId = null;
        Integer productId = null;

        if (scopeType == VatScopeType.CATEGORY) {
            Category category = (Category) categoryComboBox
                    .getSelectedItem();

            categoryId = category == null
                    ? null
                    : category.getId();
        }

        if (scopeType == VatScopeType.PRODUCT) {
            Product product = (Product) productComboBox
                    .getSelectedItem();

            productId = product == null
                    ? null
                    : product.getId();
        }

        BigDecimal vatRate;

        try {
            vatRate = new BigDecimal(
                    vatRateField
                            .getText()
                            .trim()
                            .replace(",", "."));

        } catch (NumberFormatException exception) {
            showWarning(
                    "Mức VAT không hợp lệ.");
            return;
        }

        LocalDateTime effectiveFrom = convertDateToLocalDateTime(
                (Date) effectiveFromSpinner
                        .getValue());

        LocalDateTime effectiveTo = noEndDateCheckBox.isSelected()
                ? null
                : convertDateToLocalDateTime(
                        (Date) effectiveToSpinner
                                .getValue());

        VatSettingFormData formData = new VatSettingFormData(
                scopeType,
                categoryId,
                productId,
                vatRate,
                enabledCheckBox.isSelected(),
                effectiveFrom,
                effectiveTo);

        try {
            if (editingSetting == null) {
                vatService.create(formData);

                JOptionPane.showMessageDialog(
                        this,
                        "Thêm cấu hình VAT thành công.",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

            } else {
                vatService.update(
                        editingSetting.getId(),
                        formData);

                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật cấu hình VAT thành công.",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            saved = true;
            dispose();

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            showWarning(
                    exception.getMessage());
        }
    }

    private JSpinner createDateTimeSpinner(
            Date initialDate) {
        SpinnerDateModel model = new SpinnerDateModel(
                initialDate,
                null,
                null,
                Calendar.MINUTE);

        JSpinner spinner = new JSpinner(model);

        spinner.setEditor(
                new JSpinner.DateEditor(
                        spinner,
                        "dd/MM/yyyy HH:mm"));

        return spinner;
    }

    private Date createDefaultEndDate() {
        Calendar calendar = Calendar.getInstance();

        calendar.add(
                Calendar.MONTH,
                1);

        return calendar.getTime();
    }

    private LocalDateTime convertDateToLocalDateTime(
            Date date) {
        return date.toInstant()
                .atZone(
                        ZoneId.systemDefault())
                .toLocalDateTime()
                .withSecond(0)
                .withNano(0);
    }

    private Date convertLocalDateTimeToDate(
            LocalDateTime dateTime) {
        return Date.from(
                dateTime.atZone(
                        ZoneId.systemDefault()).toInstant());
    }

    private void showWarning(
            String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Không thể lưu cấu hình VAT",
                JOptionPane.WARNING_MESSAGE);
    }

    public boolean isSaved() {
        return saved;
    }
}