package com.csms.view.dashboard;

import com.csms.entity.User;
import com.csms.utils.SessionManager;
import com.csms.view.login.LoginFrame;
import com.csms.view.order.OrderListPanel;
import com.csms.view.order.OrderPanel;
import com.csms.view.product.ProductPanel;
import com.csms.view.dashboard.DashboardPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.awt.Container;
import com.csms.entity.RoleName;
import com.csms.utils.SessionManager;
import com.csms.view.admin.user.UserManagementPanel;
import com.csms.view.admin.vat.VatSettingPanel;
import com.csms.view.admin.ingredient.IngredientManagementPanel;
import com.csms.view.admin.branch.BranchManagementPanel;
import com.csms.view.admin.vat.VatSettingPanel;
import com.csms.view.admin.backup.BackupManagementPanel;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import com.csms.view.admin.recipe.RecipeManagementPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardFrame extends JFrame {

        private static final Color PRIMARY = new Color(0, 123, 210);

        private static final Color PRIMARY_DARK = new Color(0, 87, 158);

        private static final Color PRIMARY_LIGHT = new Color(230, 244, 255);

        private static final Color SIDEBAR_BACKGROUND = new Color(0, 73, 135);

        private static final Color SIDEBAR_HOVER = new Color(13, 98, 164);

        private static final Color SIDEBAR_ACTIVE = new Color(0, 129, 224);

        private static final Color BACKGROUND = new Color(244, 247, 251);

        private static final Color CARD_BACKGROUND = Color.WHITE;

        private static final Color TITLE_COLOR = new Color(31, 41, 55);

        private static final Color TEXT_COLOR = new Color(100, 116, 139);

        private static final Color BORDER_COLOR = new Color(224, 230, 238);

        private static final Color SUCCESS = new Color(22, 163, 74);

        private static final Color WARNING = new Color(234, 143, 8);

        private static final Color DANGER = new Color(220, 53, 69);

        private static final Color NORMAL_BACKGROUND = new Color(15, 67, 105);

        private static final Color HOVER_BACKGROUND = new Color(21, 91, 139);

        private static final Color ACTIVE_BACKGROUND = new Color(25, 145, 213);

        private static final Color PRESSED_BACKGROUND = new Color(13, 104, 164);

        private static final Color NORMAL_FOREGROUND = new Color(221, 235, 245);

        private final CardLayout contentLayout;
        private final JPanel contentPanel;

        private final Map<String, MenuButton> menuButtons;

        private JLabel pageTitleLabel;
        private String activePage;

        public DashboardFrame() {
                contentLayout = new CardLayout();
                contentPanel = new JPanel(contentLayout);
                menuButtons = new LinkedHashMap<>();

                activePage = "dashboard";

                initializeFrame();
                initializeComponents();
                navigateTo("dashboard", "Tổng quan");
        }

        private void initializeFrame() {
                setTitle("CSMS - Coffee Shop Management System");
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setMinimumSize(new Dimension(1200, 760));
                setSize(new Dimension(1366, 820));
                setLocationRelativeTo(null);
        }

        private void initializeComponents() {
                JPanel rootPanel = new JPanel(new BorderLayout());
                rootPanel.setBackground(BACKGROUND);

                rootPanel.add(createSidebar(), BorderLayout.WEST);

                JPanel mainPanel = new JPanel(new BorderLayout());
                mainPanel.setBackground(BACKGROUND);

                mainPanel.add(createHeader(), BorderLayout.NORTH);
                mainPanel.add(createContentPanel(), BorderLayout.CENTER);

                rootPanel.add(mainPanel, BorderLayout.CENTER);

                setContentPane(rootPanel);
        }

        private JPanel createSidebar() {
                JPanel sidebar = new JPanel();

                sidebar.setPreferredSize(
                                new Dimension(250, 0));

                sidebar.setMinimumSize(
                                new Dimension(250, 0));

                sidebar.setBackground(
                                new Color(15, 67, 105));

                sidebar.setLayout(
                                new BoxLayout(
                                                sidebar,
                                                BoxLayout.Y_AXIS));

                sidebar.setBorder(
                                new EmptyBorder(
                                                20,
                                                14,
                                                16,
                                                14));

                /*
                 * Logo hệ thống.
                 */
                JPanel logoPanel = createLogoPanel();
                logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                sidebar.add(logoPanel);
                sidebar.add(Box.createVerticalStrut(26));

                /*
                 * Nhóm vận hành.
                 */

                sidebar.add(
                                createMenuButton(
                                                "Tổng quan",
                                                "dashboard"));

                sidebar.add(Box.createVerticalStrut(5));

                if (SessionManager.getCurrentUser()
                                .getRoleName() == RoleName.ADMIN) {

                        sidebar.add(
                                        createMenuButton(
                                                        "Người dùng",
                                                        "users"));
                }

                sidebar.add(Box.createVerticalStrut(5));

                sidebar.add(
                                createMenuButton(
                                                "Menu sản phẩm",
                                                "product"));

                sidebar.add(Box.createVerticalStrut(5));

                sidebar.add(
                                createMenuButton(
                                                "Công thức",
                                                "recipes"));

                sidebar.add(Box.createVerticalStrut(5));

                sidebar.add(
                                createMenuButton(
                                                "Nguyên liệu",
                                                "ingredients"));

                sidebar.add(Box.createVerticalStrut(22));

                sidebar.add(
                                createSidebarSectionTitle(
                                                "QUẢN LÝ HỆ THỐNG"));

                sidebar.add(
                                createMenuButton(
                                                "Chi nhánh",
                                                "branches"));

                sidebar.add(Box.createVerticalStrut(5));

                sidebar.add(
                                createMenuButton(
                                                "Cấu hình VAT",
                                                "vat"));

                sidebar.add(Box.createVerticalStrut(5));

                if (SessionManager.getCurrentUser() != null
                                && SessionManager
                                                .getCurrentUser()
                                                .getRoleName() == RoleName.ADMIN) {

                        sidebar.add(
                                        createMenuButton(
                                                        "Sao lưu dữ liệu",
                                                        "backup"));
                }

                sidebar.add(Box.createVerticalGlue());

                /*
                 * Đường phân cách phía trên tài khoản.
                 */
                JSeparator separator = new JSeparator();

                separator.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                1));

                separator.setForeground(
                                new Color(255, 255, 255, 35));

                separator.setBackground(
                                new Color(255, 255, 255, 35));

                separator.setAlignmentX(
                                Component.LEFT_ALIGNMENT);

                sidebar.add(separator);
                sidebar.add(Box.createVerticalStrut(14));

                JPanel userPanel = createUserSidebarPanel();

                userPanel.setAlignmentX(
                                Component.LEFT_ALIGNMENT);

                sidebar.add(userPanel);

                sidebar.add(Box.createVerticalStrut(10));

                JButton logoutButton = createLogoutButton();

                logoutButton.setAlignmentX(
                                Component.LEFT_ALIGNMENT);

                sidebar.add(logoutButton);

                return sidebar;
        }

        private JPanel createLogoPanel() {
                JPanel logoPanel = new JPanel(
                                new BorderLayout());

                logoPanel.setOpaque(false);
                logoPanel.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                72));

                logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel logoLabel = new JLabel("CSMS");
                logoLabel.setForeground(Color.WHITE);

                logoLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                30));

                JLabel subtitleLabel = new JLabel(
                                "Coffee Management");

                subtitleLabel.setForeground(
                                new Color(190, 219, 242));

                subtitleLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.PLAIN,
                                                13));

                logoPanel.add(logoLabel, BorderLayout.NORTH);
                logoPanel.add(subtitleLabel, BorderLayout.SOUTH);

                return logoPanel;
        }

        private JPanel createUserSidebarPanel() {
                User currentUser = SessionManager.getCurrentUser();

                String fullName = currentUser == null
                                ? "Chưa đăng nhập"
                                : currentUser.getFullName();

                String role = currentUser == null
                                ? "GUEST"
                                : String.valueOf(
                                                currentUser.getRoleName());

                RoundedPanel userPanel = new RoundedPanel(
                                14,
                                new Color(0, 62, 117));

                userPanel.setLayout(new BorderLayout(10, 0));
                userPanel.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                68));

                userPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                userPanel.setBorder(
                                new EmptyBorder(
                                                10,
                                                12,
                                                10,
                                                12));

                JLabel avatarLabel = new JLabel(
                                getInitials(fullName),
                                SwingConstants.CENTER);

                avatarLabel.setPreferredSize(
                                new Dimension(42, 42));

                avatarLabel.setOpaque(true);
                avatarLabel.setBackground(PRIMARY);
                avatarLabel.setForeground(Color.WHITE);

                avatarLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                14));

                JPanel informationPanel = new JPanel(new BorderLayout());

                informationPanel.setOpaque(false);

                JLabel nameLabel = new JLabel(fullName);
                nameLabel.setForeground(Color.WHITE);

                nameLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                13));

                JLabel roleLabel = new JLabel(role);
                roleLabel.setForeground(
                                new Color(181, 211, 235));

                roleLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.PLAIN,
                                                11));

                informationPanel.add(
                                nameLabel,
                                BorderLayout.CENTER);

                informationPanel.add(
                                roleLabel,
                                BorderLayout.SOUTH);

                userPanel.add(
                                avatarLabel,
                                BorderLayout.WEST);

                userPanel.add(
                                informationPanel,
                                BorderLayout.CENTER);

                return userPanel;
        }

        private String getInitials(String fullName) {
                if (fullName == null
                                || fullName.isBlank()
                                || "Chưa đăng nhập".equals(fullName)) {

                        return "CS";
                }

                String[] words = fullName.trim().split("\\s+");

                if (words.length == 1) {
                        return words[0]
                                        .substring(0, 1)
                                        .toUpperCase();
                }

                return (words[0].substring(0, 1)
                                + words[words.length - 1]
                                                .substring(0, 1))
                                .toUpperCase();
        }

        private JPanel createHeader() {
                JPanel headerPanel = new JPanel(
                                new BorderLayout());

                headerPanel.setBackground(Color.WHITE);
                headerPanel.setPreferredSize(
                                new Dimension(0, 78));

                headerPanel.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createMatteBorder(
                                                                0,
                                                                0,
                                                                1,
                                                                0,
                                                                BORDER_COLOR),
                                                new EmptyBorder(
                                                                14,
                                                                28,
                                                                14,
                                                                28)));

                JPanel titlePanel = new JPanel(new BorderLayout());

                titlePanel.setOpaque(false);

                pageTitleLabel = new JLabel("Tổng quan");
                pageTitleLabel.setForeground(TITLE_COLOR);

                pageTitleLabel.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                26));

                JLabel breadcrumbLabel = new JLabel(
                                "CSMS / Trang quản trị");

                breadcrumbLabel.setForeground(TEXT_COLOR);

                breadcrumbLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.PLAIN,
                                                12));

                titlePanel.add(
                                pageTitleLabel,
                                BorderLayout.NORTH);

                titlePanel.add(
                                breadcrumbLabel,
                                BorderLayout.SOUTH);

                JPanel rightPanel = new JPanel(
                                new FlowLayout(
                                                FlowLayout.RIGHT,
                                                14,
                                                4));

                rightPanel.setOpaque(false);

                LocalDate today = LocalDate.now();

                JLabel dateLabel = new JLabel(
                                today.format(
                                                DateTimeFormatter.ofPattern(
                                                                "dd/MM/yyyy")));

                dateLabel.setForeground(TEXT_COLOR);
                dateLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.PLAIN,
                                                13));

                JButton quickOrderButton = new JButton("+ Tạo đơn hàng");

                quickOrderButton.setPreferredSize(
                                new Dimension(145, 40));

                quickOrderButton.setBackground(PRIMARY);
                quickOrderButton.setForeground(Color.WHITE);
                quickOrderButton.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                13));

                quickOrderButton.setFocusPainted(false);
                quickOrderButton.setBorderPainted(false);
                quickOrderButton.setCursor(
                                Cursor.getPredefinedCursor(
                                                Cursor.HAND_CURSOR));

                quickOrderButton.addActionListener(
                                event -> navigateTo(
                                                "order",
                                                "Bán hàng"));

                quickOrderButton.addMouseListener(
                                new MouseAdapter() {

                                        @Override
                                        public void mouseEntered(
                                                        MouseEvent event) {

                                                quickOrderButton.setBackground(
                                                                PRIMARY_DARK);
                                        }

                                        @Override
                                        public void mouseExited(
                                                        MouseEvent event) {

                                                quickOrderButton.setBackground(
                                                                PRIMARY);
                                        }
                                });

                rightPanel.add(dateLabel);
                rightPanel.add(quickOrderButton);

                headerPanel.add(
                                titlePanel,
                                BorderLayout.WEST);

                headerPanel.add(
                                rightPanel,
                                BorderLayout.EAST);

                return headerPanel;
        }

        private JPanel createContentPanel() {
                contentPanel.setBackground(BACKGROUND);

                contentPanel.add(
                                createDashboardPage(),
                                "dashboard");

                contentPanel.add(
                                wrapPage(new ProductPanel()),
                                "product");

                contentPanel.add(
                                wrapPage(new OrderPanel()),
                                "order");

                contentPanel.add(
                                wrapPage(new OrderListPanel()),
                                "order-list");

                contentPanel.add(
                                new DashboardPanel(),
                                "dashboard");

                contentPanel.add(
                                new DashboardPanel(),
                                "overview");

                contentPanel.add(
                                new UserManagementPanel(),
                                "users");

                contentPanel.add(
                                new RecipeManagementPanel(),
                                "recipes");

                contentPanel.add(
                                new IngredientManagementPanel(),
                                "ingredients");

                contentPanel.add(
                                new BranchManagementPanel(),
                                "branches");

                contentPanel.add(
                                new VatSettingPanel(),
                                "vat");

                contentPanel.add(
                                new BackupManagementPanel(),
                                "backup");

                contentPanel.add(
                                createPlaceholderPage(
                                                "Quản lý nhân viên",
                                                "Quản lý tài khoản, vai trò và ca làm việc."),
                                "employee");

                contentPanel.add(
                                createPlaceholderPage(
                                                "Báo cáo và thống kê",
                                                "Theo dõi doanh thu, đơn hàng và hiệu quả bán hàng."),
                                "report");

                return contentPanel;
        }

        private JPanel wrapPage(JPanel page) {
                JPanel wrapper = new JPanel(
                                new BorderLayout());

                wrapper.setBackground(BACKGROUND);
                wrapper.setBorder(
                                new EmptyBorder(
                                                22,
                                                22,
                                                22,
                                                22));

                RoundedPanel card = new RoundedPanel(
                                18,
                                CARD_BACKGROUND);

                card.setLayout(new BorderLayout());
                card.setBorder(
                                new EmptyBorder(
                                                12,
                                                12,
                                                12,
                                                12));

                card.add(page, BorderLayout.CENTER);
                wrapper.add(card, BorderLayout.CENTER);

                return wrapper;
        }

        private JPanel createDashboardPage() {
                JPanel dashboard = new JPanel(
                                new BorderLayout(0, 20));

                dashboard.setBackground(BACKGROUND);
                dashboard.setBorder(
                                new EmptyBorder(
                                                22,
                                                22,
                                                22,
                                                22));

                dashboard.add(
                                createWelcomePanel(),
                                BorderLayout.NORTH);

                JPanel centerPanel = new JPanel(new BorderLayout(0, 20));

                centerPanel.setOpaque(false);

                centerPanel.add(
                                createStatisticPanel(),
                                BorderLayout.NORTH);

                centerPanel.add(
                                createOverviewSection(),
                                BorderLayout.CENTER);

                dashboard.add(
                                centerPanel,
                                BorderLayout.CENTER);

                return dashboard;
        }

        private JPanel createWelcomePanel() {
                RoundedPanel panel = new RoundedPanel(
                                18,
                                PRIMARY);

                panel.setLayout(new BorderLayout());
                panel.setBorder(
                                new EmptyBorder(
                                                24,
                                                28,
                                                24,
                                                28));

                User currentUser = SessionManager.getCurrentUser();

                String fullName = currentUser == null
                                ? "bạn"
                                : currentUser.getFullName();

                JPanel textPanel = new JPanel(new BorderLayout());

                textPanel.setOpaque(false);

                JLabel titleLabel = new JLabel(
                                "Xin chào, " + fullName + "!");

                titleLabel.setForeground(Color.WHITE);
                titleLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                18));

                JLabel subtitleLabel = new JLabel(
                                "Chúc bạn một ngày bán hàng hiệu quả.");

                subtitleLabel.setForeground(
                                new Color(225, 242, 255));

                subtitleLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.PLAIN,
                                                14));

                subtitleLabel.setBorder(
                                new EmptyBorder(
                                                8,
                                                0,
                                                0,
                                                0));

                textPanel.add(
                                titleLabel,
                                BorderLayout.NORTH);

                textPanel.add(
                                subtitleLabel,
                                BorderLayout.CENTER);

                JLabel statusLabel = new JLabel(
                                "Hệ thống đang hoạt động",
                                SwingConstants.CENTER);

                statusLabel.setOpaque(true);
                statusLabel.setBackground(
                                new Color(255, 255, 255, 40));

                statusLabel.setForeground(Color.WHITE);
                statusLabel.setPreferredSize(
                                new Dimension(190, 42));

                statusLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                13));

                panel.add(
                                textPanel,
                                BorderLayout.CENTER);

                panel.add(
                                statusLabel,
                                BorderLayout.EAST);

                return panel;
        }

        private JPanel createStatisticPanel() {
                JPanel statisticsPanel = new JPanel(
                                new GridLayout(1, 4, 16, 0));

                statisticsPanel.setOpaque(false);
                statisticsPanel.setPreferredSize(
                                new Dimension(0, 135));

                statisticsPanel.add(
                                createStatisticCard(
                                                "Doanh thu hôm nay",
                                                "0 ₫",
                                                "DT",
                                                PRIMARY));

                statisticsPanel.add(
                                createStatisticCard(
                                                "Tổng đơn hàng",
                                                "0",
                                                "ĐH",
                                                SUCCESS));

                statisticsPanel.add(
                                createStatisticCard(
                                                "Đơn đang xử lý",
                                                "0",
                                                "XL",
                                                WARNING));

                statisticsPanel.add(
                                createStatisticCard(
                                                "Sản phẩm sắp hết",
                                                "0",
                                                "TK",
                                                DANGER));

                return statisticsPanel;
        }

        private JPanel createStatisticCard(
                        String title,
                        String value,
                        String iconText,
                        Color accentColor) {

                RoundedPanel card = new RoundedPanel(
                                18,
                                CARD_BACKGROUND);

                card.setLayout(new BorderLayout());
                card.setBorder(
                                new EmptyBorder(
                                                20,
                                                20,
                                                20,
                                                20));

                JPanel textPanel = new JPanel(new BorderLayout());

                textPanel.setOpaque(false);

                JLabel titleLabel = new JLabel(title);
                titleLabel.setForeground(TEXT_COLOR);
                titleLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.PLAIN,
                                                13));

                JLabel valueLabel = new JLabel(value);
                valueLabel.setForeground(TITLE_COLOR);
                valueLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                26));

                valueLabel.setBorder(
                                new EmptyBorder(
                                                7,
                                                0,
                                                0,
                                                0));

                textPanel.add(
                                titleLabel,
                                BorderLayout.NORTH);

                textPanel.add(
                                valueLabel,
                                BorderLayout.CENTER);

                JLabel iconLabel = new JLabel(
                                iconText,
                                SwingConstants.CENTER);

                iconLabel.setPreferredSize(
                                new Dimension(52, 52));

                iconLabel.setOpaque(true);
                iconLabel.setBackground(
                                mixWithWhite(
                                                accentColor,
                                                0.86));

                iconLabel.setForeground(accentColor);
                iconLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                14));

                card.add(
                                textPanel,
                                BorderLayout.CENTER);

                card.add(
                                iconLabel,
                                BorderLayout.EAST);

                return card;
        }

        private JPanel createOverviewSection() {
                JPanel overviewPanel = new JPanel(
                                new GridLayout(1, 2, 18, 0));

                overviewPanel.setOpaque(false);

                overviewPanel.add(
                                createQuickActionsPanel());

                overviewPanel.add(
                                createActivityPanel());

                return overviewPanel;
        }

        private JPanel createQuickActionsPanel() {
                RoundedPanel panel = new RoundedPanel(
                                18,
                                CARD_BACKGROUND);

                panel.setLayout(new BorderLayout());
                panel.setBorder(
                                new EmptyBorder(
                                                22,
                                                22,
                                                22,
                                                22));

                JLabel titleLabel = new JLabel(
                                "Thao tác nhanh");

                titleLabel.setForeground(TITLE_COLOR);
                titleLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                18));

                titleLabel.setBorder(
                                new EmptyBorder(
                                                0,
                                                0,
                                                18,
                                                0));

                JPanel actionGrid = new JPanel(
                                new GridLayout(2, 2, 14, 14));

                actionGrid.setOpaque(false);

                actionGrid.add(
                                createQuickActionButton(
                                                "Tạo đơn hàng",
                                                "order"));

                actionGrid.add(
                                createQuickActionButton(
                                                "Xem đơn hàng",
                                                "order-list"));

                actionGrid.add(
                                createQuickActionButton(
                                                "Quản lý sản phẩm",
                                                "product"));

                actionGrid.add(
                                createQuickActionButton(
                                                "Xem báo cáo",
                                                "report"));

                panel.add(
                                titleLabel,
                                BorderLayout.NORTH);

                panel.add(
                                actionGrid,
                                BorderLayout.CENTER);

                return panel;
        }

        private JButton createQuickActionButton(
                        String text,
                        String pageName) {

                JButton button = new JButton(text);

                button.putClientProperty(
                                "JButton.buttonType",
                                "roundRect");

                button.putClientProperty(
                                "JButton.arc",
                                18);

                button.setBackground(Color.WHITE);

                button.setForeground(
                                new Color(0, 92, 170));

                button.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                14));

                button.setFocusPainted(false);
                button.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(
                                                                new Color(190, 220, 242),
                                                                1,
                                                                true),
                                                new EmptyBorder(
                                                                14,
                                                                18,
                                                                14,
                                                                18)));

                button.setCursor(
                                Cursor.getPredefinedCursor(
                                                Cursor.HAND_CURSOR));

                button.addActionListener(
                                event -> navigateTo(
                                                pageName,
                                                getPageTitle(pageName)));

                button.addMouseListener(
                                new MouseAdapter() {

                                        @Override
                                        public void mouseEntered(
                                                        MouseEvent event) {

                                                button.setBackground(
                                                                new Color(232, 246, 255));

                                                button.setForeground(PRIMARY_DARK);
                                        }

                                        @Override
                                        public void mouseExited(
                                                        MouseEvent event) {

                                                button.setBackground(Color.WHITE);

                                                button.setForeground(
                                                                new Color(0, 92, 170));
                                        }

                                        @Override
                                        public void mousePressed(
                                                        MouseEvent event) {

                                                button.setBackground(
                                                                new Color(211, 237, 255));
                                        }
                                });

                return button;
        }

        private JPanel createActivityPanel() {
                RoundedPanel panel = new RoundedPanel(
                                18,
                                CARD_BACKGROUND);

                panel.setLayout(new BorderLayout());
                panel.setBorder(
                                new EmptyBorder(
                                                22,
                                                22,
                                                22,
                                                22));

                JLabel titleLabel = new JLabel(
                                "Hoạt động gần đây");

                titleLabel.setForeground(TITLE_COLOR);
                titleLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                18));

                titleLabel.setBorder(
                                new EmptyBorder(
                                                0,
                                                0,
                                                14,
                                                0));

                JPanel activityList = new JPanel();
                activityList.setOpaque(false);

                activityList.setLayout(
                                new BoxLayout(
                                                activityList,
                                                BoxLayout.Y_AXIS));

                activityList.add(
                                createActivityItem(
                                                "Hệ thống đã sẵn sàng",
                                                "Bạn có thể bắt đầu tạo đơn hàng."));

                activityList.add(Box.createVerticalStrut(10));

                activityList.add(
                                createActivityItem(
                                                "Quản lý sản phẩm",
                                                "Kiểm tra số lượng tồn kho thường xuyên."));

                activityList.add(Box.createVerticalStrut(10));

                activityList.add(
                                createActivityItem(
                                                "Báo cáo doanh thu",
                                                "Thống kê sẽ được cập nhật theo dữ liệu bán hàng."));

                panel.add(
                                titleLabel,
                                BorderLayout.NORTH);

                panel.add(
                                activityList,
                                BorderLayout.CENTER);

                return panel;
        }

        private JPanel createActivityItem(
                        String title,
                        String description) {

                JPanel panel = new JPanel(
                                new BorderLayout(12, 0));

                panel.setOpaque(false);
                panel.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                62));

                JLabel indicator = new JLabel();
                indicator.setOpaque(true);
                indicator.setBackground(PRIMARY);
                indicator.setPreferredSize(
                                new Dimension(5, 50));

                JPanel textPanel = new JPanel(new BorderLayout());

                textPanel.setOpaque(false);

                JLabel titleLabel = new JLabel(title);
                titleLabel.setForeground(TITLE_COLOR);
                titleLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                13));

                JLabel descriptionLabel = new JLabel(description);

                descriptionLabel.setForeground(TEXT_COLOR);
                descriptionLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.PLAIN,
                                                12));

                textPanel.add(
                                titleLabel,
                                BorderLayout.NORTH);

                textPanel.add(
                                descriptionLabel,
                                BorderLayout.CENTER);

                panel.add(
                                indicator,
                                BorderLayout.WEST);

                panel.add(
                                textPanel,
                                BorderLayout.CENTER);

                return panel;
        }

        private JPanel createPlaceholderPage(
                        String title,
                        String description) {

                JPanel wrapper = new JPanel(
                                new BorderLayout());

                wrapper.setBackground(BACKGROUND);
                wrapper.setBorder(
                                new EmptyBorder(
                                                22,
                                                22,
                                                22,
                                                22));

                RoundedPanel card = new RoundedPanel(
                                18,
                                CARD_BACKGROUND);

                card.setLayout(new BorderLayout());
                card.setBorder(
                                new EmptyBorder(
                                                40,
                                                40,
                                                40,
                                                40));

                JPanel textPanel = new JPanel(new BorderLayout());

                textPanel.setOpaque(false);

                JLabel titleLabel = new JLabel(
                                title,
                                SwingConstants.CENTER);

                titleLabel.setForeground(TITLE_COLOR);
                titleLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                26));

                JLabel descriptionLabel = new JLabel(
                                description,
                                SwingConstants.CENTER);

                descriptionLabel.setForeground(TEXT_COLOR);
                descriptionLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.PLAIN,
                                                14));

                descriptionLabel.setBorder(
                                new EmptyBorder(
                                                12,
                                                0,
                                                0,
                                                0));

                textPanel.add(
                                titleLabel,
                                BorderLayout.NORTH);

                textPanel.add(
                                descriptionLabel,
                                BorderLayout.CENTER);

                card.add(
                                textPanel,
                                BorderLayout.CENTER);

                wrapper.add(card, BorderLayout.CENTER);

                return wrapper;
        }

        private JLabel createSidebarSectionTitle(
                        String title) {

                JLabel label = new JLabel(title);

                label.setForeground(
                                new Color(
                                                174,
                                                207,
                                                231));

                label.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                10));

                label.setBorder(
                                new EmptyBorder(
                                                0,
                                                12,
                                                9,
                                                0));

                label.setAlignmentX(
                                Component.LEFT_ALIGNMENT);

                label.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                26));

                return label;
        }

        private JButton createSidebarMenuButton(
                        String text,
                        String cardName,
                        boolean selected) {

                JButton button = new JButton(text);

                button.setName(cardName);

                button.setHorizontalAlignment(
                                SwingConstants.LEFT);

                button.setFont(
                                new Font(
                                                "Segoe UI",
                                                selected
                                                                ? Font.BOLD
                                                                : Font.PLAIN,
                                                14));

                button.setForeground(
                                selected
                                                ? Color.WHITE
                                                : new Color(
                                                                221,
                                                                235,
                                                                245));

                button.setBackground(
                                selected
                                                ? new Color(
                                                                26,
                                                                139,
                                                                209)
                                                : new Color(
                                                                15,
                                                                67,
                                                                105));

                button.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                44));

                button.setPreferredSize(
                                new Dimension(
                                                220,
                                                44));

                button.setMinimumSize(
                                new Dimension(
                                                180,
                                                44));

                button.setAlignmentX(
                                Component.LEFT_ALIGNMENT);

                button.setBorder(
                                BorderFactory.createCompoundBorder(
                                                selected
                                                                ? BorderFactory.createMatteBorder(
                                                                                0,
                                                                                4,
                                                                                0,
                                                                                0,
                                                                                new Color(
                                                                                                124,
                                                                                                211,
                                                                                                255))
                                                                : BorderFactory.createEmptyBorder(),
                                                new EmptyBorder(
                                                                0,
                                                                selected ? 13 : 17,
                                                                0,
                                                                10)));

                button.setFocusPainted(false);
                button.setBorderPainted(true);
                button.setContentAreaFilled(true);
                button.setOpaque(true);

                button.setCursor(
                                Cursor.getPredefinedCursor(
                                                Cursor.HAND_CURSOR));

                button.putClientProperty(
                                "menu.cardName",
                                cardName);

                button.putClientProperty(
                                "menu.selected",
                                selected);

                button.addMouseListener(
                                new MouseAdapter() {

                                        @Override
                                        public void mouseEntered(
                                                        MouseEvent event) {

                                                boolean isSelected = Boolean.TRUE.equals(
                                                                button.getClientProperty(
                                                                                "menu.selected"));

                                                if (!isSelected) {
                                                        button.setBackground(
                                                                        new Color(
                                                                                        22,
                                                                                        87,
                                                                                        132));

                                                        button.setForeground(
                                                                        Color.WHITE);
                                                }
                                        }

                                        @Override
                                        public void mouseExited(
                                                        MouseEvent event) {

                                                boolean isSelected = Boolean.TRUE.equals(
                                                                button.getClientProperty(
                                                                                "menu.selected"));

                                                if (!isSelected) {
                                                        button.setBackground(
                                                                        new Color(
                                                                                        15,
                                                                                        67,
                                                                                        105));

                                                        button.setForeground(
                                                                        new Color(
                                                                                        221,
                                                                                        235,
                                                                                        245));
                                                }
                                        }
                                });

                button.addActionListener(event -> {
                        setSelectedSidebarButton(button);

                        /*
                         * Thay showCard(cardName) bằng method chuyển trang
                         * đang có trong DashboardFrame của bạn.
                         */
                        navigateTo(
                                        cardName,
                                        getPageTitle(cardName));
                });

                return button;
        }

        private void setSelectedSidebarButton(
                        JButton selectedButton) {

                Container parent = selectedButton.getParent();

                for (Component component : parent.getComponents()) {

                        if (!(component instanceof JButton button)) {
                                continue;
                        }

                        boolean selected = button == selectedButton;

                        button.putClientProperty(
                                        "menu.selected",
                                        selected);

                        button.setFont(
                                        new Font(
                                                        "Segoe UI",
                                                        selected
                                                                        ? Font.BOLD
                                                                        : Font.PLAIN,
                                                        14));

                        button.setForeground(
                                        selected
                                                        ? Color.WHITE
                                                        : new Color(
                                                                        221,
                                                                        235,
                                                                        245));

                        button.setBackground(
                                        selected
                                                        ? new Color(
                                                                        26,
                                                                        139,
                                                                        209)
                                                        : new Color(
                                                                        15,
                                                                        67,
                                                                        105));

                        button.setBorder(
                                        BorderFactory.createCompoundBorder(
                                                        selected
                                                                        ? BorderFactory
                                                                                        .createMatteBorder(
                                                                                                        0,
                                                                                                        4,
                                                                                                        0,
                                                                                                        0,
                                                                                                        new Color(
                                                                                                                        124,
                                                                                                                        211,
                                                                                                                        255))
                                                                        : BorderFactory
                                                                                        .createEmptyBorder(),
                                                        new EmptyBorder(
                                                                        0,
                                                                        selected ? 13 : 17,
                                                                        0,
                                                                        10)));
                }

                parent.revalidate();
                parent.repaint();
        }

        private boolean isAdmin() {
                return SessionManager.getCurrentUser() != null
                                && SessionManager
                                                .getCurrentUser()
                                                .getRoleName() == RoleName.ADMIN;
        }

        private MenuButton createMenuButton(
                        String text,
                        String pageName) {

                MenuButton button = new MenuButton(text);

                button.setName(pageName);

                button.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                44));

                button.setPreferredSize(
                                new Dimension(
                                                220,
                                                44));

                button.setMinimumSize(
                                new Dimension(
                                                180,
                                                44));

                button.setAlignmentX(
                                Component.LEFT_ALIGNMENT);

                button.addActionListener(event -> {
                        navigateTo(
                                        pageName,
                                        getPageTitle(pageName));
                });

                menuButtons.put(
                                pageName,
                                button);

                return button;
        }

        private void navigateTo(
                        String pageName,
                        String pageTitle) {

                activePage = pageName;

                contentLayout.show(
                                contentPanel,
                                pageName);

                if (pageTitleLabel != null) {
                        pageTitleLabel.setText(pageTitle);
                }

                menuButtons.forEach(
                                (name, button) -> button.setActive(
                                                name.equals(activePage)));
        }

        private String getPageTitle(String pageName) {
                return switch (pageName) {
                        case "dashboard" -> "Tổng quan";
                        case "order" -> "Bán hàng";
                        case "order-list" -> "Danh sách đơn";
                        case "product" -> "Quản lý sản phẩm";
                        case "customer" -> "Quản lý khách hàng";
                        case "employee" -> "Quản lý nhân viên";
                        case "report" -> "Báo cáo và thống kê";
                        default -> "CSMS";
                };
        }

        private Color mixWithWhite(
                        Color color,
                        double ratio) {

                int red = (int) (color.getRed() * (1 - ratio)
                                + 255 * ratio);

                int green = (int) (color.getGreen() * (1 - ratio)
                                + 255 * ratio);

                int blue = (int) (color.getBlue() * (1 - ratio)
                                + 255 * ratio);

                return new Color(red, green, blue);
        }

        private void logout() {
                int result = JOptionPane.showConfirmDialog(
                                this,
                                "Bạn có chắc muốn đăng xuất?",
                                "Xác nhận đăng xuất",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);

                if (result != JOptionPane.YES_OPTION) {
                        return;
                }

                SessionManager.logout();
                dispose();

                SwingUtilities.invokeLater(() -> {
                        LoginFrame loginFrame = new LoginFrame();
                        loginFrame.setVisible(true);
                });
        }

        private static final class MenuButton extends JButton {

                private static final Color NORMAL_BACKGROUND = new Color(0, 73, 135);

                private static final Color HOVER_BACKGROUND = new Color(0, 100, 176);

                private static final Color ACTIVE_BACKGROUND = new Color(0, 142, 230);

                private static final Color PRESSED_BACKGROUND = new Color(0, 82, 153);

                private static final Color NORMAL_FOREGROUND = new Color(225, 239, 249);

                private boolean active;
                private boolean hovered;
                private boolean pressed;

                private MenuButton(
                                String text) {

                        super("    " + text);

                        setHorizontalAlignment(SwingConstants.LEFT);
                        setVerticalAlignment(SwingConstants.CENTER);

                        setFont(
                                        new Font(
                                                        "Segoe UI",
                                                        Font.PLAIN,
                                                        15));

                        setForeground(NORMAL_FOREGROUND);

                        setBorder(
                                        new EmptyBorder(
                                                        0,
                                                        18,
                                                        0,
                                                        14));

                        setFocusPainted(false);
                        setBorderPainted(false);
                        setContentAreaFilled(false);
                        setOpaque(false);
                        setRolloverEnabled(false);

                        setCursor(
                                        Cursor.getPredefinedCursor(
                                                        Cursor.HAND_CURSOR));

                        addMouseListener(
                                        new MouseAdapter() {

                                                @Override
                                                public void mouseEntered(
                                                                MouseEvent event) {

                                                        hovered = true;
                                                        repaint();
                                                }

                                                @Override
                                                public void mouseExited(
                                                                MouseEvent event) {

                                                        hovered = false;
                                                        pressed = false;
                                                        repaint();
                                                }

                                                @Override
                                                public void mousePressed(
                                                                MouseEvent event) {

                                                        if (SwingUtilities.isLeftMouseButton(event)) {
                                                                pressed = true;
                                                                repaint();
                                                        }
                                                }

                                                @Override
                                                public void mouseReleased(
                                                                MouseEvent event) {

                                                        pressed = false;
                                                        repaint();
                                                }
                                        });
                }

                private void setActive(boolean active) {
                        this.active = active;

                        setForeground(Color.WHITE);

                        setFont(
                                        new Font(
                                                        "Segoe UI",
                                                        active
                                                                        ? Font.BOLD
                                                                        : Font.PLAIN,
                                                        15));

                        repaint();
                }

                @Override
                protected void paintComponent(Graphics graphics) {
                        Graphics2D graphics2D = (Graphics2D) graphics.create();

                        graphics2D.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        Color backgroundColor;

                        if (pressed) {
                                backgroundColor = PRESSED_BACKGROUND;
                        } else if (active) {
                                backgroundColor = ACTIVE_BACKGROUND;
                        } else if (hovered) {
                                backgroundColor = HOVER_BACKGROUND;
                        } else {
                                backgroundColor = NORMAL_BACKGROUND;
                        }

                        if (active || hovered || pressed) {
                                graphics2D.setColor(backgroundColor);

                                graphics2D.fillRoundRect(
                                                0,
                                                2,
                                                getWidth(),
                                                getHeight() - 4,
                                                12,
                                                12);
                        }

                        if (active) {
                                graphics2D.setColor(Color.WHITE);

                                graphics2D.fillRoundRect(
                                                0,
                                                10,
                                                4,
                                                getHeight() - 20,
                                                4,
                                                4);
                        }

                        graphics2D.dispose();

                        super.paintComponent(graphics);
                }
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
                                Graphics graphics) {

                        Graphics2D graphics2D = (Graphics2D) graphics.create();

                        graphics2D.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        graphics2D.setColor(
                                        new Color(30, 60, 90, 18));

                        graphics2D.fillRoundRect(
                                        3,
                                        5,
                                        getWidth() - 7,
                                        getHeight() - 8,
                                        arc,
                                        arc);

                        graphics2D.setColor(panelColor);

                        graphics2D.fillRoundRect(
                                        0,
                                        0,
                                        getWidth() - 7,
                                        getHeight() - 8,
                                        arc,
                                        arc);

                        graphics2D.setColor(BORDER_COLOR);
                        graphics2D.setStroke(
                                        new BasicStroke(1F));

                        graphics2D.drawRoundRect(
                                        0,
                                        0,
                                        getWidth() - 8,
                                        getHeight() - 9,
                                        arc,
                                        arc);

                        graphics2D.dispose();

                        super.paintComponent(graphics);
                }
        }

        private JButton createLogoutButton() {
                JButton button = new JButton("    Đăng xuất");

                button.setHorizontalAlignment(
                                SwingConstants.LEFT);

                button.setVerticalAlignment(
                                SwingConstants.CENTER);

                button.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                42));

                button.setPreferredSize(
                                new Dimension(
                                                220,
                                                42));

                button.setMinimumSize(
                                new Dimension(
                                                180,
                                                42));

                button.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                13));

                button.setForeground(
                                new Color(
                                                255,
                                                205,
                                                205));

                button.setBackground(
                                new Color(
                                                15,
                                                67,
                                                105));

                button.setBorder(
                                new EmptyBorder(
                                                0,
                                                16,
                                                0,
                                                12));

                button.setFocusPainted(false);
                button.setBorderPainted(false);
                button.setContentAreaFilled(false);
                button.setOpaque(false);
                button.setRolloverEnabled(false);

                button.setCursor(
                                Cursor.getPredefinedCursor(
                                                Cursor.HAND_CURSOR));

                button.addMouseListener(
                                new MouseAdapter() {

                                        @Override
                                        public void mouseEntered(
                                                        MouseEvent event) {

                                                button.setOpaque(true);

                                                button.setContentAreaFilled(true);

                                                button.setBackground(
                                                                new Color(
                                                                                125,
                                                                                45,
                                                                                55));

                                                button.setForeground(
                                                                Color.WHITE);
                                        }

                                        @Override
                                        public void mouseExited(
                                                        MouseEvent event) {

                                                button.setOpaque(false);

                                                button.setContentAreaFilled(false);

                                                button.setForeground(
                                                                new Color(
                                                                                255,
                                                                                205,
                                                                                205));
                                        }

                                        @Override
                                        public void mousePressed(
                                                        MouseEvent event) {

                                                button.setOpaque(true);

                                                button.setContentAreaFilled(true);

                                                button.setBackground(
                                                                new Color(
                                                                                100,
                                                                                34,
                                                                                43));
                                        }
                                });

                button.addActionListener(
                                event -> logout());

                return button;
        }
}