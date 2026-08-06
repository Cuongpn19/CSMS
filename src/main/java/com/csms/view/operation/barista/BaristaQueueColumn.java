package com.csms.view.operation.barista;

import com.csms.dto.BaristaOrderGroup;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import java.util.function.Consumer;

public class BaristaQueueColumn extends JPanel {

    private final JLabel countLabel;
    private final JPanel cardContainer;

    public BaristaQueueColumn(
            String title,
            Color accentColor) {
        setLayout(
                new BorderLayout(0, 10));

        setBackground(
                new Color(248, 250, 252));

        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(226, 232, 240)),
                        BorderFactory.createEmptyBorder(
                                12,
                                12,
                                12,
                                12)));

        JPanel headerPanel = new JPanel(
                new BorderLayout());

        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        16));

        titleLabel.setForeground(accentColor);

        countLabel = new JLabel("0 đơn");

        countLabel.setForeground(
                new Color(100, 116, 139));

        headerPanel.add(
                titleLabel,
                BorderLayout.WEST);

        headerPanel.add(
                countLabel,
                BorderLayout.EAST);

        cardContainer = new JPanel();

        cardContainer.setLayout(
                new BoxLayout(
                        cardContainer,
                        BoxLayout.Y_AXIS));

        cardContainer.setBackground(
                new Color(248, 250, 252));

        JScrollPane scrollPane = new JScrollPane(cardContainer);

        scrollPane.setBorder(null);

        scrollPane.getVerticalScrollBar()
                .setUnitIncrement(18);

        scrollPane.getViewport()
                .setBackground(
                        new Color(248, 250, 252));

        add(
                headerPanel,
                BorderLayout.NORTH);

        add(
                scrollPane,
                BorderLayout.CENTER);
    }

    public void displayOrders(
            List<BaristaOrderGroup> orders,
            Consumer<BaristaOrderGroup> actionHandler) {
        cardContainer.removeAll();

        int count = orders == null
                ? 0
                : orders.size();

        countLabel.setText(
                count + " đơn");

        if (count == 0) {
            JLabel emptyLabel = new JLabel(
                    "Không có đơn",
                    JLabel.CENTER);

            emptyLabel.setForeground(
                    new Color(148, 163, 184));

            cardContainer.add(
                    Box.createVerticalStrut(30));

            cardContainer.add(emptyLabel);

        } else {
            for (BaristaOrderGroup order : orders) {

                cardContainer.add(
                        new BaristaOrderCard(
                                order,
                                actionHandler));

                cardContainer.add(
                        Box.createVerticalStrut(12));
            }
        }

        cardContainer.revalidate();
        cardContainer.repaint();
    }
}