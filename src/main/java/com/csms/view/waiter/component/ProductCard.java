package com.csms.view.waiter.component;

import com.csms.entity.Product;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

public class ProductCard extends JPanel {

    private static final Color NORMAL_BORDER = new Color(226, 230, 237);

    private static final Color HOVER_BORDER = new Color(37, 99, 235);

    private static final Color NORMAL_BACKGROUND = Color.WHITE;

    private static final Color HOVER_BACKGROUND = new Color(239, 246, 255);

    private final Product product;
    private final Consumer<Product> clickHandler;

    public ProductCard(
            Product product,
            Consumer<Product> clickHandler) {
        this.product = product;
        this.clickHandler = clickHandler;

        initializeComponents();
        registerEvents();
    }

    private void initializeComponents() {
        setLayout(
                new BorderLayout(0, 9));

        setBackground(NORMAL_BACKGROUND);

        setPreferredSize(
                new Dimension(185, 225));

        setBorder(createNormalBorder());

        setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR));

        JLabel imageLabel = new JLabel(
                loadProductImage());

        imageLabel.setHorizontalAlignment(
                JLabel.CENTER);

        imageLabel.setPreferredSize(
                new Dimension(160, 125));

        JPanel informationPanel = new JPanel(
                new BorderLayout(0, 5));

        informationPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(product.getName());

        nameLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        14));

        nameLabel.setToolTipText(
                product.getName());

        JLabel priceLabel = new JLabel(
                formatMoney(
                        product.getPrice()));

        priceLabel.setForeground(
                new Color(37, 99, 235));

        priceLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        14));

        informationPanel.add(
                nameLabel,
                BorderLayout.NORTH);

        informationPanel.add(
                priceLabel,
                BorderLayout.SOUTH);

        add(
                imageLabel,
                BorderLayout.CENTER);

        add(
                informationPanel,
                BorderLayout.SOUTH);
    }

    private void registerEvents() {
        addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseEntered(
                            MouseEvent event) {
                        setBackground(
                                HOVER_BACKGROUND);

                        setBorder(
                                createHoverBorder());
                    }

                    @Override
                    public void mouseExited(
                            MouseEvent event) {
                        setBackground(
                                NORMAL_BACKGROUND);

                        setBorder(
                                createNormalBorder());
                    }

                    @Override
                    public void mouseClicked(
                            MouseEvent event) {
                        if (clickHandler != null) {
                            clickHandler.accept(product);
                        }
                    }
                });
    }

    private javax.swing.border.Border createNormalBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        NORMAL_BORDER),
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        10,
                        10));
    }

    private javax.swing.border.Border createHoverBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        HOVER_BORDER,
                        2),
                BorderFactory.createEmptyBorder(
                        9,
                        9,
                        9,
                        9));
    }

    private ImageIcon loadProductImage() {
        int width = 155;
        int height = 120;

        try {
            String imagePath = product.getImage();

            if (imagePath != null
                    && !imagePath.isBlank()) {

                File file = new File(imagePath);

                if (file.exists()) {
                    BufferedImage bufferedImage = ImageIO.read(file);

                    return scaleImage(
                            bufferedImage,
                            width,
                            height);
                }

                InputStream resourceStream = getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                                imagePath);

                if (resourceStream != null) {
                    try (resourceStream) {
                        BufferedImage bufferedImage = ImageIO.read(
                                resourceStream);

                        return scaleImage(
                                bufferedImage,
                                width,
                                height);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        BufferedImage placeholder = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_RGB);

        java.awt.Graphics2D graphics = placeholder.createGraphics();

        graphics.setColor(
                new Color(241, 245, 249));

        graphics.fillRect(
                0,
                0,
                width,
                height);

        graphics.setColor(
                new Color(100, 116, 139));

        graphics.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        13));

        String text = "Không có ảnh";

        int textWidth = graphics.getFontMetrics()
                .stringWidth(text);

        graphics.drawString(
                text,
                (width - textWidth) / 2,
                height / 2);

        graphics.dispose();

        return new ImageIcon(placeholder);
    }

    private ImageIcon scaleImage(
            BufferedImage image,
            int width,
            int height) {
        Image scaledImage = image.getScaledInstance(
                width,
                height,
                Image.SCALE_SMOOTH);

        return new ImageIcon(scaledImage);
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