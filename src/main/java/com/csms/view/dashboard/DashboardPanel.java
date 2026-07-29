package com.csms.view.dashboard;

import com.csms.dao.DashboardDAO;
import com.csms.dto.DailyRevenue;
import com.csms.dto.DashboardStats;
import com.csms.dto.LowStockProduct;
import com.csms.view.dashboard.component.RevenueChartPanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DashboardPanel extends JPanel {

    private static final Color BACKGROUND_COLOR = new Color(246, 248, 252);

    private static final Color CARD_BACKGROUND = Color.WHITE;

    private static final Color PRIMARY_COLOR = new Color(10, 132, 222);

    private static final Color PRIMARY_LIGHT = new Color(232, 245, 255);

    private static final Color SUCCESS_COLOR = new Color(22, 163, 74);

    private static final Color SUCCESS_LIGHT = new Color(232, 250, 239);

    private static final Color WARNING_COLOR = new Color(234, 137, 22);

    private static final Color WARNING_LIGHT = new Color(255, 247, 230);

    private static final Color PURPLE_COLOR = new Color(124, 92, 230);

    private static final Color PURPLE_LIGHT = new Color(242, 238, 255);

    private static final Color TEXT_PRIMARY = new Color(25, 32, 45);

    private static final Color TEXT_SECONDARY = new Color(105, 115, 132);

    private static final Color BORDER_COLOR = new Color(228, 233, 241);

    private final DashboardDAO dashboardDAO;

    private final NumberFormat currencyFormat;

    private final DateTimeFormatter dateTimeFormatter;

    private final JLabel revenueValueLabel;
    private final JLabel orderCountValueLabel;
    private final JLabel occupiedTableValueLabel;
    private final JLabel lowStockValueLabel;
    private final JLabel lastUpdatedLabel;

    private final RevenueChartPanel revenueChartPanel;

    private final DefaultTableModel lowStockTableModel;
    private final JTable lowStockTable;

    private final JButton refreshButton;

    private boolean loading;

    public DashboardPanel() {
        dashboardDAO = new DashboardDAO();

        currencyFormat = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("vi-VN"));

        dateTimeFormatter = DateTimeFormatter.ofPattern(
                "dd/MM/yyyy HH:mm:ss");

        revenueValueLabel = createValueLabel();

        orderCountValueLabel = createValueLabel();

        occupiedTableValueLabel = createValueLabel();

        lowStockValueLabel = createValueLabel();

        lastUpdatedLabel = new JLabel("Chưa cập nhật");

        revenueChartPanel = new RevenueChartPanel();

        lowStockTableModel = createLowStockTableModel();

        lowStockTable = new JTable(lowStockTableModel);

        refreshButton = new JButton("Làm mới");

        loading = false;

        initializeComponents();
        registerEvents();

        loadDashboardData();
        startAutomaticRefresh();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BACKGROUND_COLOR);

        setBorder(
                BorderFactory.createEmptyBorder(
                        24,
                        26,
                        24,
                        26));

        add(
                createHeaderPanel(),
                BorderLayout.NORTH);

        add(
                createContentPanel(),
                BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel titlePanel = new JPanel(
                new BorderLayout(0, 5));

        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel(
                "");

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        27));

        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel descriptionLabel = new JLabel(
                "Theo dõi hoạt động kinh doanh hôm nay");

        descriptionLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14));

        descriptionLabel.setForeground(TEXT_SECONDARY);

        titlePanel.add(
                titleLabel,
                BorderLayout.NORTH);

        titlePanel.add(
                descriptionLabel,
                BorderLayout.SOUTH);

        JPanel actionPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        12,
                        0));

        actionPanel.setOpaque(false);

        lastUpdatedLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12));

        lastUpdatedLabel.setForeground(TEXT_SECONDARY);

        configureRefreshButton();

        actionPanel.add(lastUpdatedLabel);
        actionPanel.add(refreshButton);

        panel.add(
                titlePanel,
                BorderLayout.WEST);

        panel.add(
                actionPanel,
                BorderLayout.EAST);

        return panel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(
                new BorderLayout(
                        0,
                        18));

        panel.setOpaque(false);

        JPanel topSection = new JPanel(
                new BorderLayout(
                        0,
                        18));

        topSection.setOpaque(false);

        topSection.add(
                createStatisticCardsPanel(),
                BorderLayout.NORTH);

        topSection.add(
                createChartSection(),
                BorderLayout.CENTER);

        panel.add(
                topSection,
                BorderLayout.CENTER);

        panel.add(
                createLowStockSection(),
                BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatisticCardsPanel() {
        JPanel cardsPanel = new JPanel(
                new GridLayout(
                        1,
                        4,
                        16,
                        0));

        cardsPanel.setOpaque(false);

        cardsPanel.add(
                createStatisticCard(
                        "Doanh thu hôm nay",
                        revenueValueLabel,
                        "Đơn đã thanh toán",
                        PRIMARY_COLOR,
                        PRIMARY_LIGHT,
                        "₫"));

        cardsPanel.add(
                createStatisticCard(
                        "Đơn hàng hôm nay",
                        orderCountValueLabel,
                        "Không gồm đơn đã hủy",
                        SUCCESS_COLOR,
                        SUCCESS_LIGHT,
                        "ĐH"));

        cardsPanel.add(
                createStatisticCard(
                        "Bàn đang phục vụ",
                        occupiedTableValueLabel,
                        "Các bàn đang có khách",
                        PURPLE_COLOR,
                        PURPLE_LIGHT,
                        "B"));

        cardsPanel.add(
                createStatisticCard(
                        "Sản phẩm sắp hết",
                        lowStockValueLabel,
                        "Số lượng không quá 10",
                        WARNING_COLOR,
                        WARNING_LIGHT,
                        "!"));

        cardsPanel.setPreferredSize(
                new Dimension(0, 145));

        return cardsPanel;
    }

    private JPanel createStatisticCard(
            String title,
            JLabel valueLabel,
            String description,
            Color accentColor,
            Color iconBackground,
            String iconText) {

        RoundedPanel card = new RoundedPanel(
                18,
                CARD_BACKGROUND);

        card.setLayout(
                new BorderLayout(
                        12,
                        8));

        card.setBorder(
                BorderFactory.createEmptyBorder(
                        18,
                        18,
                        17,
                        18));

        JPanel topPanel = new JPanel(
                new BorderLayout());

        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13));

        titleLabel.setForeground(TEXT_SECONDARY);

        JLabel iconLabel = new JLabel(
                iconText,
                SwingConstants.CENTER);

        iconLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13));

        iconLabel.setForeground(accentColor);
        iconLabel.setOpaque(true);
        iconLabel.setBackground(iconBackground);

        iconLabel.setPreferredSize(
                new Dimension(36, 30));

        iconLabel.setBorder(
                BorderFactory.createEmptyBorder(
                        4,
                        8,
                        4,
                        8));

        topPanel.add(
                titleLabel,
                BorderLayout.WEST);

        topPanel.add(
                iconLabel,
                BorderLayout.EAST);

        valueLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        25));

        valueLabel.setForeground(TEXT_PRIMARY);

        JLabel descriptionLabel = new JLabel(description);

        descriptionLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11));

        descriptionLabel.setForeground(TEXT_SECONDARY);

        card.add(
                topPanel,
                BorderLayout.NORTH);

        card.add(
                valueLabel,
                BorderLayout.CENTER);

        card.add(
                descriptionLabel,
                BorderLayout.SOUTH);

        return card;
    }

    private JPanel createChartSection() {
        RoundedPanel panel = new RoundedPanel(
                18,
                CARD_BACKGROUND);

        panel.setLayout(
                new BorderLayout(
                        0,
                        16));

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        22,
                        18,
                        22));

        JPanel headerPanel = new JPanel(
                new BorderLayout());

        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(
                new BorderLayout(
                        0,
                        4));

        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel(
                "Doanh thu 7 ngày gần nhất");

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        17));

        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel noteLabel = new JLabel(
                "Dữ liệu từ các đơn đã thanh toán");

        noteLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12));

        noteLabel.setForeground(TEXT_SECONDARY);

        titlePanel.add(
                titleLabel,
                BorderLayout.NORTH);

        titlePanel.add(
                noteLabel,
                BorderLayout.SOUTH);

        JLabel periodLabel = new JLabel(
                "7 ngày");

        periodLabel.setOpaque(true);
        periodLabel.setBackground(PRIMARY_LIGHT);
        periodLabel.setForeground(PRIMARY_COLOR);

        periodLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12));

        periodLabel.setBorder(
                BorderFactory.createEmptyBorder(
                        7,
                        12,
                        7,
                        12));

        headerPanel.add(
                titlePanel,
                BorderLayout.WEST);

        headerPanel.add(
                periodLabel,
                BorderLayout.EAST);

        panel.add(
                headerPanel,
                BorderLayout.NORTH);

        panel.add(
                revenueChartPanel,
                BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLowStockSection() {
        RoundedPanel panel = new RoundedPanel(
                18,
                CARD_BACKGROUND);

        panel.setLayout(
                new BorderLayout(
                        0,
                        14));

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        18,
                        20,
                        18,
                        20));

        JPanel headerPanel = new JPanel(
                new BorderLayout());

        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(
                new BorderLayout(
                        0,
                        3));

        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel(
                "Sản phẩm sắp hết hàng");

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        16));

        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel descriptionLabel = new JLabel(
                "Các sản phẩm có tồn kho từ 10 trở xuống");

        descriptionLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12));

        descriptionLabel.setForeground(TEXT_SECONDARY);

        titlePanel.add(
                titleLabel,
                BorderLayout.NORTH);

        titlePanel.add(
                descriptionLabel,
                BorderLayout.SOUTH);

        JLabel warningLabel = new JLabel(
                "Cần kiểm tra");

        warningLabel.setOpaque(true);
        warningLabel.setBackground(WARNING_LIGHT);
        warningLabel.setForeground(WARNING_COLOR);

        warningLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        11));

        warningLabel.setBorder(
                BorderFactory.createEmptyBorder(
                        6,
                        10,
                        6,
                        10));

        headerPanel.add(
                titlePanel,
                BorderLayout.WEST);

        headerPanel.add(
                warningLabel,
                BorderLayout.EAST);

        configureLowStockTable();

        JScrollPane scrollPane = new JScrollPane(lowStockTable);

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder());

        scrollPane.getViewport()
                .setBackground(Color.WHITE);

        panel.add(
                headerPanel,
                BorderLayout.NORTH);

        panel.add(
                scrollPane,
                BorderLayout.CENTER);

        panel.setPreferredSize(
                new Dimension(0, 225));

        return panel;
    }

    private void configureRefreshButton() {
        refreshButton.setText("Làm mới");

        refreshButton.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13));

        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBackground(PRIMARY_COLOR);

        refreshButton.setPreferredSize(
                new Dimension(105, 38));

        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setContentAreaFilled(true);
        refreshButton.setOpaque(true);

        refreshButton.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR));
    }

    private void configureLowStockTable() {
        lowStockTable.setRowHeight(38);
        lowStockTable.setFillsViewportHeight(true);

        lowStockTable.setBackground(Color.WHITE);
        lowStockTable.setForeground(TEXT_PRIMARY);

        lowStockTable.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13));

        lowStockTable.setGridColor(
                new Color(238, 241, 246));

        lowStockTable.setShowVerticalLines(false);
        lowStockTable.setShowHorizontalLines(true);

        lowStockTable.setSelectionBackground(
                PRIMARY_LIGHT);

        lowStockTable.setSelectionForeground(
                TEXT_PRIMARY);

        lowStockTable.setIntercellSpacing(
                new Dimension(0, 1));

        lowStockTable.getTableHeader()
                .setReorderingAllowed(false);

        lowStockTable.getTableHeader()
                .setPreferredSize(
                        new Dimension(0, 40));

        lowStockTable.getTableHeader()
                .setBackground(
                        new Color(248, 250, 253));

        lowStockTable.getTableHeader()
                .setForeground(TEXT_SECONDARY);

        lowStockTable.getTableHeader()
                .setFont(
                        new Font(
                                "Segoe UI",
                                Font.BOLD,
                                12));

        lowStockTable.setSelectionMode(
                javax.swing.ListSelectionModel.SINGLE_SELECTION);

        lowStockTable.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(50);

        lowStockTable.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(240);

        lowStockTable.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(160);

        lowStockTable.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(130);

        lowStockTable.getColumnModel()
                .getColumn(4)
                .setPreferredWidth(100);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();

        centerRenderer.setHorizontalAlignment(
                SwingConstants.CENTER);

        lowStockTable.getColumnModel()
                .getColumn(0)
                .setCellRenderer(centerRenderer);

        lowStockTable.getColumnModel()
                .getColumn(4)
                .setCellRenderer(centerRenderer);
    }

    private DefaultTableModel createLowStockTableModel() {
        return new DefaultTableModel(
                new Object[] {
                        "ID",
                        "Sản phẩm",
                        "Danh mục",
                        "Giá bán",
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

    private JLabel createValueLabel() {
        JLabel label = new JLabel("0");

        label.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        25));

        label.setForeground(
                TEXT_PRIMARY);

        return label;
    }

    private void registerEvents() {
        refreshButton.addActionListener(
                event -> loadDashboardData());
    }

    private void loadDashboardData() {
        if (loading) {
            return;
        }

        setLoading(true);

        SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {

            @Override
            protected DashboardData doInBackground() {
                DashboardStats stats = dashboardDAO
                        .getDashboardStats();

                List<DailyRevenue> revenue = dashboardDAO
                        .getSevenDayRevenue();

                List<LowStockProduct> products = dashboardDAO
                        .getLowStockProducts();

                return new DashboardData(
                        stats,
                        revenue,
                        products);
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();

                    displayStatistics(
                            data.stats());

                    revenueChartPanel
                            .setRevenueData(
                                    data.dailyRevenue());

                    displayLowStockProducts(
                            data.lowStockProducts());

                    lastUpdatedLabel.setText(
                            "Cập nhật: "
                                    + LocalDateTime
                                            .now()
                                            .format(
                                                    dateTimeFormatter));

                } catch (Exception exception) {
                    Throwable cause = exception.getCause() == null
                            ? exception
                            : exception.getCause();

                    JOptionPane.showMessageDialog(
                            DashboardPanel.this,
                            cause.getMessage(),
                            "Lỗi Dashboard",
                            JOptionPane.ERROR_MESSAGE);

                } finally {
                    setLoading(false);
                }
            }
        };

        worker.execute();
    }

    private void displayStatistics(
            DashboardStats stats) {
        revenueValueLabel.setText(
                currencyFormat.format(
                        safeAmount(
                                stats.todayRevenue())));

        orderCountValueLabel.setText(
                String.valueOf(
                        stats.todayOrderCount()));

        occupiedTableValueLabel.setText(
                String.valueOf(
                        stats.occupiedTableCount()));

        lowStockValueLabel.setText(
                String.valueOf(
                        stats.lowStockProductCount()));
    }

    private void displayLowStockProducts(
            List<LowStockProduct> products) {
        lowStockTableModel.setRowCount(0);

        for (LowStockProduct product : products) {
            lowStockTableModel.addRow(
                    new Object[] {
                            product.id(),
                            product.name(),
                            product.categoryName(),
                            currencyFormat.format(
                                    safeAmount(
                                            product.price())),
                            product.quantity()
                    });
        }
    }

    private BigDecimal safeAmount(
            BigDecimal amount) {
        return amount == null
                ? BigDecimal.ZERO
                : amount;
    }

    private void setLoading(boolean loading) {
        this.loading = loading;

        refreshButton.setEnabled(!loading);

        refreshButton.setText(
                loading
                        ? "Đang tải..."
                        : "Làm mới");

        setCursor(
                loading
                        ? Cursor.getPredefinedCursor(
                                Cursor.WAIT_CURSOR)
                        : Cursor.getDefaultCursor());
    }

    private void startAutomaticRefresh() {
        /*
         * Tự tải lại mỗi 60 giây.
         */
        Timer timer = new Timer(
                60_000,
                event -> loadDashboardData());

        timer.start();
    }

    private static final class RoundedPanel
            extends JPanel {

        private final int arc;
        private final Color panelColor;

        private RoundedPanel(
                int arc,
                Color panelColor) {

            this.arc = arc;
            this.panelColor = panelColor;

            setOpaque(false);
        }

        @Override
        protected void paintComponent(
                java.awt.Graphics graphics) {

            java.awt.Graphics2D graphics2D = (java.awt.Graphics2D) graphics.create();

            graphics2D.setRenderingHint(
                    java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            graphics2D.setColor(
                    new Color(
                            31,
                            45,
                            61,
                            13));

            graphics2D.fillRoundRect(
                    2,
                    4,
                    getWidth() - 4,
                    getHeight() - 5,
                    arc,
                    arc);

            graphics2D.setColor(panelColor);

            graphics2D.fillRoundRect(
                    0,
                    0,
                    getWidth() - 3,
                    getHeight() - 4,
                    arc,
                    arc);

            graphics2D.setColor(BORDER_COLOR);

            graphics2D.drawRoundRect(
                    0,
                    0,
                    getWidth() - 4,
                    getHeight() - 5,
                    arc,
                    arc);

            graphics2D.dispose();

            super.paintComponent(graphics);
        }
    }

    private record DashboardData(
            DashboardStats stats,
            List<DailyRevenue> dailyRevenue,
            List<LowStockProduct> lowStockProducts) {
    }
}