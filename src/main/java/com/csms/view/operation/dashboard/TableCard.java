package com.csms.view.operation.dashboard;

import com.csms.entity.OrderStatus;
import com.csms.entity.TableDashboardItem;
import com.csms.entity.TableStatus;
import com.csms.view.operation.common.DashboardMode;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

public class TableCard extends JPanel {

    private static final Color TEXT_COLOR = new Color(31, 41, 55);

    private static final Color SECONDARY_TEXT_COLOR = new Color(107, 114, 128);

    private static final Color BORDER_COLOR = new Color(226, 230, 237);

    private static final Color HOVER_BORDER_COLOR = new Color(37, 99, 235);

    private final TableDashboardItem dashboardItem;
    private final Consumer<TableDashboardItem> clickHandler;

    private final Color backgroundColor;
    private final DashboardMode dashboardMode;
    private final Color statusColor;

    public TableCard(
            TableDashboardItem dashboardItem,
            DashboardMode dashboardMode,
            Consumer<TableDashboardItem> clickHandler) {
        this.dashboardItem = dashboardItem;
        this.dashboardMode = dashboardMode;
        this.clickHandler = clickHandler;

        this.backgroundColor = resolveBackgroundColor(dashboardItem);

        this.statusColor = resolveStatusColor(dashboardItem);

        initializeComponents();
        registerEvents();
        updateInteractionState();
    }

    private void initializeComponents() {
        setLayout(
                new BorderLayout(0, 12));

        setPreferredSize(
                new Dimension(215, 190));

        setMinimumSize(
                new Dimension(190, 175));

        setBackground(backgroundColor);

        setBorder(
                createNormalBorder());

        setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR));

        add(
                createHeaderPanel(),
                BorderLayout.NORTH);

        add(
                createInformationPanel(),
                BorderLayout.CENTER);

        add(
                createFooterPanel(),
                BorderLayout.SOUTH);

        setToolTipText(
                createTooltip());
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(
                new BorderLayout());

        panel.setOpaque(false);

        JLabel tableLabel = new JLabel(
                String.format(
                        "BÀN %02d",
                        dashboardItem
                                .getTableNumber()));

        tableLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        19));

        tableLabel.setForeground(TEXT_COLOR);

        JLabel statusBadge = new JLabel(
                dashboardItem.getDisplayStatus());

        statusBadge.setOpaque(true);
        statusBadge.setBackground(statusColor);
        statusBadge.setForeground(Color.WHITE);

        statusBadge.setHorizontalAlignment(
                SwingConstants.CENTER);

        statusBadge.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        11));

        statusBadge.setBorder(
                BorderFactory.createEmptyBorder(
                        5,
                        9,
                        5,
                        9));

        panel.add(tableLabel, BorderLayout.WEST);
        panel.add(statusBadge, BorderLayout.EAST);

        return panel;
    }

    private JPanel createInformationPanel() {
        JPanel panel = new JPanel(
                new GridLayout(
                        3,
                        1,
                        0,
                        5));

        panel.setOpaque(false);

        JLabel orderLabel = createInformationLabel(
                dashboardItem.hasActiveOrder()
                        ? formatOrderCode()
                        : "Chưa có đơn hàng");

        JLabel itemLabel = createInformationLabel(
                dashboardItem.hasActiveOrder()
                        ? dashboardItem
                                .getTotalQuantity()
                                + " món"
                        : "Sẵn sàng phục vụ");

        JLabel waiterLabel = createInformationLabel(
                dashboardItem.getWaiterName() == null
                        || dashboardItem
                                .getWaiterName()
                                .isBlank()
                                        ? "-"
                                        : "Waiter: "
                                                + dashboardItem
                                                        .getWaiterName());

        panel.add(orderLabel);
        panel.add(itemLabel);
        panel.add(waiterLabel);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(
                new BorderLayout());

        panel.setOpaque(false);

        JLabel totalTitleLabel = new JLabel("Tổng tiền");

        totalTitleLabel.setForeground(
                SECONDARY_TEXT_COLOR);

        JLabel amountLabel = new JLabel(
                formatMoney(
                        dashboardItem
                                .getSafeTotalAmount()));

        amountLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        16));

        amountLabel.setForeground(
                dashboardItem.hasActiveOrder()
                        ? statusColor
                        : SECONDARY_TEXT_COLOR);

        panel.add(
                totalTitleLabel,
                BorderLayout.WEST);

        panel.add(
                amountLabel,
                BorderLayout.EAST);

        return panel;
    }

    private JLabel createInformationLabel(
            String text) {
        JLabel label = new JLabel(text);

        label.setForeground(
                SECONDARY_TEXT_COLOR);

        label.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.PLAIN,
                        13));

        return label;
    }

    private void registerEvents() {
        addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(
                            MouseEvent event) {
                        if (!isTableClickable()) {
                            return;
                        }

                        if (clickHandler != null) {
                            clickHandler.accept(
                                    dashboardItem);
                        }
                    }
                });
    }

    private boolean isTableClickable() {
        if (dashboardItem.getTableStatus() == TableStatus.INACTIVE) {

            return false;
        }

        if (dashboardItem.canCreateOrder()) {
            return true;
        }

        return dashboardItem.hasActiveOrder();
    }

    private void updateInteractionState() {
        boolean clickable = isTableClickable();

        setCursor(
                clickable
                        ? Cursor.getPredefinedCursor(
                                Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());

        if (!clickable) {
            setToolTipText(
                    resolveDisabledTooltip());
        }
    }

    private String resolveDisabledTooltip() {
        if (dashboardItem.getTableStatus() == TableStatus.INACTIVE) {

            return "Bàn đang ngừng sử dụng.";
        }

        if (dashboardItem.canCreateOrder()
                && dashboardMode.isReadOnly()) {

            return "Manager chỉ theo dõi bàn trống.";
        }

        return "Bàn hiện không thể thao tác.";
    }

    private javax.swing.border.Border createNormalBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        BORDER_COLOR),
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15));
    }

    private javax.swing.border.Border createHoverBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        HOVER_BORDER_COLOR,
                        2),
                BorderFactory.createEmptyBorder(
                        14,
                        14,
                        14,
                        14));
    }

    private String formatOrderCode() {
        if (dashboardItem.getOrderCode() != null
                && !dashboardItem
                        .getOrderCode()
                        .isBlank()) {

            return dashboardItem.getOrderCode();
        }

        return dashboardItem.getOrderId() == null
                ? "-"
                : "Đơn #"
                        + dashboardItem.getOrderId();
    }

    private String formatMoney(
            BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag(
                        "vi-VN"));

        return formatter.format(
                amount == null
                        ? BigDecimal.ZERO
                        : amount);
    }

    private String createTooltip() {
        if (dashboardItem.canCreateOrder()) {
            return "Nhấn để tạo đơn mới cho bàn "
                    + dashboardItem.getTableNumber();
        }

        if (dashboardItem.hasActiveOrder()) {
            return "Nhấn để xem chi tiết đơn của bàn "
                    + dashboardItem.getTableNumber();
        }

        return "Bàn hiện không thể thao tác.";
    }

    private Color resolveBackgroundColor(
            TableDashboardItem item) {
        if (item.getTableStatus() == TableStatus.INACTIVE) {

            return new Color(243, 244, 246);
        }

        OrderStatus status = item.getOrderStatus();

        if (status == null) {
            return Color.WHITE;
        }

        return switch (status) {
            case IN_PROGRESS ->
                new Color(239, 246, 255);

            case PREPARING ->
                new Color(255, 247, 237);

            case PREPARED ->
                new Color(253, 242, 248);

            case SERVED, WAITING_PAYMENT ->
                new Color(240, 253, 244);

            case CANCEL_PENDING ->
                new Color(254, 242, 242);

            default -> Color.WHITE;
        };
    }

    private Color resolveStatusColor(
            TableDashboardItem item) {
        if (item.getTableStatus() == TableStatus.INACTIVE) {

            return new Color(107, 114, 128);
        }

        OrderStatus status = item.getOrderStatus();

        if (status == null) {
            return new Color(100, 116, 139);
        }

        return switch (status) {
            case IN_PROGRESS ->
                new Color(37, 99, 235);

            case PREPARING ->
                new Color(234, 88, 12);

            case PREPARED ->
                new Color(219, 39, 119);

            case SERVED, WAITING_PAYMENT ->
                new Color(22, 163, 74);

            case CANCEL_PENDING ->
                new Color(220, 38, 38);

            default ->
                new Color(100, 116, 139);
        };
    }
}