package com.csms.view.waiter.panel;

import com.csms.entity.Product;
import com.csms.view.waiter.component.ProductCard;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;

public class ProductGridPanel extends JPanel {

    private final JPanel gridPanel;
    private final JLabel emptyLabel;

    public ProductGridPanel() {
        setLayout(
                new BorderLayout());

        setBackground(
                new Color(246, 248, 252));

        gridPanel = new JPanel(
                new GridLayout(
                        0,
                        3,
                        14,
                        14));

        gridPanel.setOpaque(false);

        gridPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        5,
                        5,
                        5,
                        5));

        emptyLabel = new JLabel(
                "Không tìm thấy sản phẩm.",
                JLabel.CENTER);

        emptyLabel.setForeground(
                new Color(107, 114, 128));

        add(
                gridPanel,
                BorderLayout.NORTH);
    }

    public void displayProducts(
            List<Product> products,
            Consumer<Product> clickHandler) {
        gridPanel.removeAll();

        if (products == null
                || products.isEmpty()) {

            remove(emptyLabel);
            add(
                    emptyLabel,
                    BorderLayout.CENTER);

        } else {
            remove(emptyLabel);

            for (Product product : products) {
                gridPanel.add(
                        new ProductCard(
                                product,
                                clickHandler));
            }
        }

        revalidate();
        repaint();
    }
}