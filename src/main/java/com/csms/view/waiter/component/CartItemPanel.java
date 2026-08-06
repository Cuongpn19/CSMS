package com.csms.view.waiter.component;

import com.csms.view.waiter.model.CartItem;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

public class CartItemPanel extends JPanel {

    private final CartItem item;

    private final Runnable increaseHandler;
    private final Runnable decreaseHandler;
    private final Runnable removeHandler;
    private final Consumer<String> noteHandler;

    public CartItemPanel(
            CartItem item,
            Runnable increaseHandler,
            Runnable decreaseHandler,
            Runnable removeHandler,
            Consumer<String> noteHandler) {
        this.item = item;

        this.increaseHandler = increaseHandler;
        this.decreaseHandler = decreaseHandler;
        this.removeHandler = removeHandler;
        this.noteHandler = noteHandler;

        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(
                new BorderLayout(8, 8));

        setBackground(Color.WHITE);

        setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        125));

        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0,
                                0,
                                1,
                                0,
                                new Color(229, 231, 235)),
                        BorderFactory.createEmptyBorder(
                                10,
                                5,
                                10,
                                5)));

        add(
                createInformationPanel(),
                BorderLayout.NORTH);

        add(
                createActionPanel(),
                BorderLayout.CENTER);

        add(
                createNotePanel(),
                BorderLayout.SOUTH);
    }

    private JPanel createInformationPanel() {
        JPanel panel = new JPanel(
                new BorderLayout());

        panel.setOpaque(false);

        JLabel nameLabel = new JLabel(
                item.getProduct().getName());

        nameLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        14));

        JLabel subtotalLabel = new JLabel(
                formatMoney(
                        item.getSubtotal()));

        subtotalLabel.setForeground(
                new Color(37, 99, 235));

        subtotalLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        14));

        panel.add(
                nameLabel,
                BorderLayout.WEST);

        panel.add(
                subtotalLabel,
                BorderLayout.EAST);

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(
                new BorderLayout());

        panel.setOpaque(false);

        JLabel unitPriceLabel = new JLabel(
                formatMoney(
                        item.getUnitPrice())
                        + " / món");

        unitPriceLabel.setForeground(
                new Color(107, 114, 128));

        JPanel quantityPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        5,
                        0));

        quantityPanel.setOpaque(false);

        JButton decreaseButton = new JButton("−");

        JLabel quantityLabel = new JLabel(
                String.valueOf(
                        item.getQuantity()));

        quantityLabel.setHorizontalAlignment(
                JLabel.CENTER);

        quantityLabel.setPreferredSize(
                new Dimension(30, 30));

        JButton increaseButton = new JButton("+");

        JButton removeButton = new JButton("Xóa");

        decreaseButton.setPreferredSize(
                new Dimension(42, 30));

        increaseButton.setPreferredSize(
                new Dimension(42, 30));

        decreaseButton.addActionListener(
                event -> {
                    if (decreaseHandler != null) {
                        decreaseHandler.run();
                    }
                });

        increaseButton.addActionListener(
                event -> {
                    if (increaseHandler != null) {
                        increaseHandler.run();
                    }
                });

        removeButton.addActionListener(
                event -> {
                    if (removeHandler != null) {
                        removeHandler.run();
                    }
                });

        quantityPanel.add(decreaseButton);
        quantityPanel.add(quantityLabel);
        quantityPanel.add(increaseButton);
        quantityPanel.add(removeButton);

        panel.add(
                unitPriceLabel,
                BorderLayout.WEST);

        panel.add(
                quantityPanel,
                BorderLayout.EAST);

        return panel;
    }

    private JPanel createNotePanel() {
        JPanel panel = new JPanel(
                new GridLayout(1, 1));

        panel.setOpaque(false);

        JTextField noteField = new JTextField(
                item.getNote());

        noteField.putClientProperty(
                "JTextField.placeholderText",
                "Ghi chú món: ít đá, ít đường...");

        noteField.addActionListener(
                event -> notifyNoteChanged(
                        noteField.getText()));

        noteField.addFocusListener(
                new java.awt.event.FocusAdapter() {
                    @Override
                    public void focusLost(
                            java.awt.event.FocusEvent event) {
                        notifyNoteChanged(
                                noteField.getText());
                    }
                });

        panel.add(noteField);

        return panel;
    }

    private void notifyNoteChanged(String note) {
        if (noteHandler != null) {
            noteHandler.accept(note);
        }
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