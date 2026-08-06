package com.csms.view.login;

import com.csms.dto.LoginResult;
import com.csms.service.AuthService;
import com.csms.utils.SessionManager;
import com.csms.view.dashboard.DashboardFrame;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginFrame extends JFrame {

        private static final Color PRIMARY = new Color(0, 122, 204);

        private static final Color PRIMARY_DARK = new Color(0, 91, 160);

        private static final Color PRIMARY_HOVER = new Color(0, 106, 190);

        private static final Color BACKGROUND = new Color(244, 247, 251);

        private static final Color CARD_BACKGROUND = Color.WHITE;

        private static final Color TITLE_COLOR = new Color(31, 41, 55);

        private static final Color TEXT_COLOR = new Color(91, 103, 118);

        private static final Color BORDER_COLOR = new Color(214, 222, 232);

        private static final Color ERROR_COLOR = new Color(220, 53, 69);

        private final AuthService authService;

        private JTextField usernameField;
        private JPasswordField passwordField;
        private JCheckBox showPasswordCheckBox;
        private JButton loginButton;
        private JLabel messageLabel;

        public LoginFrame() {
                authService = new AuthService();

                initializeFrame();
                initializeComponents();
        }

        private void initializeFrame() {
                setTitle("CSMS - Đăng nhập");
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setMinimumSize(new Dimension(1080, 680));
                setLocationRelativeTo(null);
                setResizable(false);
        }

        private void initializeComponents() {
                JPanel rootPanel = new JPanel(new BorderLayout());
                rootPanel.setBackground(BACKGROUND);

                rootPanel.add(createBrandPanel(), BorderLayout.WEST);
                rootPanel.add(createLoginArea(), BorderLayout.CENTER);

                setContentPane(rootPanel);
                pack();

                SwingUtilities.invokeLater(
                                () -> usernameField.requestFocusInWindow());
        }

        private JPanel createBrandPanel() {
                JPanel brandPanel = new GradientBrandPanel();
                brandPanel.setPreferredSize(new Dimension(430, 680));
                brandPanel.setLayout(new GridBagLayout());

                GridBagConstraints constraints = new GridBagConstraints();

                constraints.gridx = 0;
                constraints.gridy = 0;
                constraints.insets = new Insets(0, 40, 18, 40);
                constraints.fill = GridBagConstraints.HORIZONTAL;

                JLabel logoLabel = new JLabel(
                                "CSMS",
                                SwingConstants.CENTER);

                logoLabel.setForeground(Color.WHITE);
                logoLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                54));

                brandPanel.add(logoLabel, constraints);

                constraints.gridy = 1;
                constraints.insets = new Insets(0, 40, 12, 40);

                JLabel titleLabel = new JLabel(
                                "COFFEE SHOP",
                                SwingConstants.CENTER);

                titleLabel.setForeground(Color.WHITE);
                titleLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                30));

                brandPanel.add(titleLabel, constraints);

                constraints.gridy = 2;
                constraints.insets = new Insets(0, 40, 30, 40);

                JLabel systemLabel = new JLabel(
                                "MANAGEMENT SYSTEM",
                                SwingConstants.CENTER);

                systemLabel.setForeground(
                                new Color(220, 239, 255));

                systemLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                17));

                brandPanel.add(systemLabel, constraints);

                constraints.gridy = 3;
                constraints.insets = new Insets(0, 55, 14, 55);

                JLabel descriptionLabel = new JLabel(
                                "<html><div style='text-align:center;'>"
                                                + "Quản lý bán hàng, sản phẩm, đơn hàng<br>"
                                                + "và doanh thu trên một hệ thống duy nhất."
                                                + "</div></html>",
                                SwingConstants.CENTER);

                descriptionLabel.setForeground(
                                new Color(226, 240, 252));

                descriptionLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.PLAIN,
                                                15));

                brandPanel.add(descriptionLabel, constraints);

                constraints.gridy = 4;
                constraints.insets = new Insets(35, 60, 8, 60);

                brandPanel.add(
                                createFeatureLabel(
                                                "Quản lý bán hàng nhanh chóng"),
                                constraints);

                constraints.gridy = 5;
                constraints.insets = new Insets(8, 60, 8, 60);

                brandPanel.add(
                                createFeatureLabel(
                                                "Theo dõi doanh thu trực quan"),
                                constraints);

                constraints.gridy = 6;

                brandPanel.add(
                                createFeatureLabel(
                                                "Kiểm soát tồn kho chính xác"),
                                constraints);

                return brandPanel;
        }

        private JLabel createFeatureLabel(String text) {
                JLabel label = new JLabel(
                                "  " + text,
                                SwingConstants.LEFT);

                label.setForeground(Color.WHITE);
                label.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.PLAIN,
                                                14));

                label.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(
                                                                new Color(
                                                                                255,
                                                                                255,
                                                                                255,
                                                                                75),
                                                                1,
                                                                true),
                                                new EmptyBorder(
                                                                10,
                                                                12,
                                                                10,
                                                                12)));

                return label;
        }

        private JPanel createLoginArea() {
                JPanel loginArea = new JPanel(
                                new GridBagLayout());

                loginArea.setBackground(BACKGROUND);

                GridBagConstraints constraints = new GridBagConstraints();

                constraints.gridx = 0;
                constraints.gridy = 0;

                loginArea.add(
                                createLoginCard(),
                                constraints);

                return loginArea;
        }

        private JPanel createLoginCard() {
                RoundedPanel cardPanel = new RoundedPanel(
                                24,
                                CARD_BACKGROUND);

                cardPanel.setPreferredSize(
                                new Dimension(460, 520));

                cardPanel.setLayout(
                                new BorderLayout());

                cardPanel.setBorder(
                                new EmptyBorder(
                                                42,
                                                48,
                                                34,
                                                48));

                cardPanel.add(
                                createHeaderPanel(),
                                BorderLayout.NORTH);

                cardPanel.add(
                                createFormPanel(),
                                BorderLayout.CENTER);

                cardPanel.add(
                                createFooterPanel(),
                                BorderLayout.SOUTH);

                return cardPanel;
        }

        private JPanel createHeaderPanel() {
                JPanel panel = new JPanel(new BorderLayout());

                panel.setOpaque(false);
                panel.setBorder(
                                new EmptyBorder(
                                                0,
                                                0,
                                                28,
                                                0));

                JLabel titleLabel = new JLabel(
                                "Đăng nhập hệ thống",
                                SwingConstants.LEFT);

                titleLabel.setForeground(TITLE_COLOR);
                titleLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                28));

                JLabel subtitleLabel = new JLabel(
                                "Nhập tài khoản để tiếp tục",
                                SwingConstants.LEFT);

                subtitleLabel.setForeground(TEXT_COLOR);
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

                panel.add(
                                titleLabel,
                                BorderLayout.NORTH);

                panel.add(
                                subtitleLabel,
                                BorderLayout.CENTER);

                return panel;
        }

        private JPanel createFormPanel() {
                JPanel formPanel = new JPanel(
                                new GridBagLayout());

                formPanel.setOpaque(false);

                GridBagConstraints constraints = new GridBagConstraints();

                constraints.gridx = 0;
                constraints.weightx = 1.0;
                constraints.fill = GridBagConstraints.HORIZONTAL;

                JLabel usernameLabel = createFieldLabel(
                                "Tên đăng nhập");

                usernameField = new JTextField();
                configureTextField(usernameField);

                JLabel passwordLabel = createFieldLabel(
                                "Mật khẩu");

                passwordField = new JPasswordField();

                configureTextField(passwordField);

                char defaultEchoChar = passwordField.getEchoChar();

                showPasswordCheckBox = new JCheckBox(
                                "Hiện mật khẩu");

                showPasswordCheckBox.setOpaque(false);
                showPasswordCheckBox.setFocusPainted(false);
                showPasswordCheckBox.setForeground(TEXT_COLOR);
                showPasswordCheckBox.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.PLAIN,
                                                13));

                showPasswordCheckBox.setCursor(
                                Cursor.getPredefinedCursor(
                                                Cursor.HAND_CURSOR));

                showPasswordCheckBox.addActionListener(
                                event -> {
                                        if (showPasswordCheckBox
                                                        .isSelected()) {

                                                passwordField.setEchoChar(
                                                                (char) 0);

                                        } else {
                                                passwordField.setEchoChar(
                                                                defaultEchoChar);
                                        }
                                });

                messageLabel = new JLabel(" ");
                messageLabel.setForeground(ERROR_COLOR);
                messageLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.PLAIN,
                                                13));

                loginButton = new JButton("ĐĂNG NHẬP");

                configureLoginButton();

                constraints.gridy = 0;
                constraints.insets = new Insets(0, 0, 8, 0);

                formPanel.add(
                                usernameLabel,
                                constraints);

                constraints.gridy = 1;
                constraints.insets = new Insets(0, 0, 20, 0);

                formPanel.add(
                                usernameField,
                                constraints);

                constraints.gridy = 2;
                constraints.insets = new Insets(0, 0, 8, 0);

                formPanel.add(
                                passwordLabel,
                                constraints);

                constraints.gridy = 3;
                constraints.insets = new Insets(0, 0, 10, 0);

                formPanel.add(
                                passwordField,
                                constraints);

                constraints.gridy = 4;
                constraints.insets = new Insets(0, 0, 8, 0);

                formPanel.add(
                                showPasswordCheckBox,
                                constraints);

                constraints.gridy = 5;
                constraints.insets = new Insets(0, 0, 16, 0);

                formPanel.add(
                                messageLabel,
                                constraints);

                constraints.gridy = 6;
                constraints.insets = new Insets(0, 0, 0, 0);

                formPanel.add(
                                loginButton,
                                constraints);

                usernameField.addActionListener(
                                event -> passwordField
                                                .requestFocusInWindow());

                passwordField.addActionListener(
                                event -> handleLogin());

                getRootPane().setDefaultButton(
                                loginButton);

                return formPanel;
        }

        private JPanel createFooterPanel() {
                JPanel footerPanel = new JPanel(new BorderLayout());

                footerPanel.setOpaque(false);
                footerPanel.setBorder(
                                new EmptyBorder(
                                                28,
                                                0,
                                                0,
                                                0));

                JLabel footerLabel = new JLabel(
                                "Coffee Shop Management System",
                                SwingConstants.CENTER);

                footerLabel.setForeground(
                                new Color(140, 151, 165));

                footerLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.PLAIN,
                                                12));

                footerPanel.add(
                                footerLabel,
                                BorderLayout.CENTER);

                return footerPanel;
        }

        private JLabel createFieldLabel(
                        String text) {

                JLabel label = new JLabel(text);

                label.setForeground(TITLE_COLOR);
                label.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                14));

                return label;
        }

        private void configureTextField(
                        JTextField textField) {

                textField.setPreferredSize(
                                new Dimension(360, 48));

                textField.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.PLAIN,
                                                15));

                textField.setForeground(TITLE_COLOR);
                textField.setBackground(Color.WHITE);
                textField.setCaretColor(PRIMARY);

                setNormalBorder(textField);

                textField.addFocusListener(
                                new FocusAdapter() {

                                        @Override
                                        public void focusGained(
                                                        FocusEvent event) {

                                                textField.setBorder(
                                                                BorderFactory
                                                                                .createCompoundBorder(
                                                                                                BorderFactory
                                                                                                                .createLineBorder(
                                                                                                                                PRIMARY,
                                                                                                                                2,
                                                                                                                                true),
                                                                                                new EmptyBorder(
                                                                                                                0,
                                                                                                                13,
                                                                                                                0,
                                                                                                                13)));
                                        }

                                        @Override
                                        public void focusLost(
                                                        FocusEvent event) {

                                                setNormalBorder(
                                                                textField);
                                        }
                                });
        }

        private void setNormalBorder(
                        JTextField textField) {

                textField.setBorder(
                                BorderFactory
                                                .createCompoundBorder(
                                                                BorderFactory
                                                                                .createLineBorder(
                                                                                                BORDER_COLOR,
                                                                                                1,
                                                                                                true),
                                                                new EmptyBorder(
                                                                                0,
                                                                                13,
                                                                                0,
                                                                                13)));
        }

        private void configureLoginButton() {
                loginButton.setPreferredSize(
                                new Dimension(360, 50));

                loginButton.setForeground(Color.WHITE);
                loginButton.setBackground(PRIMARY);

                loginButton.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                15));

                loginButton.setFocusPainted(false);
                loginButton.setBorderPainted(false);
                loginButton.setContentAreaFilled(true);
                loginButton.setOpaque(true);

                loginButton.setCursor(
                                Cursor.getPredefinedCursor(
                                                Cursor.HAND_CURSOR));

                loginButton.addActionListener(
                                event -> handleLogin());

                loginButton.addMouseListener(
                                new MouseAdapter() {

                                        @Override
                                        public void mouseEntered(
                                                        MouseEvent event) {

                                                if (loginButton.isEnabled()) {
                                                        loginButton.setBackground(
                                                                        PRIMARY_HOVER);
                                                }
                                        }

                                        @Override
                                        public void mouseExited(
                                                        MouseEvent event) {

                                                if (loginButton.isEnabled()) {
                                                        loginButton.setBackground(
                                                                        PRIMARY);
                                                }
                                        }

                                        @Override
                                        public void mousePressed(
                                                        MouseEvent event) {

                                                if (loginButton.isEnabled()) {
                                                        loginButton.setBackground(
                                                                        PRIMARY_DARK);
                                                }
                                        }

                                        @Override
                                        public void mouseReleased(
                                                        MouseEvent event) {

                                                if (loginButton.isEnabled()) {
                                                        loginButton.setBackground(
                                                                        PRIMARY_HOVER);
                                                }
                                        }
                                });
        }

        private void handleLogin() {
                String username = usernameField
                                .getText()
                                .trim();

                String password = new String(
                                passwordField
                                                .getPassword());

                messageLabel.setText(" ");

                if (username.isBlank()) {
                        showValidationError(
                                        "Vui lòng nhập tên đăng nhập.",
                                        usernameField);

                        return;
                }

                if (password.isBlank()) {
                        showValidationError(
                                        "Vui lòng nhập mật khẩu.",
                                        passwordField);

                        return;
                }

                setLoginFormEnabled(false);
                loginButton.setText(
                                "ĐANG ĐĂNG NHẬP...");

                try {
                        LoginResult result = authService.login(
                                        username,
                                        password);

                        if (!result.success()) {
                                messageLabel.setText(
                                                result.message());

                                passwordField.setText("");
                                passwordField
                                                .requestFocusInWindow();

                                return;
                        }

                        SessionManager.login(
                                        result.user());

                        dispose();

                        DashboardFrame dashboardFrame = new DashboardFrame();

                        dashboardFrame.setVisible(true);

                } catch (IllegalStateException exception) {
                        JOptionPane.showMessageDialog(
                                        this,
                                        "Không thể kết nối hệ thống.\n"
                                                        + exception.getMessage(),
                                        "Lỗi hệ thống",
                                        JOptionPane.ERROR_MESSAGE);

                } finally {
                        setLoginFormEnabled(true);

                        loginButton.setText(
                                        "ĐĂNG NHẬP");

                        loginButton.setBackground(
                                        PRIMARY);
                }
        }

        private void showValidationError(
                        String message,
                        JTextField field) {

                messageLabel.setText(message);
                field.requestFocusInWindow();
        }

        private void setLoginFormEnabled(
                        boolean enabled) {

                usernameField.setEnabled(enabled);
                passwordField.setEnabled(enabled);
                showPasswordCheckBox.setEnabled(enabled);
                loginButton.setEnabled(enabled);
        }

        private static final class GradientBrandPanel
                        extends JPanel {

                @Override
                protected void paintComponent(
                                Graphics graphics) {

                        Graphics2D graphics2D = (Graphics2D) graphics.create();

                        graphics2D.setRenderingHint(
                                        RenderingHints.KEY_RENDERING,
                                        RenderingHints.VALUE_RENDER_QUALITY);

                        GradientPaint gradientPaint = new GradientPaint(
                                        0,
                                        0,
                                        new Color(0, 132, 222),
                                        getWidth(),
                                        getHeight(),
                                        new Color(0, 82, 155));

                        graphics2D.setPaint(gradientPaint);

                        graphics2D.fillRect(
                                        0,
                                        0,
                                        getWidth(),
                                        getHeight());

                        graphics2D.dispose();
                }
        }

        private static final class RoundedPanel
                        extends JPanel {

                private final int arc;
                private final Color backgroundColor;

                private RoundedPanel(
                                int arc,
                                Color backgroundColor) {

                        this.arc = arc;
                        this.backgroundColor = backgroundColor;

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
                                        new Color(
                                                        20,
                                                        45,
                                                        80,
                                                        25));

                        graphics2D.fillRoundRect(
                                        6,
                                        8,
                                        getWidth() - 12,
                                        getHeight() - 12,
                                        arc,
                                        arc);

                        graphics2D.setColor(
                                        backgroundColor);

                        graphics2D.fillRoundRect(
                                        0,
                                        0,
                                        getWidth() - 8,
                                        getHeight() - 8,
                                        arc,
                                        arc);

                        graphics2D.setColor(
                                        new Color(
                                                        220,
                                                        227,
                                                        236));

                        graphics2D.setStroke(
                                        new BasicStroke(1F));

                        graphics2D.drawRoundRect(
                                        0,
                                        0,
                                        getWidth() - 9,
                                        getHeight() - 9,
                                        arc,
                                        arc);

                        graphics2D.dispose();

                        super.paintComponent(graphics);
                }
        }
}