package com.csms.view.waiter.dialog;

import com.csms.dao.OrderDAO;
import com.csms.entity.Order;
import com.csms.entity.OrderDetail;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class OrderDetailDialog extends JDialog {

    private final int orderId;
    private final OrderDAO orderDAO;

    private final JLabel orderCodeLabel;
    private final JLabel tableLabel;
    private final JLabel statusLabel;
    private final JLabel waiterLabel;
    private final JLabel totalLabel;
    private final JLabel loadingLabel;

    private final DefaultTableModel tableModel;

    public OrderDetailDialog(
            Window owner,
            int orderId) {

        super(
                owner,
                "Chi tiết đơn hàng",
                ModalityType.APPLICATION_MODAL);

        this.orderId = orderId;
        this.orderDAO = new OrderDAO();

        orderCodeLabel = new JLabel("-");
        tableLabel = new JLabel("-");
        statusLabel = new JLabel("-");
        waiterLabel = new JLabel("-");
        totalLabel = new JLabel("0 đ");
        loadingLabel = new JLabel("Đang tải dữ liệu...");

        tableModel = new DefaultTableModel(
                new Object[] {
                        "Món",
                        "Đơn giá",
                        "Số lượng",
                        "Thành tiền",
                        "Ghi chú"
                },
                0) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };

        initializeComponents();
        loadOrder();
    }

    private void initializeComponents() {
        setDefaultCloseOperation(
                DISPOSE_ON_CLOSE);

        setMinimumSize(
                new Dimension(
                        850,
                        600));

        JPanel rootPanel = new JPanel(
                new BorderLayout(
                        0,
                        16));

        rootPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        24,
                        20,
                        24));

        JLabel titleLabel = new JLabel(
                "CHI TIẾT ĐƠN HÀNG");

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        22));

        JPanel headerPanel = new JPanel(
                new BorderLayout());

        headerPanel.add(
                titleLabel,
                BorderLayout.WEST);

        headerPanel.add(
                loadingLabel,
                BorderLayout.EAST);

        JPanel informationPanel = new JPanel(
                new java.awt.GridLayout(
                        2,
                        4,
                        12,
                        12));

        informationPanel.add(
                new JLabel("Mã đơn:"));

        informationPanel.add(
                orderCodeLabel);

        informationPanel.add(
                new JLabel("Bàn:"));

        informationPanel.add(
                tableLabel);

        informationPanel.add(
                new JLabel("Trạng thái:"));

        informationPanel.add(
                statusLabel);

        informationPanel.add(
                new JLabel("Waiter:"));

        informationPanel.add(
                waiterLabel);

        JTable detailTable = new JTable(
                tableModel);

        detailTable.setRowHeight(36);
        detailTable.setFillsViewportHeight(true);

        JPanel centerPanel = new JPanel(
                new BorderLayout(
                        0,
                        14));

        centerPanel.add(
                informationPanel,
                BorderLayout.NORTH);

        centerPanel.add(
                new JScrollPane(
                        detailTable),
                BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(
                new BorderLayout());

        JPanel totalPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT));

        JLabel totalTitle = new JLabel(
                "Tổng tiền:");

        totalTitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        16));

        totalLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        18));

        totalPanel.add(totalTitle);
        totalPanel.add(totalLabel);

        JButton closeButton = new JButton(
                "Đóng");

        closeButton.addActionListener(
                event -> dispose());

        footerPanel.add(
                closeButton,
                BorderLayout.WEST);

        footerPanel.add(
                totalPanel,
                BorderLayout.EAST);

        rootPanel.add(
                headerPanel,
                BorderLayout.NORTH);

        rootPanel.add(
                centerPanel,
                BorderLayout.CENTER);

        rootPanel.add(
                footerPanel,
                BorderLayout.SOUTH);

        setContentPane(rootPanel);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void loadOrder() {
        SwingWorker<Order, Void> worker = new SwingWorker<>() {

            @Override
            protected Order doInBackground() {
                return orderDAO
                        .findById(orderId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Không tìm thấy đơn hàng."));
            }

            @Override
            protected void done() {
                try {
                    displayOrder(
                            get());

                    loadingLabel.setText(" ");

                } catch (InterruptedException exception) {

                    Thread.currentThread()
                            .interrupt();

                    loadingLabel.setText(
                            "Đã dừng tải dữ liệu");

                } catch (ExecutionException exception) {

                    Throwable cause = exception.getCause() == null
                            ? exception
                            : exception.getCause();

                    loadingLabel.setText(
                            cause.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void displayOrder(
            Order order) {

        NumberFormat currencyFormat = NumberFormat.getNumberInstance(
                new Locale(
                        "vi",
                        "VN"));

        orderCodeLabel.setText(
                order.getOrderCode());

        tableLabel.setText(
                order.getTableName() == null
                        ? "-"
                        : "Bàn "
                                + order.getTableName());

        statusLabel.setText(
                order.getStatus() == null
                        ? "-"
                        : order.getStatus().name());

        waiterLabel.setText(
                order.getCashierName() == null
                        ? "-"
                        : order.getCashierName());

        totalLabel.setText(
                currencyFormat.format(
                        order.getTotalAmount())
                        + " đ");

        tableModel.setRowCount(0);

        if (order.getDetails() == null) {
            return;
        }

        for (OrderDetail detail : order.getDetails()) {

            tableModel.addRow(
                    new Object[] {
                            detail.getProductName(),
                            currencyFormat.format(
                                    detail.getUnitPrice())
                                    + " đ",
                            detail.getQuantity(),
                            currencyFormat.format(
                                    detail.getSubtotal())
                                    + " đ",
                            detail.getNote() == null
                                    ? ""
                                    : detail.getNote()
                    });
        }
    }
}