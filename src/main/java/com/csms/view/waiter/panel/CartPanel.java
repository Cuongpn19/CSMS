package com.csms.view.waiter.panel;

import com.csms.view.waiter.component.CartItemPanel;
import com.csms.view.waiter.model.CartItem;
import com.csms.view.waiter.model.CartModel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

public class CartPanel extends JPanel {

    private final CartModel cartModel;

    private final JPanel itemContainer;
    private final JLabel emptyLabel;

    public CartPanel(CartModel cartModel) {
        this.cartModel = cartModel;

        itemContainer = new JPanel();

        emptyLabel = new JLabel(
                "Chưa có món nào trong đơn",
                JLabel.CENTER);

        initializeComponents();
        registerEvents();
        rebuildItems();
    }

    private void initializeComponents() {
        setLayout(
                new BorderLayout(0, 10));

        setBackground(Color.WHITE);

        setBorder(
                BorderFactory.createEmptyBorder(
                        12,
                        12,
                        12,
                        12));

        JLabel titleLabel = new JLabel("MÓN ĐÃ CHỌN");

        titleLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        17));

        itemContainer.setLayout(
                new BoxLayout(
                        itemContainer,
                        BoxLayout.Y_AXIS));

        itemContainer.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(itemContainer);

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder());

        scrollPane.getVerticalScrollBar()
                .setUnitIncrement(16);

        add(
                titleLabel,
                BorderLayout.NORTH);

        add(
                scrollPane,
                BorderLayout.CENTER);
    }

    private void registerEvents() {
        cartModel.addChangeListener(
                event -> rebuildItems());
    }

    private void rebuildItems() {
        itemContainer.removeAll();

        if (cartModel.isEmpty()) {
            emptyLabel.setForeground(
                    new Color(107, 114, 128));

            itemContainer.add(
                    Box.createVerticalStrut(30));

            itemContainer.add(emptyLabel);

        } else {
            for (CartItem item : cartModel.getItems()) {

                int productId = item.getProduct().getId();

                CartItemPanel itemPanel = new CartItemPanel(
                        item,

                        () -> cartModel
                                .increaseQuantity(
                                        productId),

                        () -> cartModel
                                .decreaseQuantity(
                                        productId),

                        () -> cartModel
                                .removeProduct(
                                        productId),

                        note -> cartModel
                                .updateNote(
                                        productId,
                                        note));

                itemContainer.add(itemPanel);
            }
        }

        itemContainer.revalidate();
        itemContainer.repaint();
    }
}