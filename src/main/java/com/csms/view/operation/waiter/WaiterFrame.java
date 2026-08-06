package com.csms.view.operation.waiter;

import com.csms.dto.OrderResult;
import com.csms.entity.TableDashboardItem;
import com.csms.service.TableOrderService;
import com.csms.view.operation.common.DashboardMode;
import com.csms.view.operation.common.TableDashboardListener;
import com.csms.view.operation.dashboard.TableDashboardPanel;
import com.csms.view.operation.dialog.TableDetailDialog;
import com.csms.view.waiter.dialog.CreateOrderDialog;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;

public class WaiterFrame extends JFrame {

    private static final String TABLE_DASHBOARD_CARD = "tableDashboard";

    private final TableOrderService tableOrderService;

    private CardLayout cardLayout;
    private JPanel contentPanel;

    private TableDashboardPanel tableDashboardPanel;

    public WaiterFrame() {
        tableOrderService = new TableOrderService();

        initializeFrame();
        initializeContent();
    }

    private void initializeFrame() {
        setTitle("CSMS - Waiter");

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE);

        setLayout(
                new BorderLayout());

        setSize(
                1450,
                880);

        setLocationRelativeTo(null);
    }

    private void initializeContent() {
        cardLayout = new CardLayout();

        contentPanel = new JPanel(cardLayout);

        initializeTableDashboard();

        contentPanel.add(
                tableDashboardPanel,
                TABLE_DASHBOARD_CARD);

        add(
                contentPanel,
                BorderLayout.CENTER);

        showTableDashboard();
    }

    private void initializeTableDashboard() {
        tableDashboardPanel = new TableDashboardPanel(
                DashboardMode.WAITER,
                new TableDashboardListener() {

                    @Override
                    public void onCreateOrderRequested(
                            TableDashboardItem tableItem) {
                        openCreateOrderDialog(
                                tableItem);
                    }

                    @Override
                    public void onViewTableRequested(
                            TableDashboardItem tableItem) {
                        openTableDetailDialog(
                                tableItem);
                    }
                });
    }

    public void showTableDashboard() {
        cardLayout.show(
                contentPanel,
                TABLE_DASHBOARD_CARD);

        tableDashboardPanel.loadDashboard();
    }

    private void openCreateOrderDialog(
            TableDashboardItem tableItem) {
        CreateOrderDialog dialog = new CreateOrderDialog(
                this,
                tableItem,
                request -> {
                    OrderResult result = tableOrderService
                            .createAndSendOrder(
                                    request);

                    JOptionPane.showMessageDialog(
                            this,
                            result.message()
                                    + "\nMã đơn: "
                                    + result.orderCode(),
                            "Tạo đơn thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                });

        dialog.setVisible(true);

        /*
         * Chỉ reload Dashboard khi dialog đã gửi đơn thành công.
         */
        if (dialog.isSubmitted()) {
            tableDashboardPanel
                    .loadDashboard();
        }
    }

    private void openTableDetailDialog(
            TableDashboardItem tableItem) {
        TableDetailDialog dialog = new TableDetailDialog(
                this,
                tableItem.getTableId(),
                DashboardMode.WAITER,
                tableDashboardPanel::loadDashboard);

        dialog.setVisible(true);

        /*
         * Có thể reload thêm sau khi dialog đóng,
         * phòng trường hợp bên trong vừa cập nhật dữ liệu.
         */
        tableDashboardPanel
                .loadDashboard();
    }
}