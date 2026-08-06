package com.csms.view.waiter.panel;

import com.csms.view.waiter.model.CartModel;

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
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class OrderSummaryPanel extends JPanel {

    private final CartModel cartModel;

    private final JLabel quantityValueLabel;
    private final JLabel subtotalValueLabel;
    private final JLabel vatValueLabel;
    private final JLabel totalValueLabel;

    private final JTextArea orderNoteArea;

    private final JButton clearButton;
    private final JButton sendButton;

    public OrderSummaryPanel(
            CartModel cartModel,
            Runnable sendHandler) {
        this.cartModel = cartModel;

        quantityValueLabel = createValueLabel();
        subtotalValueLabel = createValueLabel();
        vatValueLabel = createValueLabel();
        totalValueLabel = createValueLabel();

        orderNoteArea = new JTextArea();

        clearButton = new JButton("Xóa toàn bộ");
        sendButton = new JButton(
                "XÁC NHẬN VÀ GỬI BARISTA");

        initializeComponents();
        registerEvents(sendHandler);
        updateSummary();
    }

    private void initializeComponents() {
        setLayout(
                new BorderLayout(0, 12));

        setBackground(Color.WHITE);

        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                1,
                                0,
                                0,
                                0,
                                new Color(226, 230, 237)),
                        BorderFactory.createEmptyBorder(
                                14,
                                12,
                                14,
                                12)));

        JPanel summaryGrid = new JPanel(
                new GridLayout(
                        4,
                        2,
                        0,
                        8));

        summaryGrid.setOpaque(false);

        summaryGrid.add(
                new JLabel("Tổng số lượng:"));

        summaryGrid.add(quantityValueLabel);

        summaryGrid.add(
                new JLabel("Tạm tính:"));

        summaryGrid.add(subtotalValueLabel);

        summaryGrid.add(
                new JLabel("VAT:"));

        summaryGrid.add(vatValueLabel);

        JLabel totalTitleLabel = new JLabel("TỔNG TIỀN:");

        totalTitleLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        16));

        totalValueLabel.setForeground(
                new Color(37, 99, 235));

        totalValueLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        18));

        summaryGrid.add(totalTitleLabel);
        summaryGrid.add(totalValueLabel);

        JPanel notePanel = new JPanel(
                new BorderLayout(0, 5));

        notePanel.setOpaque(false);

        notePanel.add(
                new JLabel("Ghi chú chung:"),
                BorderLayout.NORTH);

        orderNoteArea.setRows(3);
        orderNoteArea.setLineWrap(true);
        orderNoteArea.setWrapStyleWord(true);

        orderNoteArea.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(209, 213, 219)),
                        BorderFactory.createEmptyBorder(
                                7,
                                7,
                                7,
                                7)));

        notePanel.add(
                new javax.swing.JScrollPane(
                        orderNoteArea),
                BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(
                new GridLayout(
                        1,
                        2,
                        8,
                        0));

        buttonPanel.setOpaque(false);

        sendButton.setPreferredSize(
                new Dimension(0, 48));

        sendButton.setBackground(
                new Color(37, 99, 235));

        sendButton.setForeground(Color.WHITE);

        sendButton.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        13));

        buttonPanel.add(clearButton);
        buttonPanel.add(sendButton);

        JPanel centerPanel = new JPanel(
                new BorderLayout(0, 10));

        centerPanel.setOpaque(false);

        centerPanel.add(
                summaryGrid,
                BorderLayout.NORTH);

        centerPanel.add(
                notePanel,
                BorderLayout.CENTER);

        add(
                centerPanel,
                BorderLayout.CENTER);

        add(
                buttonPanel,
                BorderLayout.SOUTH);
    }

    private void registerEvents(
            Runnable sendHandler) {
        cartModel.addChangeListener(
                event -> updateSummary());

        clearButton.addActionListener(
                event -> cartModel.clear());

        sendButton.addActionListener(
                event -> {
                    if (sendHandler != null) {
                        sendHandler.run();
                    }
                });
    }

    private void updateSummary() {
        quantityValueLabel.setText(
                String.valueOf(
                        cartModel.getTotalQuantity()));

        subtotalValueLabel.setText(
                formatMoney(
                        cartModel.getSubtotal()));

        vatValueLabel.setText(
                formatMoney(
                        cartModel.getVatAmount()));

        totalValueLabel.setText(
                formatMoney(
                        cartModel.getTotalAmount()));

        clearButton.setEnabled(
                !cartModel.isEmpty());

        sendButton.setEnabled(
                !cartModel.isEmpty());
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("0");

        label.setHorizontalAlignment(
                JLabel.RIGHT);

        return label;
    }

    public String getOrderNote() {
        return orderNoteArea
                .getText()
                .trim();
    }

    private String formatMoney(
            BigDecimal value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag(
                        "vi-VN"));

        return formatter.format(
                value == null
                        ? BigDecimal.ZERO
                        : value);
    }
}