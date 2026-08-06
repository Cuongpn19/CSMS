package com.csms.view.operation.dashboard;

import com.csms.entity.TableDashboardItem;
import com.csms.view.operation.common.DashboardMode;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.function.Consumer;

public class TableGridPanel extends JPanel
        implements Scrollable {

    private static final int CARD_MINIMUM_WIDTH = 215;
    private static final int CARD_HEIGHT = 190;

    private static final int HORIZONTAL_GAP = 14;
    private static final int VERTICAL_GAP = 14;

    private static final int OUTER_PADDING = 8;

    private List<TableDashboardItem> items;

    private DashboardMode dashboardMode;

    private Consumer<TableDashboardItem> clickHandler;

    private int currentColumnCount;

    public TableGridPanel() {
        items = List.of();

        currentColumnCount = 1;

        setOpaque(false);

        setBorder(
                BorderFactory.createEmptyBorder(
                        OUTER_PADDING,
                        OUTER_PADDING,
                        OUTER_PADDING,
                        OUTER_PADDING));

        setLayout(
                new GridLayout(
                        0,
                        currentColumnCount,
                        HORIZONTAL_GAP,
                        VERTICAL_GAP));

        addComponentListener(
                new ComponentAdapter() {

                    @Override
                    public void componentResized(
                            ComponentEvent event) {

                        updateGridColumns();
                    }
                });
    }

    public void displayTables(
            List<TableDashboardItem> items,
            DashboardMode dashboardMode,
            Consumer<TableDashboardItem> clickHandler) {

        this.items = items == null
                ? List.of()
                : List.copyOf(items);

        this.dashboardMode = dashboardMode;

        this.clickHandler = clickHandler;

        updateGridColumns();

        rebuildCards();
    }

    private void rebuildCards() {
        removeAll();

        for (TableDashboardItem item : items) {

            TableCard card = new TableCard(
                    item,
                    dashboardMode,
                    clickHandler);

            card.setPreferredSize(
                    new Dimension(
                            CARD_MINIMUM_WIDTH,
                            CARD_HEIGHT));

            add(card);
        }

        updatePreferredHeight(
                currentColumnCount);

        revalidate();
        repaint();
    }

    private void updateGridColumns() {
        int newColumnCount = calculateColumnCount();

        if (newColumnCount == currentColumnCount) {

            return;
        }

        currentColumnCount = newColumnCount;

        setLayout(
                new GridLayout(
                        0,
                        currentColumnCount,
                        HORIZONTAL_GAP,
                        VERTICAL_GAP));

        updatePreferredHeight(
                currentColumnCount);

        revalidate();
        repaint();
    }

    private int calculateColumnCount() {
        int availableWidth = getParent() == null
                ? getWidth()
                : getParent().getWidth();

        if (availableWidth <= 0) {
            availableWidth = 1100;
        }

        availableWidth -= OUTER_PADDING * 2;

        int columns = (availableWidth
                + HORIZONTAL_GAP)
                / (CARD_MINIMUM_WIDTH
                        + HORIZONTAL_GAP);

        return Math.max(
                1,
                columns);
    }

    private void updatePreferredHeight(
            int columns) {

        int safeColumns = Math.max(
                1,
                columns);

        int rowCount = Math.max(
                1,
                (int) Math.ceil(
                        items.size()
                                / (double) safeColumns));

        int contentHeight = rowCount * CARD_HEIGHT
                + Math.max(
                        0,
                        rowCount - 1)
                        * VERTICAL_GAP
                + OUTER_PADDING * 2;

        setPreferredSize(
                new Dimension(
                        0,
                        contentHeight));
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(
            Rectangle visibleRectangle,
            int orientation,
            int direction) {

        return orientation == SwingConstants.VERTICAL
                ? 24
                : 0;
    }

    @Override
    public int getScrollableBlockIncrement(
            Rectangle visibleRectangle,
            int orientation,
            int direction) {

        return orientation == SwingConstants.VERTICAL
                ? Math.max(
                        CARD_HEIGHT,
                        visibleRectangle.height - CARD_HEIGHT)
                : 0;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        /*
         * Quan trọng:
         * Panel luôn bám theo chiều rộng viewport,
         * không được kéo dài sang phải.
         */
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        /*
         * Cho phép panel cao hơn viewport để xuất hiện scroll dọc.
         */
        return false;
    }
}