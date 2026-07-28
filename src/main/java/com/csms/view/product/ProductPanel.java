package com.csms.view.product;

import com.csms.dao.CategoryDAO;
import com.csms.dao.ProductDAO;
import com.csms.entity.Category;
import com.csms.entity.Product;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ProductPanel extends JPanel {

    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;

    private final JTextField searchField;
    private final DefaultTableModel tableModel;
    private final JTable productTable;

    private final NumberFormat currencyFormat;

    public ProductPanel() {
        productDAO = new ProductDAO();
        categoryDAO = new CategoryDAO();

        searchField = new JTextField();

        tableModel = new DefaultTableModel(
                new Object[] {
                        "ID",
                        "Tên sản phẩm",
                        "Danh mục",
                        "Giá bán",
                        "Số lượng",
                        "Trạng thái"
                },
                0) {
            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {
                return false;
            }
        };

        productTable = new JTable(tableModel);

        currencyFormat = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("vi-VN"));

        initializeComponents();
        loadProducts();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(0, 18));

        setBorder(
                BorderFactory.createEmptyBorder(
                        25,
                        25,
                        25,
                        25));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(
                new BorderLayout(15, 15));

        JLabel titleLabel = new JLabel(
                "QUẢN LÝ SẢN PHẨM");

        titleLabel.setFont(
                titleLabel.getFont().deriveFont(
                        Font.BOLD,
                        24F));

        JPanel searchPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        8,
                        0));

        searchField.setPreferredSize(
                new Dimension(280, 38));

        JButton searchButton = new JButton("Tìm kiếm");
        JButton refreshButton = new JButton("Làm mới");

        searchButton.addActionListener(
                event -> searchProducts());

        refreshButton.addActionListener(
                event -> {
                    searchField.setText("");
                    loadProducts();
                });

        searchField.addActionListener(
                event -> searchProducts());

        searchPanel.add(new JLabel("Từ khóa:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        JPanel actionPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        8,
                        0));

        JButton addButton = new JButton("Thêm");
        JButton editButton = new JButton("Sửa");
        JButton deleteButton = new JButton("Xóa");

        addButton.addActionListener(
                event -> addProduct());

        editButton.addActionListener(
                event -> editProduct());

        deleteButton.addActionListener(
                event -> deleteProduct());

        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);

        JPanel controlPanel = new JPanel(
                new BorderLayout());

        controlPanel.add(
                searchPanel,
                BorderLayout.WEST);

        controlPanel.add(
                actionPanel,
                BorderLayout.EAST);

        headerPanel.add(
                titleLabel,
                BorderLayout.NORTH);

        headerPanel.add(
                controlPanel,
                BorderLayout.SOUTH);

        return headerPanel;
    }

    private JScrollPane createTablePanel() {
        productTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        productTable.setRowHeight(34);
        productTable.getTableHeader().setReorderingAllowed(false);

        productTable.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(50);

        productTable.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(220);

        productTable.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(140);

        productTable.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(130);

        productTable.getColumnModel()
                .getColumn(4)
                .setPreferredWidth(90);

        productTable.getColumnModel()
                .getColumn(5)
                .setPreferredWidth(120);

        productTable.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent event) {
                        if (event.getClickCount() == 2) {
                            editProduct();
                        }
                    }
                });

        return new JScrollPane(productTable);
    }

    private void loadProducts() {
        try {
            List<Product> products = productDAO.findAll();

            displayProducts(products);

        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void searchProducts() {
        try {
            String keyword = searchField.getText().trim();

            List<Product> products = keyword.isBlank()
                    ? productDAO.findAll()
                    : productDAO.search(keyword);

            displayProducts(products);

        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void displayProducts(
            List<Product> products) {
        tableModel.setRowCount(0);

        for (Product product : products) {
            tableModel.addRow(
                    new Object[] {
                            product.getId(),
                            product.getName(),
                            product.getCategoryName(),
                            currencyFormat.format(
                                    product.getPrice()),
                            product.getQuantity(),
                            getStatusText(product)
                    });
        }
    }

    private String getStatusText(Product product) {
        return switch (product.getStatus()) {
            case AVAILABLE -> "Đang bán";
            case UNAVAILABLE -> "Ngừng bán";
        };
    }

    private void addProduct() {
        try {
            List<Category> categories = categoryDAO.findAllActive();

            if (categories.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Chưa có danh mục đang hoạt động.",
                        "Không thể thêm sản phẩm",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            ProductDialog dialog = new ProductDialog(
                    getOwnerFrame(),
                    categories,
                    null);

            dialog.setVisible(true);

            if (!dialog.isConfirmed()) {
                return;
            }

            productDAO.insert(dialog.getProduct());

            JOptionPane.showMessageDialog(
                    this,
                    "Thêm sản phẩm thành công.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            loadProducts();

        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void editProduct() {
        Integer selectedProductId = getSelectedProductId();

        if (selectedProductId == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn sản phẩm cần sửa.",
                    "Chưa chọn sản phẩm",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Optional<Product> optionalProduct = productDAO.findById(
                    selectedProductId);

            if (optionalProduct.isEmpty()) {
                showError(
                        "Sản phẩm không còn tồn tại.");
                loadProducts();
                return;
            }

            List<Category> categories = categoryDAO.findAllActive();

            ProductDialog dialog = new ProductDialog(
                    getOwnerFrame(),
                    categories,
                    optionalProduct.get());

            dialog.setVisible(true);

            if (!dialog.isConfirmed()) {
                return;
            }

            boolean updated = productDAO.update(
                    dialog.getProduct());

            if (!updated) {
                showError(
                        "Không tìm thấy sản phẩm cần cập nhật.");
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Cập nhật sản phẩm thành công.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            loadProducts();

        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void deleteProduct() {
        Integer selectedProductId = getSelectedProductId();

        if (selectedProductId == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn sản phẩm cần xóa.",
                    "Chưa chọn sản phẩm",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = productTable.getSelectedRow();

        String productName = String.valueOf(
                tableModel.getValueAt(
                        selectedRow,
                        1));

        int result = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa sản phẩm \""
                        + productName
                        + "\"?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean deleted = productDAO.delete(
                    selectedProductId);

            if (!deleted) {
                showError(
                        "Không tìm thấy sản phẩm cần xóa.");
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Xóa sản phẩm thành công.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            loadProducts();

        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private Integer getSelectedProductId() {
        int selectedRow = productTable.getSelectedRow();

        if (selectedRow < 0) {
            return null;
        }

        Object idValue = tableModel.getValueAt(
                selectedRow,
                0);

        return Integer.parseInt(
                idValue.toString());
    }

    private JFrame getOwnerFrame() {
        return (JFrame) SwingUtilities.getWindowAncestor(
                this);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
    }
}