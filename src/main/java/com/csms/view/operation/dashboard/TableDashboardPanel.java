package com.csms.view.operation.dashboard;

import com.csms.dto.TableDashboardSummary;
// import com.csms.entity.RoleName;
import com.csms.entity.TableDashboardItem;
import com.csms.entity.TableStatus;
import com.csms.service.CoffeeTableService;
// import com.csms.service.TableOrderService;
import com.csms.view.operation.common.StatisticCard;

import com.csms.view.operation.common.DashboardMode;
import com.csms.view.operation.common.TableDashboardListener;

import javax.swing.BorderFactory;
// import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.Timer;
import java.util.concurrent.ExecutionException;
// import java.util.function.Consumer;

public class TableDashboardPanel extends JPanel {

    private final CoffeeTableService coffeeTableService;
    private final DashboardMode dashboardMode;
    private final TableDashboardListener dashboardListener;

    private final StatisticCard totalCard;
    private final StatisticCard availableCard;
    private final StatisticCard inProgressCard;
    private final StatisticCard preparedCard;
    private final StatisticCard servedCard;

    private final JTextField searchField;
    // private final JButton refreshButton;

    private boolean loadingDashboard;

    private final TableGridPanel tableGridPanel;
    private static final int AUTO_REFRESH_DELAY = 3000;
    private final javax.swing.Timer autoRefreshTimer;

    private final JLabel resultLabel;
    private final JLabel loadingLabel;

    private List<TableDashboardItem> allItems;

    public TableDashboardPanel(
            DashboardMode dashboardMode,
            TableDashboardListener dashboardListener) {

        if (dashboardMode == null) {
            throw new IllegalArgumentException(
                    "DashboardMode không được để trống.");
        }

        if (dashboardListener == null) {
            throw new IllegalArgumentException(
                    "TableDashboardListener không được để trống.");
        }

        this.coffeeTableService = new CoffeeTableService();

        this.dashboardMode = dashboardMode;

        this.dashboardListener = dashboardListener;

        this.totalCard = new StatisticCard(
                "Tổng số bàn",
                "0",
                new Color(71, 85, 105));

        this.availableCard = new StatisticCard(
                "Bàn trống",
                "0",
                new Color(100, 116, 139));

        this.inProgressCard = new StatisticCard(
                "Đang pha chế",
                "0",
                new Color(37, 99, 235));

        this.preparedCard = new StatisticCard(
                "Đã pha xong",
                "0",
                new Color(219, 39, 119));

        this.servedCard = new StatisticCard(
                "Đã phục vụ",
                "0",
                new Color(22, 163, 74));

        this.searchField = new JTextField();

        this.tableGridPanel = new TableGridPanel();

        this.resultLabel = new JLabel("0 bàn");

        this.loadingLabel = new JLabel(" ");

        this.allItems = new ArrayList<>();

        initializeComponents();
        registerEvents();

        autoRefreshTimer = new Timer(
                AUTO_REFRESH_DELAY,
                event -> refreshDashboardSilently());

        autoRefreshTimer.setRepeats(true);
        autoRefreshTimer.start();

        loadDashboard();
    }

    private void displayTables(
            java.util.List<TableDashboardItem> items) {
        tableGridPanel.displayTables(
                items,
                dashboardMode,
                this::handleTableClick);
    }

    private void handleAvailableTable(
            TableDashboardItem item) {

        if (!dashboardMode.canCreateOrder()) {

            if (dashboardListener == null) {
                showWarning(
                        "Chưa cấu hình chức năng xem thông tin bàn.");
                return;
            }

            dashboardListener
                    .onViewTableRequested(item);

            return;
        }

        try {
            coffeeTableService
                    .validateCanCreateOrder(
                            item.getTableId());

            dashboardListener
                    .onCreateOrderRequested(item);

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {

            showWarning(
                    exception.getMessage());

            loadDashboard();
        }
    }

    private void showWarning(
            String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
    }

    private void initializeComponents() {
        setLayout(
                new BorderLayout(0, 18));

        setBackground(
                new Color(245, 247, 251));

        setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        22,
                        20,
                        22));

        add(
                createHeaderSection(),
                BorderLayout.NORTH);

        add(
                createTableSection(),
                BorderLayout.CENTER);
    }

    private void refreshDashboardSilently() {
        if (!isShowing()
                || loadingDashboard) {
            return;
        }

        loadDashboard();
    }

    private JPanel createHeaderSection() {
        JPanel wrapper = new JPanel(
                new BorderLayout(0, 18));

        wrapper.setOpaque(false);

        wrapper.add(
                createTitlePanel(),
                BorderLayout.NORTH);

        wrapper.add(
                createStatisticsPanel(),
                BorderLayout.CENTER);

        wrapper.add(
                createSearchPanel(),
                BorderLayout.SOUTH);

        return wrapper;
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(
                new BorderLayout());

        panel.setOpaque(false);

        JPanel textPanel = new JPanel(
                new BorderLayout(0, 4));

        textPanel.setOpaque(false);

        JLabel subtitleLabel = new JLabel(
                "Theo dõi trạng thái và hoạt động của 30 bàn");

        subtitleLabel.setForeground(
                new Color(105, 113, 128));

        textPanel.add(
                subtitleLabel,
                BorderLayout.SOUTH);

        loadingLabel.setForeground(
                new Color(37, 99, 235));

        panel.add(
                textPanel,
                BorderLayout.WEST);

        panel.add(
                loadingLabel,
                BorderLayout.EAST);

        return panel;
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(
                new GridLayout(
                        1,
                        5,
                        12,
                        0));

        panel.setOpaque(false);

        panel.add(totalCard);
        panel.add(availableCard);
        panel.add(inProgressCard);
        panel.add(preparedCard);
        panel.add(servedCard);

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        0));

        panel.setOpaque(false);

        searchField.setPreferredSize(
                new Dimension(280, 38));

        searchField.putClientProperty(
                "JTextField.placeholderText",
                "Tìm bàn theo số...");

        // refreshButton.setPreferredSize(
        // new Dimension(100, 38));

        panel.add(
                new JLabel("Tìm kiếm:"));

        panel.add(searchField);
        // panel.add(refreshButton);
        panel.add(resultLabel);

        return panel;
    }

    private JPanel createTableSection() {
        JPanel panel = new JPanel(
                new BorderLayout());

        panel.setBackground(
                Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(
                                        226,
                                        230,
                                        237)),
                        BorderFactory.createEmptyBorder(
                                12,
                                12,
                                12,
                                12)));

        JScrollPane scrollPane = new JScrollPane(
                tableGridPanel);

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder());

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        scrollPane.getVerticalScrollBar()
                .setUnitIncrement(22);

        scrollPane.getViewport()
                .setBackground(
                        Color.WHITE);

        panel.add(
                scrollPane,
                BorderLayout.CENTER);

        return panel;
    }

    private void registerEvents() {
        // refreshButton.addActionListener(
        // event -> loadDashboard());

        searchField.addActionListener(
                event -> applySearch());

        searchField.getDocument()
                .addDocumentListener(
                        new javax.swing.event.DocumentListener() {
                            @Override
                            public void insertUpdate(
                                    javax.swing.event.DocumentEvent event) {
                                applySearch();
                            }

                            @Override
                            public void removeUpdate(
                                    javax.swing.event.DocumentEvent event) {
                                applySearch();
                            }

                            @Override
                            public void changedUpdate(
                                    javax.swing.event.DocumentEvent event) {
                                applySearch();
                            }
                        });
    }

    public void loadDashboard() {
        if (loadingDashboard) {
            return;
        }

        loadingDashboard = true;

        // refreshButton.setEnabled(false);
        loadingLabel.setText("Đang tải dữ liệu...");

        SwingWorker<List<TableDashboardItem>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<TableDashboardItem> doInBackground() {
                return coffeeTableService.getDashboardTables();
            }

            @Override
            protected void done() {
                try {
                    List<TableDashboardItem> items = get();

                    updateDashboardData(items);

                    loadingLabel.setText(
                            ""
                                    + java.time.LocalTime.now()
                                            .format(
                                                    java.time.format.DateTimeFormatter
                                                            .ofPattern(
                                                                    "")));

                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();

                    loadingLabel.setText(
                            "");

                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause() == null
                            ? exception
                            : exception.getCause();

                    loadingLabel.setText(
                            "Không thể cập nhật dữ liệu");

                    System.err.println(
                            "Không thể tải Dashboard bàn: "
                                    + cause.getMessage());

                } finally {
                    loadingDashboard = false;
                    // refreshButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void updateDashboardData(
            List<TableDashboardItem> items) {

        allItems.clear();

        if (items != null) {
            allItems.addAll(items);
        }

        allItems.sort(
                Comparator.comparingInt(
                        TableDashboardItem::getTableNumber));

        updateStatistics();

        /*
         * applySearch() vừa lọc dữ liệu,
         * vừa gọi tableGridPanel.displayTables(...).
         */
        applySearch();
    }

    private void applySearch() {
        String keyword = searchField.getText().trim();

        List<TableDashboardItem> filtered = new ArrayList<>();

        for (TableDashboardItem item : allItems) {
            if (keyword.isEmpty()
                    || String.valueOf(
                            item.getTableNumber()).contains(keyword)) {

                filtered.add(item);
            }
        }

        displayTables(filtered);

        resultLabel.setText(
                filtered.size() + " bàn");
    }

    private void updateStatistics() {
        TableDashboardSummary summary = coffeeTableService
                .calculateSummary(
                        allItems);

        totalCard.setValue(
                summary.getTotalTables());

        availableCard.setValue(
                summary.getAvailableTables());

        inProgressCard.setValue(
                summary.getInProgressTables());

        preparedCard.setValue(
                summary.getPreparedTables());

        servedCard.setValue(
                summary.getServedTables());
    }

    private void handleTableClick(
            TableDashboardItem item) {
        if (item == null) {
            return;
        }

        if (item.getTableStatus() == TableStatus.INACTIVE) {

            showWarning(
                    "Bàn "
                            + item.getTableNumber()
                            + " đang ngừng sử dụng.");

            return;
        }

        if (item.canCreateOrder()) {
            handleAvailableTable(item);
            return;
        }

        if (item.hasActiveOrder()) {
            handleOccupiedTable(item);
            return;
        }

        showWarning(
                "Bàn hiện không thể thao tác.");
    }

    private void handleOccupiedTable(
            TableDashboardItem item) {

        if (dashboardListener == null) {
            showWarning(
                    "Chưa cấu hình chức năng xem chi tiết bàn.");
            return;
        }

        dashboardListener
                .onViewTableRequested(item);
    }

    @Override
    public void removeNotify() {
        if (autoRefreshTimer.isRunning()) {
            autoRefreshTimer.stop();
        }

        super.removeNotify();
    }

    @Override
    public void addNotify() {
        super.addNotify();

        if (!autoRefreshTimer.isRunning()) {
            autoRefreshTimer.start();
        }
    }
}