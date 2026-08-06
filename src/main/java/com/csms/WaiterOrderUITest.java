package com.csms;

import com.csms.dto.OrderResult;
import com.csms.entity.RoleName;
import com.csms.entity.TableDashboardItem;
import com.csms.service.TableOrderService;
import com.csms.view.operation.dashboard.TableDashboardPanel;
import com.csms.view.waiter.dialog.CreateOrderDialog;
import com.csms.view.waiter.dialog.OrderDetailDialog;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class WaiterOrderUITest {

    private static TableDashboardPanel dashboardPanel;

    private static final TableOrderService tableOrderService = new TableOrderService();

    private WaiterOrderUITest() {
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(
                    "CSMS - Waiter Dashboard");

            frame.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE);

            dashboardPanel = new TableDashboardPanel(
                    RoleName.WAITER,
                    item -> openCreateOrderDialog(
                            frame,
                            item),
                    item -> openOrderDetailDialog(
                            frame,
                            item));

            frame.setContentPane(
                    dashboardPanel);

            frame.setSize(
                    1400,
                    850);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static void openCreateOrderDialog(
            JFrame owner,
            TableDashboardItem tableItem) {

        CreateOrderDialog dialog = new CreateOrderDialog(
                owner,
                tableItem,
                request -> {
                    OrderResult result = tableOrderService
                            .createAndSendOrder(
                                    request);

                    JOptionPane.showMessageDialog(
                            owner,
                            result.message()
                                    + "\nMã đơn: "
                                    + result.orderCode(),
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                });

        dialog.setVisible(true);

        if (dialog.isSubmitted()
                && dashboardPanel != null) {

            dashboardPanel.loadDashboard();
        }
    }

    private static void openOrderDetailDialog(
            JFrame owner,
            TableDashboardItem tableItem) {

        if (tableItem == null
                || tableItem.getOrderId() == null) {

            JOptionPane.showMessageDialog(
                    owner,
                    "Không tìm thấy đơn hàng của bàn.",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);

            return;
        }

        OrderDetailDialog dialog = new OrderDetailDialog(
                owner,
                tableItem.getOrderId());

        dialog.setVisible(true);

        /*
         * Sau khi dialog đóng, tải lại Dashboard để nhận
         * trạng thái mới như PREPARED, SERVED, PAID...
         */
        if (dashboardPanel != null) {
            dashboardPanel.loadDashboard();
        }
    }
}