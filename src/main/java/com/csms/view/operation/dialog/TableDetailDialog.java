package com.csms.view.operation.dialog;

import com.csms.dto.TableOrderDetailView;
import com.csms.dto.TableOrderItemView;
import com.csms.entity.OrderStatus;
import com.csms.service.OrderCancelRequestService;
import com.csms.service.TableDetailService;
import com.csms.service.WaiterOrderService;
import com.csms.view.operation.common.DashboardMode;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class TableDetailDialog extends JDialog {

    private final int tableId;
    private final DashboardMode dashboardMode;
    private final Runnable refreshHandler;

    private final TableDetailService tableDetailService;

    private final WaiterOrderService waiterOrderService;

    private final OrderCancelRequestService orderCancelRequestService;

    private final DefaultTableModel tableModel;

    private final JTable itemTable;

    private final JLabel tableLabel;
    private final JLabel orderCodeLabel;
    private final JLabel waiterLabel;
    private final JLabel statusLabel;
    private final JLabel subtotalLabel;
    private final JLabel vatLabel;
    private final JLabel totalLabel;

    private final JButton updateButton;
    private final JButton serveButton;
    private final JButton cancelButton;
    private final JButton closeButton;

    private TableOrderDetailView detail;

    public TableDetailDialog(
            Window owner,
            int tableId,
            DashboardMode dashboardMode,
            Runnable refreshHandler) {
        super(
                owner,
                "Chi tiết bàn",
                ModalityType.APPLICATION_MODAL);

        this.tableId = tableId;
        this.dashboardMode = dashboardMode;
        this.refreshHandler = refreshHandler;

        tableDetailService = new TableDetailService();

        waiterOrderService = new WaiterOrderService();

        orderCancelRequestService = new OrderCancelRequestService();

        tableModel = createTableModel();

        itemTable = new JTable(tableModel);

        tableLabel = new JLabel("-");
        orderCodeLabel = new JLabel("-");
        waiterLabel = new JLabel("-");
        statusLabel = new JLabel("-");
        subtotalLabel = new JLabel("0 ₫");
        vatLabel = new JLabel("0 ₫");
        totalLabel = new JLabel("0 ₫");

        updateButton = new JButton("Cập nhật món");

        serveButton = new JButton("Đã phục vụ");

        cancelButton = new JButton("Yêu cầu hủy");

        closeButton = new JButton("Đóng");

        initializeComponents();
        registerEvents();
        applyPermissions();
        loadDetail();
    }

    private void applyPermissions() {
        updateButton.setVisible(
                dashboardMode.canUpdateOrder());

        serveButton.setVisible(
                dashboardMode.canServeOrder());

        cancelButton.setVisible(
                dashboardMode.canRequestCancel());
    }

    private void updateActionState() {
        if (detail == null
                || detail.orderId() == null) {

            updateButton.setEnabled(false);
            serveButton.setEnabled(false);
            cancelButton.setEnabled(false);

            return;
        }

        OrderStatus status = detail.orderStatus();

        updateButton.setEnabled(
                dashboardMode.canUpdateOrder()
                        && status != null
                        && status.canWaiterAddItem());

        serveButton.setEnabled(
                dashboardMode.canServeOrder()
                        && status == OrderStatus.PREPARED);

        cancelButton.setEnabled(
                dashboardMode.canRequestCancel()
                        && status == OrderStatus.IN_PROGRESS);
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(
                new Object[] {
                        "Món",
                        "Số lượng",
                        "Đơn giá",
                        "VAT",
                        "Thành tiền",
                        "Trạng thái",
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
    }

    private void displayDetail(
            TableOrderDetailView detail) {
        this.detail = detail;

        tableLabel.setText(
                String.format(
                        "BÀN %02d",
                        detail.tableNumber()));

        if (detail.orderId() == null) {
            displayAvailableTable();
            return;
        }

        orderCodeLabel.setText(
                detail.orderCode() == null
                        ? "Đơn #"
                                + detail.orderId()
                        : detail.orderCode());

        waiterLabel.setText(
                detail.waiterName() == null
                        ? "-"
                        : detail.waiterName());

        statusLabel.setText(
                detail.orderStatus() == null
                        ? "-"
                        : detail.orderStatus()
                                .getDisplayName());

        subtotalLabel.setText(
                formatMoney(detail.subtotal()));

        vatLabel.setText(
                formatMoney(detail.vatAmount()));

        totalLabel.setText(
                formatMoney(detail.totalAmount()));

        tableModel.setRowCount(0);

        for (TableOrderItemView item : detail.items()) {

            tableModel.addRow(
                    new Object[] {
                            item.productName(),
                            item.quantity(),
                            formatMoney(
                                    item.unitPrice()),
                            formatRate(
                                    item.vatRate()),
                            formatMoney(
                                    item.subtotal()
                                            .add(
                                                    safeAmount(
                                                            item.vatAmount()))),
                            item.status()
                                    .getDisplayName(),
                            item.note()
                    });
        }

        updateActionState();
    }

    private void displayAvailableTable() {
        orderCodeLabel.setText(
                "Chưa có đơn hàng");

        waiterLabel.setText("-");
        statusLabel.setText("Bàn trống");

        subtotalLabel.setText("0 ₫");
        vatLabel.setText("0 ₫");
        totalLabel.setText("0 ₫");

        tableModel.setRowCount(0);

        updateActionState();
    }

    private void loadDetail() {
        SwingWorker<TableOrderDetailView, Void> worker = new SwingWorker<>() {

            @Override
            protected TableOrderDetailView doInBackground() {

                return tableDetailService
                        .getByTableId(
                                tableId);
            }

            @Override
            protected void done() {
                try {
                    displayDetail(get());

                } catch (InterruptedException exception) {
                    Thread.currentThread()
                            .interrupt();

                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause() == null
                            ? exception
                            : exception.getCause();

                    javax.swing.JOptionPane
                            .showMessageDialog(
                                    TableDetailDialog.this,
                                    cause.getMessage(),
                                    "Lỗi",
                                    javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private String formatMoney(
            BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag(
                        "vi-VN"));

        return formatter.format(
                safeAmount(amount));
    }

    private String formatRate(
            BigDecimal rate) {
        return safeAmount(rate)
                .stripTrailingZeros()
                .toPlainString()
                + "%";
    }

    private BigDecimal safeAmount(
            BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private void initializeComponents() {
        setMinimumSize(
                new Dimension(950, 650));

        setLayout(new BorderLayout());

        /*
         * Tạo:
         * NORTH → thông tin bàn và đơn
         * CENTER → JTable danh sách món
         * SOUTH → tổng tiền và các nút
         */

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void registerEvents() {
        closeButton.addActionListener(
                event -> dispose());

        /*
         * updateButton, serveButton và cancelButton
         * sẽ nối service ở bước nghiệp vụ tiếp theo.
         */
    }

    private void markOrderServed() {
        if (detail == null
                || detail.orderId() == null) {

            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận đã bưng các món pha xong ra bàn?",
                "Xác nhận phục vụ",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {

            return;
        }

        try {
            waiterOrderService
                    .markOrderServed(
                            detail.orderId());

            JOptionPane.showMessageDialog(
                    this,
                    "Đã cập nhật món sang trạng thái phục vụ.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            loadDetail();

            if (refreshHandler != null) {
                refreshHandler.run();
            }

        } catch (
                IllegalArgumentException
                | IllegalStateException
                | SecurityException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void requestOrderCancellation() {
        if (detail == null
                || detail.orderId() == null) {

            return;
        }

        String reason = JOptionPane.showInputDialog(
                this,
                "Nhập lý do yêu cầu hủy đơn:",
                "Yêu cầu hủy đơn",
                JOptionPane.WARNING_MESSAGE);

        if (reason == null) {
            return;
        }

        try {
            orderCancelRequestService
                    .requestCancellation(
                            detail.orderId(),
                            reason);

            JOptionPane.showMessageDialog(
                    this,
                    "Đã gửi yêu cầu hủy sang Manager.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            loadDetail();

            if (refreshHandler != null) {
                refreshHandler.run();
            }

        } catch (
                IllegalArgumentException
                | IllegalStateException
                | SecurityException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}