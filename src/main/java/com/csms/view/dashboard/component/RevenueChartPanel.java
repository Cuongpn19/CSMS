package com.csms.view.dashboard.component;

import com.csms.dto.DailyRevenue;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RevenueChartPanel extends JPanel {

    private static final int LEFT_PADDING = 75;
    private static final int RIGHT_PADDING = 25;
    private static final int TOP_PADDING = 35;
    private static final int BOTTOM_PADDING = 55;

    private static final Color PRIMARY_COLOR = new Color(0, 104, 255);

    private static final Color GRID_COLOR = new Color(225, 230, 238);

    private static final Color TEXT_COLOR = new Color(80, 88, 105);

    private static final Color CHART_BACKGROUND = Color.WHITE;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");

    private final DecimalFormat numberFormat = new DecimalFormat("#,##0.#");

    private List<DailyRevenue> revenueData;

    public RevenueChartPanel() {
        revenueData = new ArrayList<>();

        setOpaque(true);
        setBackground(CHART_BACKGROUND);

        setPreferredSize(
                new Dimension(600, 300));
    }

    public void setRevenueData(
            List<DailyRevenue> revenueData) {
        this.revenueData = revenueData == null
                ? new ArrayList<>()
                : new ArrayList<>(revenueData);

        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D graphics2D = (Graphics2D) graphics.create();

        try {
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            drawChart(graphics2D);

        } finally {
            graphics2D.dispose();
        }
    }

    private void drawChart(Graphics2D graphics2D) {
        int chartWidth = getWidth()
                - LEFT_PADDING
                - RIGHT_PADDING;

        int chartHeight = getHeight()
                - TOP_PADDING
                - BOTTOM_PADDING;

        if (chartWidth <= 0 || chartHeight <= 0) {
            return;
        }

        drawGrid(
                graphics2D,
                chartWidth,
                chartHeight);

        if (revenueData.isEmpty()) {
            drawEmptyMessage(graphics2D);
            return;
        }

        BigDecimal maximumRevenue = findMaximumRevenue();

        if (maximumRevenue.compareTo(
                BigDecimal.ZERO) <= 0) {
            maximumRevenue = BigDecimal.ONE;
        }

        drawBars(
                graphics2D,
                chartWidth,
                chartHeight,
                maximumRevenue);
    }

    private void drawGrid(
            Graphics2D graphics2D,
            int chartWidth,
            int chartHeight) {
        int gridLineCount = 4;

        graphics2D.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.PLAIN,
                        11));

        for (int index = 0; index <= gridLineCount; index++) {

            int y = TOP_PADDING
                    + index * chartHeight
                            / gridLineCount;

            graphics2D.setColor(GRID_COLOR);
            graphics2D.setStroke(
                    new BasicStroke(1F));

            graphics2D.drawLine(
                    LEFT_PADDING,
                    y,
                    LEFT_PADDING + chartWidth,
                    y);

            BigDecimal maximumRevenue = findMaximumRevenue();

            BigDecimal percentage = BigDecimal.valueOf(
                    gridLineCount - index).divide(
                            BigDecimal.valueOf(
                                    gridLineCount),
                            4,
                            RoundingMode.HALF_UP);

            BigDecimal gridValue = maximumRevenue.multiply(
                    percentage);

            String label = formatCompactMoney(gridValue);

            FontMetrics metrics = graphics2D.getFontMetrics();

            graphics2D.setColor(TEXT_COLOR);

            graphics2D.drawString(
                    label,
                    LEFT_PADDING
                            - metrics.stringWidth(label)
                            - 10,
                    y + metrics.getAscent() / 2);
        }
    }

    private void drawBars(
            Graphics2D graphics2D,
            int chartWidth,
            int chartHeight,
            BigDecimal maximumRevenue) {
        int itemCount = revenueData.size();

        int groupWidth = chartWidth / Math.max(itemCount, 1);

        int barWidth = Math.min(
                55,
                Math.max(
                        20,
                        groupWidth - 25));

        graphics2D.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.PLAIN,
                        11));

        for (int index = 0; index < itemCount; index++) {

            DailyRevenue item = revenueData.get(index);

            BigDecimal revenue = item.revenue() == null
                    ? BigDecimal.ZERO
                    : item.revenue();

            double ratio = revenue.divide(
                    maximumRevenue,
                    8,
                    RoundingMode.HALF_UP).doubleValue();

            ratio = Math.max(
                    0D,
                    Math.min(1D, ratio));

            int barHeight = (int) Math.round(
                    ratio * chartHeight);

            int x = LEFT_PADDING
                    + index * groupWidth
                    + (groupWidth - barWidth) / 2;

            int y = TOP_PADDING
                    + chartHeight
                    - barHeight;

            graphics2D.setColor(PRIMARY_COLOR);

            graphics2D.fillRoundRect(
                    x,
                    y,
                    barWidth,
                    barHeight,
                    12,
                    12);

            drawDateLabel(
                    graphics2D,
                    item,
                    x,
                    barWidth,
                    chartHeight);

            if (revenue.compareTo(
                    BigDecimal.ZERO) > 0) {
                drawRevenueLabel(
                        graphics2D,
                        revenue,
                        x,
                        y,
                        barWidth);
            }
        }
    }

    private void drawDateLabel(
            Graphics2D graphics2D,
            DailyRevenue item,
            int x,
            int barWidth,
            int chartHeight) {
        String dateText = item.date().format(
                dateFormatter);

        FontMetrics metrics = graphics2D.getFontMetrics();

        int textX = x
                + (barWidth
                        - metrics.stringWidth(dateText))
                        / 2;

        int textY = TOP_PADDING
                + chartHeight
                + 25;

        graphics2D.setColor(TEXT_COLOR);

        graphics2D.drawString(
                dateText,
                textX,
                textY);
    }

    private void drawRevenueLabel(
            Graphics2D graphics2D,
            BigDecimal revenue,
            int x,
            int y,
            int barWidth) {
        String revenueText = formatCompactMoney(revenue);

        Font oldFont = graphics2D.getFont();

        graphics2D.setFont(
                oldFont.deriveFont(
                        Font.BOLD,
                        10F));

        FontMetrics metrics = graphics2D.getFontMetrics();

        int textX = x
                + (barWidth
                        - metrics.stringWidth(
                                revenueText))
                        / 2;

        int textY = Math.max(
                TOP_PADDING - 5,
                y - 6);

        graphics2D.setColor(TEXT_COLOR);

        graphics2D.drawString(
                revenueText,
                textX,
                textY);

        graphics2D.setFont(oldFont);
    }

    private void drawEmptyMessage(
            Graphics2D graphics2D) {
        String message = "Chưa có dữ liệu doanh thu";

        graphics2D.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.PLAIN,
                        14));

        FontMetrics metrics = graphics2D.getFontMetrics();

        int x = (getWidth()
                - metrics.stringWidth(message))
                / 2;

        int y = getHeight() / 2;

        graphics2D.setColor(TEXT_COLOR);

        graphics2D.drawString(
                message,
                x,
                y);
    }

    private BigDecimal findMaximumRevenue() {
        BigDecimal maximum = BigDecimal.ZERO;

        for (DailyRevenue item : revenueData) {
            if (item.revenue() != null
                    && item.revenue()
                            .compareTo(maximum) > 0) {

                maximum = item.revenue();
            }
        }

        return maximum;
    }

    private String formatCompactMoney(
            BigDecimal value) {
        if (value == null) {
            return "0 ₫";
        }

        BigDecimal oneMillion = BigDecimal.valueOf(1_000_000);

        BigDecimal oneThousand = BigDecimal.valueOf(1_000);

        if (value.compareTo(oneMillion) >= 0) {
            return numberFormat.format(
                    value.divide(
                            oneMillion,
                            1,
                            RoundingMode.HALF_UP))
                    + " tr";
        }

        if (value.compareTo(oneThousand) >= 0) {
            return numberFormat.format(
                    value.divide(
                            oneThousand,
                            1,
                            RoundingMode.HALF_UP))
                    + "K";
        }

        return numberFormat.format(value) + " ₫";
    }
}