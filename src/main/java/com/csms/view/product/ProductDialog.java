package com.csms.view.product;

import com.csms.entity.Category;
import com.csms.entity.Product;
import com.csms.entity.ProductStatus;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;

public class ProductDialog extends JDialog {

    private final JComboBox<Category> categoryComboBox;
    private final JTextField nameField;
    private final JTextField priceField;
    private final JTextField quantityField;
    private final JTextField imageField;
    private final JTextArea descriptionArea;
    private final JComboBox<ProductStatus> statusComboBox;

    private Product product;
    private boolean confirmed;

    public ProductDialog(
            JFrame owner,
            List<Category> categories,
            Product product) {
        super(
                owner,
                product == null
                        ? "Thêm sản phẩm"
                        : "Cập nhật sản phẩm",
                true);

        this.product = product;
        this.confirmed = false;

        categoryComboBox = new JComboBox<>();
        nameField = new JTextField();
        priceField = new JTextField();
        quantityField = new JTextField();
        imageField = new JTextField();
        descriptionArea = new JTextArea(4, 20);
        statusComboBox = new JComboBox<>(
                ProductStatus.values());

        initializeFrame();
        loadCategories(categories);
        initializeComponents();
        fillProductData();
    }

    private void initializeFrame() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(520, 580));
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    private void initializeComponents() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        24,
                        20,
                        24));

        JPanel formPanel = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.insets = new Insets(7, 5, 7, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;

        int row = 0;

        addFormRow(
                formPanel,
                constraints,
                row++,
                "Danh mục",
                categoryComboBox);

        addFormRow(
                formPanel,
                constraints,
                row++,
                "Tên sản phẩm",
                nameField);

        addFormRow(
                formPanel,
                constraints,
                row++,
                "Giá bán",
                priceField);

        addFormRow(
                formPanel,
                constraints,
                row++,
                "Số lượng",
                quantityField);

        addFormRow(
                formPanel,
                constraints,
                row++,
                "Đường dẫn ảnh",
                imageField);

        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        addFormRow(
                formPanel,
                constraints,
                row++,
                "Mô tả",
                descriptionScrollPane);

        addFormRow(
                formPanel,
                constraints,
                row,
                "Trạng thái",
                statusComboBox);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));

        JButton cancelButton = new JButton("Hủy");
        JButton saveButton = new JButton("Lưu");

        cancelButton.addActionListener(
                event -> dispose());

        saveButton.addActionListener(
                event -> handleSave());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        rootPanel.add(formPanel, BorderLayout.CENTER);
        rootPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
        pack();
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String labelText,
            java.awt.Component component) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        panel.add(
                new JLabel(labelText),
                constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;

        if (!(component instanceof JScrollPane)) {
            component.setPreferredSize(
                    new Dimension(300, 36));
        }

        panel.add(component, constraints);
    }

    private void loadCategories(
            List<Category> categories) {
        for (Category category : categories) {
            categoryComboBox.addItem(category);
        }
    }

    private void fillProductData() {
        if (product == null) {
            quantityField.setText("0");
            statusComboBox.setSelectedItem(
                    ProductStatus.AVAILABLE);
            return;
        }

        nameField.setText(product.getName());
        priceField.setText(
                product.getPrice().toPlainString());
        quantityField.setText(
                String.valueOf(product.getQuantity()));
        imageField.setText(
                product.getImage() == null
                        ? ""
                        : product.getImage());
        descriptionArea.setText(
                product.getDescription() == null
                        ? ""
                        : product.getDescription());
        statusComboBox.setSelectedItem(
                product.getStatus());

        for (int index = 0; index < categoryComboBox.getItemCount(); index++) {

            Category category = categoryComboBox.getItemAt(index);

            if (category.getId() == product.getCategoryId()) {

                categoryComboBox.setSelectedIndex(index);
                break;
            }
        }
    }

    private void handleSave() {
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();

        String name = nameField.getText().trim();
        String priceText = priceField.getText().trim();
        String quantityText = quantityField.getText().trim();

        if (selectedCategory == null) {
            showWarning("Vui lòng chọn danh mục.");
            return;
        }

        if (name.isBlank()) {
            showWarning("Vui lòng nhập tên sản phẩm.");
            nameField.requestFocusInWindow();
            return;
        }

        BigDecimal price;

        try {
            price = new BigDecimal(priceText);

            if (price.compareTo(BigDecimal.ZERO) < 0) {
                showWarning(
                        "Giá sản phẩm không được nhỏ hơn 0.");
                return;
            }
        } catch (NumberFormatException exception) {
            showWarning("Giá sản phẩm không hợp lệ.");
            priceField.requestFocusInWindow();
            return;
        }

        int quantity;

        try {
            quantity = Integer.parseInt(quantityText);

            if (quantity < 0) {
                showWarning(
                        "Số lượng không được nhỏ hơn 0.");
                return;
            }
        } catch (NumberFormatException exception) {
            showWarning("Số lượng không hợp lệ.");
            quantityField.requestFocusInWindow();
            return;
        }

        if (product == null) {
            product = new Product();
        }

        product.setCategoryId(
                selectedCategory.getId());
        product.setCategoryName(
                selectedCategory.getName());
        product.setName(name);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setImage(
                emptyToNull(imageField.getText()));
        product.setDescription(
                emptyToNull(descriptionArea.getText()));
        product.setStatus(
                (ProductStatus) statusComboBox.getSelectedItem());

        confirmed = true;
        dispose();
    }

    private String emptyToNull(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }

        return value.trim();
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Dữ liệu không hợp lệ",
                JOptionPane.WARNING_MESSAGE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Product getProduct() {
        return product;
    }
}