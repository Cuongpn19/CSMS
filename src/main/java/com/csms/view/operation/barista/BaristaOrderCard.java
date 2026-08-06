package com.csms.view.operation.barista;

import com.csms.dto.BaristaOrderGroup;
import com.csms.dto.BaristaOrderItem;
import com.csms.entity.OrderItemStatus;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class BaristaOrderCard extends JPanel {

    private final BaristaOrderGroup orderGroup;
    private final Consumer<BaristaOrderGroup> actionHandler;

    public BaristaOrderCard(
            BaristaOrderGroup orderGroup,
            Consumer<BaristaOrderGroup> actionHandler) {
        this.orderGroup = orderGroup;
        this.actionHandler = actionHandler;

        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(
                new BorderLayout(0, 12));

        setBackground(Color.WHITE);

        setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        330));

        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                resolveAccentColor()),
                        BorderFactory.createEmptyBorder(
                                14,
                                14,
                                14,
                                14)));

        add(
                createHeaderPanel(),
                BorderLayout.NORTH);

        add(
                createItemsPanel(),
                BorderLayout.CENTER);

        add(
                createActionPanel(),
                BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(
                new GridLayout(
                        3,
                        1,
                        0,
                        4));

        panel.setOpaque(false);

        String orderLabel = orderGroup.orderCode() == null
                || orderGroup.orderCode().isBlank()
                        ? "ĐƠN #" + orderGroup.orderId()
                        : orderGroup.orderCode();

        JLabel codeLabel = new JLabel(orderLabel);

        codeLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        16));

        JLabel tableLabel = new JLabel(
                String.format(
                        "Bàn %02d • %d món",
                        orderGroup.tableNumber(),
                        orderGroup.getTotalQuantity()));

        tableLabel.setForeground(
                new Color(75, 85, 99));

        JLabel timeLabel = new JLabel(
                formatOrderTime());

        timeLabel.setForeground(
                resolveAccentColor());

        panel.add(codeLabel);
        panel.add(tableLabel);
        panel.add(timeLabel);

        return panel;
    }

    private JPanel createItemsPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(
                new javax.swing.BoxLayout(
                        panel,
                        javax.swing.BoxLayout.Y_AXIS));

        panel.setOpaque(false);

        for (BaristaOrderItem item : orderGroup.items()) {

            JPanel itemPanel = new JPanel(
                    new BorderLayout(
                            8,
                            3));

            itemPanel.setOpaque(false);

            JLabel nameLabel = new JLabel(
                    item.productName());

            nameLabel.setFont(
                    new Font(
                            Font.SANS_SERIF,
                            Font.BOLD,
                            13));

            JLabel quantityLabel = new JLabel(
                    "x" + item.quantity());

            quantityLabel.setFont(
                    new Font(
                            Font.SANS_SERIF,
                            Font.BOLD,
                            13));

            itemPanel.add(
                    nameLabel,
                    BorderLayout.WEST);

            itemPanel.add(
                    quantityLabel,
                    BorderLayout.EAST);

            panel.add(itemPanel);

            if (item.note() != null
                    && !item.note().isBlank()) {

                JTextArea noteArea = new JTextArea(
                        "Ghi chú: "
                                + item.note());

                noteArea.setEditable(false);
                noteArea.setOpaque(false);
                noteArea.setLineWrap(true);
                noteArea.setWrapStyleWord(true);

                noteArea.setForeground(
                        new Color(220, 38, 38));

                noteArea.setFont(
                        new Font(
                                Font.SANS_SERIF,
                                Font.ITALIC,
                                12));

                panel.add(noteArea);
            }

            panel.add(
                    javax.swing.Box
                            .createVerticalStrut(7));
        }

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(
                new BorderLayout());

        panel.setOpaque(false);

        JButton actionButton = new JButton(
                resolveActionText());

        actionButton.setPreferredSize(
                new Dimension(0, 42));

        actionButton.setBackground(
                resolveAccentColor());

        actionButton.setForeground(
                Color.WHITE);

        actionButton.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        13));

        boolean actionable = orderGroup.queueStatus() != OrderItemStatus.PREPARED;

        actionButton.setEnabled(actionable);
        actionButton.setVisible(actionable);

        actionButton.addActionListener(
                event -> {
                    if (actionHandler != null) {
                        actionHandler.accept(
                                orderGroup);
                    }
                });

        panel.add(
                actionButton,
                BorderLayout.CENTER);

        return panel;
    }

    private String resolveActionText() {
        return switch (orderGroup.queueStatus()) {
            case IN_PROGRESS ->
                "NHẬN PHA CHẾ";

            case PREPARING ->
                "HOÀN THÀNH PHA CHẾ";

            case PREPARED ->
                "ĐÃ PHA XONG";

            default ->
                "CẬP NHẬT";
        };
    }

    private Color resolveAccentColor() {
        return switch (orderGroup.queueStatus()) {
            case IN_PROGRESS ->
                new Color(37, 99, 235);

            case PREPARING ->
                new Color(234, 88, 12);

            case PREPARED ->
                new Color(219, 39, 119);

            default ->
                new Color(100, 116, 139);
        };
    }

    private String formatOrderTime() {
        if (orderGroup.createdAt() == null) {
            return "-";
        }

        long minutes = Math.max(
                0,
                Duration.between(
                        orderGroup.createdAt(),
                        LocalDateTime.now()).toMinutes());

        String time = orderGroup.createdAt()
                .format(
                        DateTimeFormatter.ofPattern(
                                "HH:mm"));

        return time + " • "
                + minutes
                + " phút trước";
    }
}