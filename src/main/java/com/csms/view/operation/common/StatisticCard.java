package com.csms.view.operation.common;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public class StatisticCard extends JPanel {

    private final JLabel valueLabel;
    private final JLabel titleLabel;

    public StatisticCard(
            String title,
            String initialValue,
            Color accentColor) {
        setLayout(
                new BorderLayout(0, 6));

        setBackground(Color.WHITE);

        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0,
                                5,
                                0,
                                0,
                                accentColor),
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(
                                        new Color(226, 230, 237)),
                                BorderFactory.createEmptyBorder(
                                        14,
                                        16,
                                        14,
                                        16))));

        setPreferredSize(
                new Dimension(180, 88));

        titleLabel = new JLabel(title);
        titleLabel.setForeground(
                new Color(100, 108, 120));

        titleLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.PLAIN,
                        13));

        valueLabel = new JLabel(initialValue);
        valueLabel.setHorizontalAlignment(
                SwingConstants.LEFT);

        valueLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        25));

        valueLabel.setForeground(
                new Color(31, 41, 55));

        add(titleLabel, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
    }

    public void setValue(int value) {
        valueLabel.setText(
                String.valueOf(value));
    }

    public void setValue(String value) {
        valueLabel.setText(
                value == null ? "0" : value);
    }
}