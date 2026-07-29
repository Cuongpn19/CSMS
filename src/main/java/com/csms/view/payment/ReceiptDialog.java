package com.csms.view.payment;

import com.csms.dto.ReceiptData;
import com.csms.utils.ReceiptFormatter;
import com.csms.utils.ReceiptPrinter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

public class ReceiptDialog extends JDialog {

    private final ReceiptData receiptData;
    private final String receiptContent;

    public ReceiptDialog(
            JFrame owner,
            ReceiptData receiptData) {
        super(owner, "Hóa đơn thanh toán", true);

        this.receiptData = receiptData;
        this.receiptContent = ReceiptFormatter.format(receiptData);

        initializeComponents();
    }

    private void initializeComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel rootPanel = new JPanel(
                new BorderLayout(
                        0,
                        15));

        rootPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        18,
                        18,
                        18,
                        18));

        JTextArea receiptArea = new JTextArea(receiptContent);

        receiptArea.setEditable(false);
        receiptArea.setFont(
                new Font(
                        Font.MONOSPACED,
                        Font.PLAIN,
                        13));

        receiptArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(receiptArea);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT));

        JButton closeButton = new JButton("Đóng");

        JButton printButton = new JButton("In hóa đơn");

        closeButton.addActionListener(
                event -> dispose());

        printButton.addActionListener(
                event -> printReceipt());

        buttonPanel.add(closeButton);
        buttonPanel.add(printButton);

        rootPanel.add(
                scrollPane,
                BorderLayout.CENTER);

        rootPanel.add(
                buttonPanel,
                BorderLayout.SOUTH);

        setContentPane(rootPanel);
        setPreferredSize(
                new Dimension(620, 700));

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void printReceipt() {
        try {
            boolean printed = ReceiptPrinter.printReceipt(
                    receiptContent);

            if (printed) {
                JOptionPane.showMessageDialog(
                        this,
                        "Đã gửi hóa đơn đến máy in.",
                        "In hóa đơn",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Lỗi in hóa đơn",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}