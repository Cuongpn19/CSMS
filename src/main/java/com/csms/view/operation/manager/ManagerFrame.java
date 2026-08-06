package com.csms.view.operation.manager;

import com.csms.entity.TableDashboardItem;
import com.csms.view.operation.common.DashboardMode;
import com.csms.view.operation.common.TableDashboardListener;
import com.csms.view.operation.dashboard.TableDashboardPanel;
import com.csms.view.operation.dialog.TableDetailDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

public class ManagerFrame extends JFrame {

    private static final String TABLE_DASHBOARD_CARD = "tableDashboard";

    private static final String PROBLEM_MANAGEMENT_CARD = "problemManagement";

    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    private TableDashboardPanel tableDashboardPanel;

    public ManagerFrame() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        initializeFrame();
        initializeContent();
    }

    private void initializeFrame() {
        setTitle("CSMS - Manager");

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE);

        setLayout(
                new BorderLayout());

        setSize(
                1450,
                880);

        setMinimumSize(
                new Dimension(
                        1150,
                        700));

        setLocationRelativeTo(null);
    }

    private void initializeContent() {
        add(
                createSidebarPanel(),
                BorderLayout.WEST);

        initializeDashboard();

        contentPanel.add(
                tableDashboardPanel,
                TABLE_DASHBOARD_CARD);

        /*
         * Sau này thay placeholder bằng:
         *
         * new ProblemManagementPanel()
         */
        contentPanel.add(
                createPlaceholderPanel(
                        "PROBLEM MANAGEMENT"),
                PROBLEM_MANAGEMENT_CARD);

        add(
                contentPanel,
                BorderLayout.CENTER);

        showTableDashboard();
    }

    private void initializeDashboard() {
        tableDashboardPanel = new TableDashboardPanel(
                DashboardMode.MANAGER,
                new TableDashboardListener() {

                    @Override
                    public void onCreateOrderRequested(
                            TableDashboardItem tableItem) {
                        /*
                         * Manager không được tạo đơn.
                         *
                         * Với DashboardMode.MANAGER,
                         * TableDashboardPanel sẽ không gọi
                         * callback này khi click bàn trống.
                         */
                        openTableMonitor(
                                tableItem);
                    }

                    @Override
                    public void onViewTableRequested(
                            TableDashboardItem tableItem) {
                        openTableMonitor(
                                tableItem);
                    }
                });
    }

    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel(
                new BorderLayout());

        sidebarPanel.setPreferredSize(
                new Dimension(
                        230,
                        0));

        sidebarPanel.setBackground(
                new Color(
                        15,
                        23,
                        42));

        sidebarPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        14,
                        20,
                        14));

        JLabel titleLabel = new JLabel(
                "CSMS MANAGER");

        titleLabel.setForeground(
                Color.WHITE);

        titleLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        20));

        JPanel menuPanel = new JPanel(
                new GridLayout(
                        0,
                        1,
                        0,
                        10));

        menuPanel.setOpaque(false);

        JButton dashboardButton = createMenuButton(
                "Dashboard bàn");

        JButton problemButton = createMenuButton(
                "Problem Management");

        dashboardButton.addActionListener(
                event -> showTableDashboard());

        problemButton.addActionListener(
                event -> cardLayout.show(
                        contentPanel,
                        PROBLEM_MANAGEMENT_CARD));

        menuPanel.add(
                dashboardButton);

        menuPanel.add(
                problemButton);

        sidebarPanel.add(
                titleLabel,
                BorderLayout.NORTH);

        sidebarPanel.add(
                menuPanel,
                BorderLayout.CENTER);

        return sidebarPanel;
    }

    private JButton createMenuButton(
            String text) {
        JButton button = new JButton(text);

        button.setFocusPainted(false);

        button.setHorizontalAlignment(
                JButton.LEFT);

        button.setBackground(
                new Color(
                        30,
                        41,
                        59));

        button.setForeground(
                Color.WHITE);

        button.setBorder(
                BorderFactory.createEmptyBorder(
                        12,
                        14,
                        12,
                        14));

        return button;
    }

    private JPanel createPlaceholderPanel(
            String title) {
        JPanel panel = new JPanel(
                new BorderLayout());

        panel.setBackground(
                new Color(
                        245,
                        247,
                        251));

        JLabel label = new JLabel(
                title,
                JLabel.CENTER);

        label.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        25));

        panel.add(
                label,
                BorderLayout.CENTER);

        return panel;
    }

    public void showTableDashboard() {
        cardLayout.show(
                contentPanel,
                TABLE_DASHBOARD_CARD);

        tableDashboardPanel
                .loadDashboard();
    }

    private void openTableMonitor(
            TableDashboardItem tableItem) {
        if (tableItem == null) {
            return;
        }

        TableDetailDialog dialog = new TableDetailDialog(
                this,
                tableItem.getTableId(),
                DashboardMode.MANAGER,
                tableDashboardPanel::loadDashboard);

        dialog.setVisible(true);
    }
}