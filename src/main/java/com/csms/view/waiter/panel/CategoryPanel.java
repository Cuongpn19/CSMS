package com.csms.view.waiter.panel;

import com.csms.entity.Category;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.function.Consumer;

public class CategoryPanel extends JPanel {

    private static final Integer ALL_CATEGORY_ID = null;

    private final ButtonGroup buttonGroup;

    public CategoryPanel() {
        buttonGroup = new ButtonGroup();

        setLayout(
                new BoxLayout(
                        this,
                        BoxLayout.Y_AXIS));

        setBackground(Color.WHITE);

        setBorder(
                BorderFactory.createEmptyBorder(
                        12,
                        10,
                        12,
                        10));
    }

    public void displayCategories(
            List<Category> categories,
            Consumer<Integer> selectionHandler) {
        removeAll();

        buttonGroup.clearSelection();

        JToggleButton allButton = createCategoryButton(
                "Tất cả");

        allButton.addActionListener(
                event -> {
                    if (selectionHandler != null) {
                        selectionHandler.accept(
                                ALL_CATEGORY_ID);
                    }
                });

        buttonGroup.add(allButton);
        add(allButton);
        add(Box.createVerticalStrut(7));

        allButton.setSelected(true);

        if (categories != null) {
            for (Category category : categories) {
                JToggleButton button = createCategoryButton(
                        category.getName());

                button.addActionListener(
                        event -> {
                            if (selectionHandler != null) {

                                selectionHandler.accept(
                                        category.getId());
                            }
                        });

                buttonGroup.add(button);
                add(button);
                add(Box.createVerticalStrut(7));
            }
        }

        add(Box.createVerticalGlue());

        revalidate();
        repaint();
    }

    private JToggleButton createCategoryButton(
            String text) {
        JToggleButton button = new JToggleButton(text);

        button.setFocusPainted(false);

        button.setHorizontalAlignment(
                JButton.LEFT);

        button.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        42));

        button.setPreferredSize(
                new Dimension(150, 42));

        button.putClientProperty(
                "JButton.buttonType",
                "roundRect");

        return button;
    }
}