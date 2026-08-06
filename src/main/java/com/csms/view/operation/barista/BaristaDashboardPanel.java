package com.csms.view.operation.barista;

import com.csms.dto.BaristaOrderGroup;
import com.csms.entity.OrderItemStatus;
import com.csms.service.BaristaOrderService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BaristaDashboardPanel extends JPanel {

    private final BaristaOrderService baristaOrderService;

    private final BaristaQueueColumn waitingColumn;
    private final BaristaQueueColumn preparingColumn;
    private final BaristaQueueColumn preparedColumn;

    private final JButton refreshButton;
    private final JLabel loadingLabel;

    private final Timer refreshTimer;

    private boolean loading;

    public BaristaDashboardPanel() {
        baristaOrderService = new BaristaOrderService();

        waitingColumn = new BaristaQueueColumn(
                "CHỜ PHA",
                new Color(37, 99, 235));

        preparingColumn = new BaristaQueueColumn(
                "ĐANG PHA",
                new Color(234, 88, 12));

        preparedColumn = new BaristaQueueColumn(
                "ĐÃ PHA XONG",
                new Color(219, 39, 119));

        refreshButton = new JButton("Làm mới");

        loadingLabel = new JLabel(" ");

        loading = false;

        refreshTimer = new Timer(
                5000,
                event -> {
                    if (!loading
                            && isShowing()) {

                        loadQueue(false);
                    }
                });

        initializeComponents();
        registerEvents();

        loadQueue(true);
        refreshTimer.start();
    }

    private void initializeComponents() {
        setLayout(
                new BorderLayout(0, 16));

        setBackground(
                new Color(245, 247, 251));

        setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        22,
                        20,
                        22));

        add(
                createHeaderPanel(),
                BorderLayout.NORTH);

        add(
                createQueuePanel(),
                BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(
                new BorderLayout());

        panel.setOpaque(false);

        JPanel titlePanel = new JPanel(
                new BorderLayout(0, 4));

        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel(
                "HÀNG ĐỢI PHA CHẾ");

        titleLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        25));

        JLabel subtitleLabel = new JLabel(
                "Theo dõi và xử lý các món được gửi từ Waiter");

        subtitleLabel.setForeground(
                new Color(105, 113, 128));

        titlePanel.add(
                titleLabel,
                BorderLayout.NORTH);

        titlePanel.add(
                subtitleLabel,
                BorderLayout.SOUTH);

        JPanel actionPanel = new JPanel(
                new java.awt.FlowLayout(
                        java.awt.FlowLayout.RIGHT,
                        10,
                        0));

        actionPanel.setOpaque(false);

        loadingLabel.setForeground(
                new Color(37, 99, 235));

        actionPanel.add(loadingLabel);
        actionPanel.add(refreshButton);

        panel.add(
                titlePanel,
                BorderLayout.WEST);

        panel.add(
                actionPanel,
                BorderLayout.EAST);

        return panel;
    }

    private JPanel createQueuePanel() {
        JPanel panel = new JPanel(
                new GridLayout(
                        1,
                        3,
                        14,
                        0));

        panel.setOpaque(false);

        panel.add(waitingColumn);
        panel.add(preparingColumn);
        panel.add(preparedColumn);

        return panel;
    }

    private void registerEvents() {
        refreshButton.addActionListener(
                event -> loadQueue(true));
    }

    public void loadQueue() {
        loadQueue(true);
    }

    private void loadQueue(
            boolean showLoadingText) {
        if (loading) {
            return;
        }

        setLoading(
                true,
                showLoadingText);

        SwingWorker<List<BaristaOrderGroup>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<BaristaOrderGroup> doInBackground() {

                return baristaOrderService
                        .getQueue();
            }

            @Override
            protected void done() {
                try {
                    displayQueue(get());

                } catch (InterruptedException exception) {
                    Thread.currentThread()
                            .interrupt();

                    if (showLoadingText) {
                        showError(
                                "Quá trình tải hàng đợi đã bị gián đoạn.");
                    }

                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause() == null
                            ? exception
                            : exception.getCause();

                    if (showLoadingText) {
                        showError(
                                cause.getMessage());
                    }

                } finally {
                    setLoading(false, false);
                }
            }
        };

        worker.execute();
    }

    private void displayQueue(
            List<BaristaOrderGroup> groups) {
        List<BaristaOrderGroup> waiting = new ArrayList<>();

        List<BaristaOrderGroup> preparing = new ArrayList<>();

        List<BaristaOrderGroup> prepared = new ArrayList<>();

        for (BaristaOrderGroup group : groups) {
            switch (group.queueStatus()) {
                case IN_PROGRESS ->
                    waiting.add(group);

                case PREPARING ->
                    preparing.add(group);

                case PREPARED ->
                    prepared.add(group);

                default -> {
                }
            }
        }

        waitingColumn.displayOrders(
                waiting,
                this::handleOrderAction);

        preparingColumn.displayOrders(
                preparing,
                this::handleOrderAction);

        preparedColumn.displayOrders(
                prepared,
                null);
    }

    private void handleOrderAction(
            BaristaOrderGroup group) {
        if (group == null || loading) {
            return;
        }

        String actionText = group.queueStatus() == OrderItemStatus.IN_PROGRESS
                ? "nhận pha chế"
                : "hoàn thành pha chế";

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận "
                        + actionText
                        + " cho "
                        + resolveOrderName(group)
                        + " - Bàn "
                        + group.tableNumber()
                        + "?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        updateOrderStatus(group);
    }

    private void updateOrderStatus(
            BaristaOrderGroup group) {
        setLoading(true, true);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() {
                if (group.queueStatus() == OrderItemStatus.IN_PROGRESS) {

                    baristaOrderService
                            .startPreparing(
                                    group.orderId());

                } else if (group.queueStatus() == OrderItemStatus.PREPARING) {
                    baristaOrderService
                            .markPrepared(
                                    group.orderId());
                }

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    JOptionPane.showMessageDialog(
                            BaristaDashboardPanel.this,
                            group.queueStatus() == OrderItemStatus.IN_PROGRESS
                                    ? "Đã nhận pha chế."
                                    : "Đã hoàn thành pha chế.",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (InterruptedException exception) {
                    Thread.currentThread()
                            .interrupt();

                    showError(
                            "Thao tác đã bị gián đoạn.");

                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause() == null
                            ? exception
                            : exception.getCause();

                    showError(
                            cause.getMessage());

                } finally {
                    setLoading(false, false);
                    loadQueue(false);
                }
            }
        };

        worker.execute();
    }

    private String resolveOrderName(
            BaristaOrderGroup group) {
        if (group.orderCode() != null
                && !group.orderCode().isBlank()) {

            return group.orderCode();
        }

        return "đơn #" + group.orderId();
    }

    private void setLoading(
            boolean loading,
            boolean showText) {
        this.loading = loading;

        refreshButton.setEnabled(!loading);

        loadingLabel.setText(
                loading && showText
                        ? "Đang xử lý..."
                        : " ");

        setCursor(
                loading
                        ? Cursor.getPredefinedCursor(
                                Cursor.WAIT_CURSOR)
                        : Cursor.getDefaultCursor());
    }

    private void showError(
            String message) {
        JOptionPane.showMessageDialog(
                this,
                message == null
                        ? "Đã xảy ra lỗi không xác định."
                        : message,
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void removeNotify() {
        refreshTimer.stop();
        super.removeNotify();
    }

    @Override
    public void addNotify() {
        super.addNotify();

        if (!refreshTimer.isRunning()) {
            refreshTimer.start();
        }
    }
}