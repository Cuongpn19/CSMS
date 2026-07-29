package com.csms.view.admin.user;

import com.csms.entity.User;
import com.csms.service.UserService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;

public class ResetPasswordDialog
        extends JDialog {

    private final UserService userService;
    private final User user;

    private final JPasswordField newPasswordField;
    private final JPasswordField confirmPasswordField;

    private boolean passwordChanged;

    public ResetPasswordDialog(
            Window owner,
            User user) {
        super(
                owner,
                "Đặt lại mật khẩu",
                ModalityType.APPLICATION_MODAL);

        this.userService = new UserService();

        this.user = user;

        newPasswordField = new JPasswordField();

        confirmPasswordField = new JPasswordField();

        passwordChanged = false;

        initializeComponents();
    }

    private void initializeComponents() {
        setDefaultCloseOperation(
                DISPOSE_ON_CLOSE);

        setResizable(false);

        JPanel rootPanel = new JPanel(
                new BorderLayout(
                        0,
                        16));

        rootPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        22,
                        20,
                        22));

        JLabel informationLabel = new JLabel(
                "Tài khoản: "
                        + user.getUsername()
                        + " – "
                        + user.getFullName());

        JPanel formPanel = new JPanel(
                new GridLayout(
                        2,
                        2,
                        12,
                        12));

        formPanel.add(
                new JLabel("Mật khẩu mới:"));

        formPanel.add(
                newPasswordField);

        formPanel.add(
                new JLabel("Xác nhận mật khẩu:"));

        formPanel.add(
                confirmPasswordField);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT));

        JButton cancelButton = new JButton("Hủy");

        JButton saveButton = new JButton("Đặt lại mật khẩu");

        cancelButton.addActionListener(
                event -> dispose());

        saveButton.addActionListener(
                event -> resetPassword());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        rootPanel.add(
                informationLabel,
                BorderLayout.NORTH);

        rootPanel.add(
                formPanel,
                BorderLayout.CENTER);

        rootPanel.add(
                buttonPanel,
                BorderLayout.SOUTH);

        setContentPane(rootPanel);

        setPreferredSize(
                new Dimension(480, 230));

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void resetPassword() {
        String newPassword = new String(
                newPasswordField
                        .getPassword());

        String confirmPassword = new String(
                confirmPasswordField
                        .getPassword());

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận đặt lại mật khẩu cho tài khoản "
                        + user.getUsername()
                        + "?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            userService.resetPassword(
                    user.getId(),
                    newPassword,
                    confirmPassword);

            passwordChanged = true;

            JOptionPane.showMessageDialog(
                    this,
                    "Đặt lại mật khẩu thành công.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Không thể đặt lại mật khẩu",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isPasswordChanged() {
        return passwordChanged;
    }
}