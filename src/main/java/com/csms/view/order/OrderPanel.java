package com.csms.view.order;

import com.csms.dao.CoffeeTableDAO;
import com.csms.dao.OrderDAO;
import com.csms.dao.ProductDAO;
import com.csms.entity.CoffeeTable;
import com.csms.entity.Order;
import com.csms.entity.OrderDetail;
import com.csms.entity.OrderStatus;
import com.csms.entity.OrderType;
import com.csms.entity.Product;
import com.csms.entity.User;
import com.csms.utils.OrderCodeGenerator;
import com.csms.utils.SessionManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderPanel extends JPanel {

    private final ProductDAO productDAO;
    private final CoffeeTableDAO coffeeTableDAO;
    private final OrderDAO orderDAO;

    private final JComboBox<OrderType> orderTypeComboBox;
    private final JComboBox<CoffeeTable> tableComboBox;

    private final DefaultTableModel productTableModel;
    private final JTable productTable;

    private final DefaultTableModel cartTableModel;
    private final JTable cartTable;

    private final JSpinner quantitySpinner;
    private final JTextField discountField;
    private final JTextArea noteArea;

    private final JLabel subtotalLabel;
    private final JLabel totalLabel;

    private final List<Product> availableProducts;
    private final List<OrderDetail> cartItems;

    private final NumberFormat currencyFormat;

    public OrderPanel() {
        productDAO = new ProductDAO();
        coffeeTableDAO = new CoffeeTableDAO();
        orderDAO = new OrderDAO();

        availableProducts = new ArrayList<>();
        cartItems = new ArrayList<>();

        currencyFormat = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("vi-VN"));

        orderTypeComboBox = new JComboBox<>(
                OrderType.values());

        tableComboBox = new JComboBox<>();

        quantitySpinner = new JSpinner(
                new SpinnerNumberModel(
                        1,
                        1,
                        100,
                        1));

        discountField = new JTextField("0");
        noteArea = new JTextArea(3, 20);

        subtotalLabel = new JLabel(
                currencyFormat.format(BigDecimal.ZERO),
                SwingConstants.RIGHT);

        totalLabel = new JLabel(
                currencyFormat.format(BigDecimal.ZERO),
                SwingConstants.RIGHT);

        productTableModel = createProductTableModel();
        productTable = new JTable(productTableModel);

        cartTableModel = createCartTableModel();
        cartTable = new JTable(cartTableModel);

        initializeComponents();
        registerEvents();

        loadProducts();
        loadAvailableTables();
        updateOrderTypeState();
        refreshTotals();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(18, 18));

        setBorder(
                BorderFactory.createEmptyBorder(
                        22,
                        22,
                        22,
                        22));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(
                new BorderLayout(15, 15));

        JLabel titleLabel = new JLabel(
                "TẠO ĐƠN HÀNG");

        titleLabel.setFont(
                titleLabel.getFont().deriveFont(
                        Font.BOLD,
                        24F));

        JPanel orderInfoPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        0));

        orderTypeComboBox.setPreferredSize(
                new Dimension(150, 36));

        tableComboBox.setPreferredSize(
                new Dimension(160, 36));

        orderInfoPanel.add(
                new JLabel("Loại đơn:"));
        orderInfoPanel.add(orderTypeComboBox);
        orderInfoPanel.add(
                new JLabel("Chọn bàn:"));
        orderInfoPanel.add(tableComboBox);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(orderInfoPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(
                new GridLayout(1, 2, 18, 0));

        mainPanel.add(createProductSection());
        mainPanel.add(createCartSection());

        return mainPanel;
    }

    private JPanel createProductSection() {
        JPanel panel = new JPanel(
                new BorderLayout(0, 12));

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        "Danh sách sản phẩm"));

        productTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        productTable.setRowHeight(32);
        productTable.getTableHeader()
                .setReorderingAllowed(false);

        productTable.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(45);

        productTable.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(180);

        productTable.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(110);

        productTable.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(100);

        productTable.getColumnModel()
                .getColumn(4)
                .setPreferredWidth(70);

        JPanel actionPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        8,
                        0));

        quantitySpinner.setPreferredSize(
                new Dimension(80, 36));

        JButton addButton = new JButton("Thêm vào đơn");

        addButton.addActionListener(
                event -> addSelectedProduct());

        actionPanel.add(
                new JLabel("Số lượng:"));
        actionPanel.add(quantitySpinner);
        actionPanel.add(addButton);

        panel.add(
                new JScrollPane(productTable),
                BorderLayout.CENTER);

        panel.add(
                actionPanel,
                BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCartSection() {
        JPanel panel = new JPanel(
                new BorderLayout(0, 12));

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        "Chi tiết đơn hàng"));

        cartTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        cartTable.setRowHeight(32);
        cartTable.getTableHeader()
                .setReorderingAllowed(false);

        cartTable.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(45);

        cartTable.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(170);

        cartTable.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(90);

        cartTable.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(70);

        cartTable.getColumnModel()
                .getColumn(4)
                .setPreferredWidth(110);

        JPanel actionPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        8,
                        0));

        JButton decreaseButton = new JButton("-");

        JButton increaseButton = new JButton("+");

        JButton removeButton = new JButton("Xóa");

        decreaseButton.addActionListener(
                event -> decreaseQuantity());

        increaseButton.addActionListener(
                event -> increaseQuantity());

        removeButton.addActionListener(
                event -> removeCartItem());

        actionPanel.add(decreaseButton);
        actionPanel.add(increaseButton);
        actionPanel.add(removeButton);

        panel.add(
                new JScrollPane(cartTable),
                BorderLayout.CENTER);

        panel.add(
                actionPanel,
                BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel rootPanel = new JPanel(
                new BorderLayout(18, 0));

        rootPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        5,
                        0,
                        0,
                        0));

        JPanel notePanel = new JPanel(
                new BorderLayout(8, 8));

        notePanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Ghi chú"));

        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);

        notePanel.add(
                new JScrollPane(noteArea),
                BorderLayout.CENTER);

        JPanel paymentPanel = new JPanel(
                new GridLayout(4, 2, 10, 10));

        paymentPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Thông tin thanh toán"));

        discountField.setHorizontalAlignment(
                JTextField.RIGHT);

        subtotalLabel.setFont(
                subtotalLabel.getFont().deriveFont(
                        Font.BOLD,
                        16F));

        totalLabel.setFont(
                totalLabel.getFont().deriveFont(
                        Font.BOLD,
                        20F));

        JButton createOrderButton = new JButton("Tạo đơn hàng");

        JButton clearButton = new JButton("Làm mới đơn");

        createOrderButton.addActionListener(
                event -> createOrder());

        clearButton.addActionListener(
                event -> clearOrder());

        paymentPanel.add(new JLabel("Tạm tính:"));
        paymentPanel.add(subtotalLabel);

        paymentPanel.add(new JLabel("Giảm giá:"));
        paymentPanel.add(discountField);

        paymentPanel.add(new JLabel("Tổng tiền:"));
        paymentPanel.add(totalLabel);

        paymentPanel.add(clearButton);
        paymentPanel.add(createOrderButton);

        notePanel.setPreferredSize(
                new Dimension(0, 155));

        paymentPanel.setPreferredSize(
                new Dimension(390, 155));

        rootPanel.add(notePanel, BorderLayout.CENTER);
        rootPanel.add(paymentPanel, BorderLayout.EAST);

        return rootPanel;
    }

    private DefaultTableModel createProductTableModel() {
        return new DefaultTableModel(
                new Object[] {
                        "ID",
                        "Sản phẩm",
                        "Danh mục",
                        "Giá",
                        "Tồn kho"
                },
                0) {
            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {
                return false;
            }
        };
    }

    private DefaultTableModel createCartTableModel() {
        return new DefaultTableModel(
                new Object[] {
                        "ID",
                        "Sản phẩm",
                        "Đơn giá",
                        "SL",
                        "Thành tiền"
                },
                0) {
            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {
                return false;
            }
        };
    }

    private void registerEvents() {
        orderTypeComboBox.addActionListener(
                event -> updateOrderTypeState());

        discountField.getDocument()
                .addDocumentListener(
                        new javax.swing.event.DocumentListener() {
                            @Override
                            public void insertUpdate(
                                    javax.swing.event.DocumentEvent event) {
                                refreshTotals();
                            }

                            @Override
                            public void removeUpdate(
                                    javax.swing.event.DocumentEvent event) {
                                refreshTotals();
                            }

                            @Override
                            public void changedUpdate(
                                    javax.swing.event.DocumentEvent event) {
                                refreshTotals();
                            }
                        });

        productTable.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent event) {
                        if (event.getClickCount() == 2) {
                            addSelectedProduct();
                        }
                    }
                });
    }

    private void loadProducts() {
        try {
            availableProducts.clear();
            availableProducts.addAll(
                    productDAO.findAvailable());

            productTableModel.setRowCount(0);

            for (Product product : availableProducts) {
                productTableModel.addRow(
                        new Object[] {
                                product.getId(),
                                product.getName(),
                                product.getCategoryName(),
                                currencyFormat.format(
                                        product.getPrice()),
                                product.getQuantity()
                        });
            }

        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void loadAvailableTables() {
        try {
            tableComboBox.removeAllItems();

            List<CoffeeTable> tables = coffeeTableDAO.findAvailable();

            for (CoffeeTable table : tables) {
                tableComboBox.addItem(table);
            }

        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void updateOrderTypeState() {
        OrderType selectedType = (OrderType) orderTypeComboBox
                .getSelectedItem();

        boolean dineIn = selectedType == OrderType.DINE_IN;

        tableComboBox.setEnabled(dineIn);
    }

    private void addSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();

        if (selectedRow < 0) {
            showWarning(
                    "Vui lòng chọn sản phẩm cần thêm.");
            return;
        }

        int productId = Integer.parseInt(
                productTableModel.getValueAt(
                        selectedRow,
                        0).toString());

        Product product = availableProducts.stream()
                .filter(item -> item.getId() == productId)
                .findFirst()
                .orElse(null);

        if (product == null) {
            showWarning(
                    "Không tìm thấy sản phẩm.");
            return;
        }

        int quantity = (Integer) quantitySpinner.getValue();

        OrderDetail existingItem = cartItems.stream()
                .filter(item -> item.getProductId() == product.getId())
                .findFirst()
                .orElse(null);

        int currentQuantity = existingItem == null
                ? 0
                : existingItem.getQuantity();

        if (currentQuantity + quantity > product.getQuantity()) {
            showWarning(
                    "Số lượng vượt quá tồn kho. Tồn kho hiện tại: "
                            + product.getQuantity());
            return;
        }

        if (existingItem != null) {
            existingItem.setQuantity(
                    existingItem.getQuantity()
                            + quantity);
            existingItem.calculateSubtotal();

        } else {
            OrderDetail detail = new OrderDetail();

            detail.setProductId(product.getId());
            detail.setProductName(product.getName());
            detail.setUnitPrice(product.getPrice());
            detail.setQuantity(quantity);
            detail.calculateSubtotal();

            cartItems.add(detail);
        }

        quantitySpinner.setValue(1);
        refreshCartTable();
    }

    private void increaseQuantity() {
        OrderDetail detail = getSelectedCartItem();

        if (detail == null) {
            showWarning(
                    "Vui lòng chọn sản phẩm trong đơn.");
            return;
        }

        Product product = availableProducts.stream()
                .filter(item -> item.getId() == detail.getProductId())
                .findFirst()
                .orElse(null);

        if (product == null) {
            showWarning(
                    "Không tìm thấy thông tin tồn kho.");
            return;
        }

        if (detail.getQuantity() + 1 > product.getQuantity()) {
            showWarning(
                    "Số lượng đã đạt giới hạn tồn kho.");
            return;
        }

        detail.setQuantity(
                detail.getQuantity() + 1);
        detail.calculateSubtotal();

        refreshCartTable();
    }

    private void decreaseQuantity() {
        OrderDetail detail = getSelectedCartItem();

        if (detail == null) {
            showWarning(
                    "Vui lòng chọn sản phẩm trong đơn.");
            return;
        }

        if (detail.getQuantity() <= 1) {
            cartItems.remove(detail);
        } else {
            detail.setQuantity(
                    detail.getQuantity() - 1);
            detail.calculateSubtotal();
        }

        refreshCartTable();
    }

    private void removeCartItem() {
        OrderDetail detail = getSelectedCartItem();

        if (detail == null) {
            showWarning(
                    "Vui lòng chọn sản phẩm cần xóa.");
            return;
        }

        cartItems.remove(detail);
        refreshCartTable();
    }

    private OrderDetail getSelectedCartItem() {
        int selectedRow = cartTable.getSelectedRow();

        if (selectedRow < 0
                || selectedRow >= cartItems.size()) {
            return null;
        }

        return cartItems.get(selectedRow);
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);

        for (OrderDetail detail : cartItems) {
            cartTableModel.addRow(
                    new Object[] {
                            detail.getProductId(),
                            detail.getProductName(),
                            currencyFormat.format(
                                    detail.getUnitPrice()),
                            detail.getQuantity(),
                            currencyFormat.format(
                                    detail.getSubtotal())
                    });
        }

        refreshTotals();
    }

    private void refreshTotals() {
        BigDecimal subtotal = cartItems.stream()
                .map(OrderDetail::getSubtotal)
                .filter(value -> value != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        BigDecimal discount = parseDiscount(false);

        BigDecimal total = subtotal.subtract(discount);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        subtotalLabel.setText(
                currencyFormat.format(subtotal));

        totalLabel.setText(
                currencyFormat.format(total));
    }

    private BigDecimal parseDiscount(
            boolean showValidationMessage) {
        String value = discountField.getText().trim();

        if (value.isBlank()) {
            return BigDecimal.ZERO;
        }

        try {
            BigDecimal discount = new BigDecimal(value);

            if (discount.compareTo(
                    BigDecimal.ZERO) < 0) {
                if (showValidationMessage) {
                    showWarning(
                            "Giảm giá không được nhỏ hơn 0.");
                }

                return BigDecimal.ZERO;
            }

            return discount;

        } catch (NumberFormatException exception) {
            if (showValidationMessage) {
                showWarning(
                        "Giá trị giảm giá không hợp lệ.");
            }

            return BigDecimal.ZERO;
        }
    }

    private void createOrder() {
        if (cartItems.isEmpty()) {
            showWarning(
                    "Đơn hàng phải có ít nhất một sản phẩm.");
            return;
        }

        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            showError(
                    "Phiên đăng nhập không hợp lệ.");
            return;
        }

        OrderType orderType = (OrderType) orderTypeComboBox
                .getSelectedItem();

        CoffeeTable selectedTable = (CoffeeTable) tableComboBox
                .getSelectedItem();

        if (orderType == OrderType.DINE_IN
                && selectedTable == null) {
            showWarning(
                    "Vui lòng chọn bàn.");
            return;
        }

        BigDecimal discount = parseDiscount(true);

        BigDecimal subtotal = cartItems.stream()
                .map(OrderDetail::getSubtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        if (discount.compareTo(subtotal) > 0) {
            showWarning(
                    "Giảm giá không được lớn hơn tạm tính.");
            return;
        }

        Order order = new Order();

        order.setOrderCode(
                OrderCodeGenerator.generate());

        order.setCashierId(
                currentUser.getId());

        order.setOrderType(orderType);
        order.setStatus(OrderStatus.PENDING);

        order.setTableId(
                orderType == OrderType.DINE_IN
                        ? selectedTable.getId()
                        : null);

        order.setDiscount(discount);

        String note = noteArea.getText().trim();

        order.setNote(
                note.isBlank()
                        ? null
                        : note);

        List<OrderDetail> orderDetails = new ArrayList<>();

        for (OrderDetail item : cartItems) {
            OrderDetail detail = new OrderDetail();

            detail.setProductId(
                    item.getProductId());
            detail.setProductName(
                    item.getProductName());
            detail.setUnitPrice(
                    item.getUnitPrice());
            detail.setQuantity(
                    item.getQuantity());
            detail.setNote(item.getNote());
            detail.calculateSubtotal();

            orderDetails.add(detail);
        }

        order.setDetails(orderDetails);
        order.calculateTotals();

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận tạo đơn "
                        + order.getOrderCode()
                        + "?\nTổng tiền: "
                        + currencyFormat.format(
                                order.getTotalAmount()),
                "Xác nhận đơn hàng",
                JOptionPane.YES_NO_OPTION);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int orderId = orderDAO.create(order);

            JOptionPane.showMessageDialog(
                    this,
                    "Tạo đơn hàng thành công.\n"
                            + "ID: "
                            + orderId
                            + "\nMã đơn: "
                            + order.getOrderCode(),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            clearOrder();
            loadAvailableTables();

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void clearOrder() {
        cartItems.clear();
        cartTableModel.setRowCount(0);

        discountField.setText("0");
        noteArea.setText("");
        quantitySpinner.setValue(1);

        orderTypeComboBox.setSelectedItem(
                OrderType.DINE_IN);

        productTable.clearSelection();
        cartTable.clearSelection();

        refreshTotals();
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
    }
}