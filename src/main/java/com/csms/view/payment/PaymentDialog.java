package com.csms.view.payment;

import com.csms.dao.PaymentDAO;
import com.csms.entity.Order;
import com.csms.entity.Payment;
import com.csms.entity.PaymentMethod;
import com.csms.dto.ReceiptData;
import com.csms.service.ReceiptService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class PaymentDialog extends JDialog {

    private final Order order;
    private final PaymentDAO paymentDAO;

    private final JComboBox<PaymentMethod> paymentMethodComboBox;

    private final JTextField amountReceivedField;

    private final JLabel changeAmountLabel;

    private final NumberFormat currencyFormat;

    private final ReceiptService receiptService;

    private boolean paymentSuccessful;

    public PaymentDialog(
            JFrame owner,
            Order order) {
        super(owner, "Thanh toán đơn hàng", true);

        this.order = order;
        this.paymentDAO = new PaymentDAO();
        this.receiptService = new ReceiptService();

        this.currencyFormat = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("vi-VN"));

        this.paymentMethodComboBox = new JComboBox<>(
                PaymentMethod.values());

        this.amountReceivedField = new JTextField();

        this.changeAmountLabel = new JLabel(
                currencyFormat.format(
                        BigDecimal.ZERO));

        this.paymentSuccessful = false;

        initializeComponents();
        registerEvents();
        updatePaymentMethodState();
        calculateChangeAmount();
    }

    private void initializeComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel rootPanel = new JPanel(
                new BorderLayout(0, 18));

        rootPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        22,
                        24,
                        22,
                        24));

        JLabel titleLabel = new JLabel("THANH TOÁN");

        titleLabel.setFont(
                titleLabel.getFont().deriveFont(
                        Font.BOLD,
                        22F));

        JPanel informationPanel = new JPanel(
                new GridLayout(
                        4,
                        2,
                        12,
                        12));

        informationPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Thông tin đơn hàng"));

        informationPanel.add(
                new JLabel("Mã đơn:"));

        informationPanel.add(
                new JLabel(order.getOrderCode()));

        informationPanel.add(
                new JLabel("Bàn:"));

        informationPanel.add(
                new JLabel(
                        order.getTableName() == null
                                ? "Mang đi"
                                : order.getTableName()));

        informationPanel.add(
                new JLabel("Tổng thanh toán:"));

        JLabel totalAmountLabel = new JLabel(
                currencyFormat.format(
                        order.getTotalAmount()));

        totalAmountLabel.setFont(
                totalAmountLabel.getFont()
                        .deriveFont(
                                Font.BOLD,
                                18F));

        informationPanel.add(totalAmountLabel);

        informationPanel.add(
                new JLabel("Trạng thái:"));

        informationPanel.add(
                new JLabel("Chờ thanh toán"));

        JPanel paymentPanel = new JPanel(
                new GridLayout(
                        3,
                        2,
                        12,
                        12));

        paymentPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Thông tin thanh toán"));

        paymentMethodComboBox.setPreferredSize(
                new Dimension(190, 36));

        amountReceivedField.setHorizontalAlignment(
                JTextField.RIGHT);

        changeAmountLabel.setHorizontalAlignment(
                JLabel.RIGHT);

        changeAmountLabel.setFont(
                changeAmountLabel.getFont()
                        .deriveFont(
                                Font.BOLD,
                                17F));

        paymentPanel.add(
                new JLabel("Phương thức:"));

        paymentPanel.add(
                paymentMethodComboBox);

        paymentPanel.add(
                new JLabel("Tiền khách đưa:"));

        paymentPanel.add(
                amountReceivedField);

        paymentPanel.add(
                new JLabel("Tiền thối:"));

        paymentPanel.add(
                changeAmountLabel);

        JPanel centerPanel = new JPanel(
                new BorderLayout(0, 15));

        centerPanel.add(
                informationPanel,
                BorderLayout.NORTH);

        centerPanel.add(
                paymentPanel,
                BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT));

        JButton cancelButton = new JButton("Hủy");

        JButton paymentButton = new JButton("Xác nhận thanh toán");

        cancelButton.addActionListener(
                event -> dispose());

        paymentButton.addActionListener(
                event -> processPayment());

        buttonPanel.add(cancelButton);
        buttonPanel.add(paymentButton);

        rootPanel.add(
                titleLabel,
                BorderLayout.NORTH);

        rootPanel.add(
                centerPanel,
                BorderLayout.CENTER);

        rootPanel.add(
                buttonPanel,
                BorderLayout.SOUTH);

        setContentPane(rootPanel);
        pack();
        setMinimumSize(new Dimension(480, 390));
        setLocationRelativeTo(getOwner());
    }

    private void registerEvents() {
        paymentMethodComboBox.addActionListener(
                event -> {
                    updatePaymentMethodState();
                    calculateChangeAmount();
                });

        amountReceivedField.getDocument()
                .addDocumentListener(
                        new DocumentListener() {
                            @Override
                            public void insertUpdate(
                                    DocumentEvent event) {
                                calculateChangeAmount();
                            }

                            @Override
                            public void removeUpdate(
                                    DocumentEvent event) {
                                calculateChangeAmount();
                            }

                            @Override
                            public void changedUpdate(
                                    DocumentEvent event) {
                                calculateChangeAmount();
                            }
                        });
    }

    private void updatePaymentMethodState() {
        PaymentMethod method = (PaymentMethod) paymentMethodComboBox
                .getSelectedItem();

        boolean cash = method == PaymentMethod.CASH;

        amountReceivedField.setEnabled(cash);

        if (cash) {
            if (amountReceivedField
                    .getText()
                    .isBlank()) {

                amountReceivedField.setText(
                        order.getTotalAmount()
                                .toPlainString());
            }
        } else {
            amountReceivedField.setText(
                    order.getTotalAmount()
                            .toPlainString());
        }
    }

    private void calculateChangeAmount() {
        PaymentMethod method = (PaymentMethod) paymentMethodComboBox
                .getSelectedItem();

        if (method != PaymentMethod.CASH) {
            changeAmountLabel.setText(
                    currencyFormat.format(
                            BigDecimal.ZERO));
            return;
        }

        BigDecimal receivedAmount = parseAmount(false);

        if (receivedAmount == null) {
            changeAmountLabel.setText(
                    currencyFormat.format(
                            BigDecimal.ZERO));
            return;
        }

        BigDecimal change = receivedAmount.subtract(
                order.getTotalAmount());

        if (change.compareTo(
                BigDecimal.ZERO) < 0) {
            change = BigDecimal.ZERO;
        }

        changeAmountLabel.setText(
                currencyFormat.format(change));
    }

    private void processPayment() {
        PaymentMethod method = (PaymentMethod) paymentMethodComboBox
                .getSelectedItem();

        BigDecimal amountReceived = parseAmount(true);

        if (amountReceived == null) {
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận thanh toán đơn "
                        + order.getOrderCode()
                        + " với số tiền "
                        + currencyFormat.format(
                                order.getTotalAmount())
                        + "?",
                "Xác nhận thanh toán",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Payment payment = paymentDAO.createPayment(
                    order.getId(),
                    method,
                    amountReceived);

            paymentSuccessful = true;

            JOptionPane.showMessageDialog(
                    this,
                    "Thanh toán thành công.\n"
                            + "Tiền thối: "
                            + currencyFormat.format(
                                    payment.getChangeAmount()),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();

            showReceipt();

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Thanh toán thất bại",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showReceipt() {
        try {
            ReceiptData receiptData = receiptService.getReceiptData(
                    order.getId());

            ReceiptDialog receiptDialog = new ReceiptDialog(
                    (JFrame) getOwner(),
                    receiptData);

            receiptDialog.setVisible(true);

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    getOwner(),
                    "Thanh toán đã thành công nhưng không thể tải hóa đơn: "
                            + exception.getMessage(),
                    "Lỗi hóa đơn",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private BigDecimal parseAmount(
            boolean showMessage) {
        String text = amountReceivedField
                .getText()
                .trim()
                .replace(".", "")
                .replace(",", "");

        if (text.isBlank()) {
            if (showMessage) {
                showWarning(
                        "Vui lòng nhập tiền khách đưa.");
            }

            return null;
        }

        try {
            BigDecimal amount = new BigDecimal(text);

            if (amount.compareTo(
                    BigDecimal.ZERO) < 0) {
                if (showMessage) {
                    showWarning(
                            "Tiền khách đưa không được nhỏ hơn 0.");
                }

                return null;
            }

            return amount;

        } catch (NumberFormatException exception) {
            if (showMessage) {
                showWarning(
                        "Tiền khách đưa không hợp lệ.");
            }

            return null;
        }
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
    }

    public boolean isPaymentSuccessful() {
        return paymentSuccessful;
    }
}