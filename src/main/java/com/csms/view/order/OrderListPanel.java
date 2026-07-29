package com.csms.view.order;

import com.csms.dao.OrderDAO;
import com.csms.entity.Order;
import com.csms.entity.OrderStatus;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import com.csms.view.payment.PaymentDialog;
import com.csms.dto.ReceiptData;
import com.csms.service.ReceiptService;
import com.csms.view.payment.ReceiptDialog;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class OrderListPanel extends JPanel {

    private final OrderDAO orderDAO;

    private final DefaultTableModel tableModel;
    private final JTable orderTable;

    private final JComboBox<OrderStatus> statusComboBox;

    private final NumberFormat currencyFormat;

    private final DateTimeFormatter dateFormatter;

    private final ReceiptService receiptService;

    public OrderListPanel() {
        orderDAO = new OrderDAO();

        currencyFormat = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("vi-VN"));

        dateFormatter = DateTimeFormatter.ofPattern(
                "dd/MM/yyyy HH:mm");

        tableModel = new DefaultTableModel(
                new Object[] {
                        "ID",
                        "Mã đơn",
                        "Loại đơn",
                        "Bàn",
                        "Nhân viên",
                        "Tổng tiền",
                        "Trạng thái",
                        "Ngày tạo"
                },
                0) {
            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {
                return false;
            }
        };

        receiptService = new ReceiptService();

        orderTable = new JTable(tableModel);

        statusComboBox = new JComboBox<>(
                OrderStatus.values());

        initializeComponents();
        loadOrders();
    }

    private void openPaymentDialog() {
        Integer orderId = getSelectedOrderId();

        if (orderId == null) {
            showWarning(
                    "Vui lòng chọn đơn hàng cần thanh toán.");
            return;
        }

        try {
            Optional<Order> optionalOrder = orderDAO.findById(orderId);

            if (optionalOrder.isEmpty()) {
                showError(
                        "Không tìm thấy đơn hàng.");
                loadOrders();
                return;
            }

            Order order = optionalOrder.get();

            if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {

                showWarning(
                        "Chỉ có thể thanh toán đơn đang ở trạng thái "
                                + "\"Chờ thanh toán\".");
                return;
            }

            JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(
                    this);

            PaymentDialog dialog = new PaymentDialog(
                    owner,
                    order);

            dialog.setVisible(true);

            if (dialog.isPaymentSuccessful()) {
                loadOrders();
            }

        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(0, 18));

        setBorder(
                BorderFactory.createEmptyBorder(
                        24,
                        24,
                        24,
                        24));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(
                new BorderLayout());
        new JButton("Thanh toán");

        JLabel titleLabel = new JLabel(
                "");

        titleLabel.setFont(
                titleLabel.getFont().deriveFont(
                        Font.BOLD,
                        24F));

        JPanel actionPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        8,
                        0));

        JButton refreshButton = new JButton("Làm mới");

        JButton detailButton = new JButton("Chi tiết");

        JButton updateButton = new JButton("Cập nhật trạng thái");

        JButton paymentButton = new JButton("Thanh toán");

        JButton receiptButton = new JButton("Xem hóa đơn");

        JButton cancelButton = new JButton("Hủy đơn");

        refreshButton.addActionListener(
                event -> loadOrders());

        detailButton.addActionListener(
                event -> showOrderDetail());

        updateButton.addActionListener(
                event -> updateStatus());

        paymentButton.addActionListener(
                event -> openPaymentDialog());

        receiptButton.addActionListener(
                event -> showReceipt());

        cancelButton.addActionListener(
                event -> cancelOrder());

        actionPanel.add(
                new JLabel("Trạng thái mới:"));
        actionPanel.add(statusComboBox);
        actionPanel.add(updateButton);
        actionPanel.add(paymentButton);
        actionPanel.add(receiptButton);
        actionPanel.add(detailButton);
        actionPanel.add(cancelButton);
        actionPanel.add(refreshButton);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(actionPanel, BorderLayout.EAST);

        return panel;
    }

    private void showReceipt() {
        Integer orderId = getSelectedOrderId();

        if (orderId == null) {
            showWarning(
                    "Vui lòng chọn đơn hàng cần xem hóa đơn.");
            return;
        }

        try {
            Optional<Order> optionalOrder = orderDAO.findById(orderId);

            if (optionalOrder.isEmpty()) {
                showError(
                        "Không tìm thấy đơn hàng.");
                return;
            }

            Order order = optionalOrder.get();

            if (order.getStatus() != OrderStatus.PAID
                    && order.getStatus() != OrderStatus.PAID) {

                showWarning(
                        "Chỉ có thể xem hóa đơn của đơn đã thanh toán.");
                return;
            }

            ReceiptData receiptData = receiptService.getReceiptData(
                    orderId);

            JFrame owner = (JFrame) SwingUtilities
                    .getWindowAncestor(
                            this);

            ReceiptDialog dialog = new ReceiptDialog(
                    owner,
                    receiptData);

            dialog.setVisible(true);

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private JScrollPane createTablePanel() {
        orderTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        orderTable.setRowHeight(34);

        orderTable.getTableHeader()
                .setReorderingAllowed(false);

        orderTable.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent event) {
                        if (event.getClickCount() == 2) {
                            showOrderDetail();
                        }
                    }
                });

        return new JScrollPane(orderTable);
    }

    private void loadOrders() {
        try {
            List<Order> orders = orderDAO.findAll();

            tableModel.setRowCount(0);

            for (Order order : orders) {
                tableModel.addRow(
                        new Object[] {
                                order.getId(),
                                order.getOrderCode(),
                                getOrderTypeText(order),
                                getTableText(order),
                                order.getCashierName(),
                                currencyFormat.format(
                                        order.getTotalAmount()),
                                getStatusText(
                                        order.getStatus()),
                                order.getCreatedAt()
                                        .format(dateFormatter)
                        });
            }

        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void updateStatus() {
        Integer orderId = getSelectedOrderId();

        if (orderId == null) {
            showWarning(
                    "Vui lòng chọn đơn hàng.");
            return;
        }

        OrderStatus newStatus = (OrderStatus) statusComboBox
                .getSelectedItem();

        if (newStatus == null) {
            return;
        }

        if (newStatus == OrderStatus.CANCELLED) {
            cancelOrder();
            return;
        }

        try {
            Optional<Order> optionalOrder = orderDAO.findById(orderId);

            if (optionalOrder.isEmpty()) {
                showError(
                        "Không tìm thấy đơn hàng.");
                loadOrders();
                return;
            }

            Order order = optionalOrder.get();

            if (!isValidTransition(
                    order.getStatus(),
                    newStatus)) {
                showWarning(
                        "Không thể chuyển trạng thái từ "
                                + getStatusText(
                                        order.getStatus())
                                + " sang "
                                + getStatusText(newStatus)
                                + ".");
                return;
            }

            if (order.getStatus() == OrderStatus.PENDING_PAYMENT
                    && newStatus == OrderStatus.PAID) {

                showWarning(
                        "Vui lòng sử dụng nút Thanh toán "
                                + "để chuyển đơn sang Đã thanh toán.");
                return;
            }

            boolean updated = orderDAO.updateStatus(
                    orderId,
                    newStatus);

            if (!updated) {
                showError(
                        "Không thể cập nhật đơn hàng.");
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Cập nhật trạng thái thành công.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            loadOrders();

        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private boolean isValidTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            return false;
        }

        return switch (currentStatus) {
            case PENDING ->
                newStatus == OrderStatus.PREPARED;

            case PREPARED ->
                newStatus == OrderStatus.SERVED;

            case SERVED ->
                newStatus == OrderStatus.PENDING_PAYMENT;

            case PENDING_PAYMENT -> false;

            case PAID ->
                newStatus == OrderStatus.SERVED;

            case CANCELLED -> false;
        };
    }

    private void cancelOrder() {
        Integer orderId = getSelectedOrderId();

        if (orderId == null) {
            showWarning(
                    "Vui lòng chọn đơn hàng cần hủy.");
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn hủy đơn hàng này?\n"
                        + "Tồn kho sẽ được hoàn trả.",
                "Xác nhận hủy đơn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean cancelled = orderDAO.cancelOrder(orderId);

            if (!cancelled) {
                showError(
                        "Không tìm thấy đơn hàng.");
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Hủy đơn hàng thành công.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            loadOrders();

        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void showOrderDetail() {
        Integer orderId = getSelectedOrderId();

        if (orderId == null) {
            showWarning(
                    "Vui lòng chọn đơn hàng.");
            return;
        }

        try {
            Optional<Order> optionalOrder = orderDAO.findById(orderId);

            if (optionalOrder.isEmpty()) {
                showError(
                        "Không tìm thấy đơn hàng.");
                return;
            }

            Order order = optionalOrder.get();

            StringBuilder content = new StringBuilder();

            content.append("Mã đơn: ")
                    .append(order.getOrderCode())
                    .append("\n");

            content.append("Nhân viên: ")
                    .append(order.getCashierName())
                    .append("\n");

            content.append("Bàn: ")
                    .append(getTableText(order))
                    .append("\n");

            content.append("Trạng thái: ")
                    .append(
                            getStatusText(
                                    order.getStatus()))
                    .append("\n\n");

            for (var detail : order.getDetails()) {
                content.append("- ")
                        .append(detail.getProductName())
                        .append(" x ")
                        .append(detail.getQuantity())
                        .append(" = ")
                        .append(
                                currencyFormat.format(
                                        detail.getSubtotal()))
                        .append("\n");
            }

            content.append("\nTạm tính: ")
                    .append(
                            currencyFormat.format(
                                    order.getSubtotal()));

            content.append("\nGiảm giá: ")
                    .append(
                            currencyFormat.format(
                                    order.getDiscount()));

            content.append("\nTổng tiền: ")
                    .append(
                            currencyFormat.format(
                                    order.getTotalAmount()));

            JOptionPane.showMessageDialog(
                    this,
                    content.toString(),
                    "Chi tiết đơn hàng",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private Integer getSelectedOrderId() {
        int selectedRow = orderTable.getSelectedRow();

        if (selectedRow < 0) {
            return null;
        }

        return Integer.parseInt(
                tableModel.getValueAt(
                        selectedRow,
                        0).toString());
    }

    private String getOrderTypeText(Order order) {
        return switch (order.getOrderType()) {
            case DINE_IN -> "Tại bàn";
            case TAKE_AWAY -> "Mang đi";
        };
    }

    private String getTableText(Order order) {
        return order.getTableName() == null
                ? "-"
                : order.getTableName();
    }

    private String getStatusText(
            OrderStatus status) {
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case PREPARED -> "Đang chuẩn bị";
            case SERVED -> "Chờ thanh toán";
            case PENDING_PAYMENT -> "Đã thanh toán";
            case PAID -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
        };
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