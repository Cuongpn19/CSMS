package com.csms.view.admin.user;

import com.csms.dto.UserFormData;
import com.csms.entity.RoleName;
import com.csms.entity.User;
import com.csms.entity.UserStatus;
import com.csms.service.UserService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

public class UserDialog extends JDialog {

    private final UserService userService;
    private final User editingUser;
    private final int currentLoggedInUserId;

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JPasswordField confirmPasswordField;

    private final JTextField fullNameField;
    private final JTextField emailField;
    private final JTextField phoneField;

    private final JComboBox<RoleName> roleComboBox;
    private final JComboBox<UserStatus> statusComboBox;

    private boolean saved;

    public UserDialog(
            Window owner,
            User editingUser,
            int currentLoggedInUserId) {
        super(
                owner,
                editingUser == null
                        ? "Thêm tài khoản"
                        : "Cập nhật tài khoản",
                ModalityType.APPLICATION_MODAL);

        this.userService = new UserService();
        this.editingUser = editingUser;
        this.currentLoggedInUserId = currentLoggedInUserId;

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        fullNameField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();

        roleComboBox = new JComboBox<>(
                RoleName.values());

        statusComboBox = new JComboBox<>(
                UserStatus.values());

        saved = false;

        initializeComponents();
        fillEditingData();
    }

    private void initializeComponents() {
        setDefaultCloseOperation(
                DISPOSE_ON_CLOSE);

        setResizable(false);

        JPanel rootPanel = new JPanel(
                new BorderLayout(
                        0,
                        18));

        rootPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        22,
                        20,
                        22));

        JPanel formPanel = new JPanel(
                new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.insets = new Insets(
                7,
                7,
                7,
                7);

        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.weightx = 1;

        int row = 0;

        addFormRow(
                formPanel,
                constraints,
                row++,
                "Tên đăng nhập:",
                usernameField);

        addFormRow(
                formPanel,
                constraints,
                row++,
                "Họ và tên:",
                fullNameField);

        addFormRow(
                formPanel,
                constraints,
                row++,
                "Email:",
                emailField);

        addFormRow(
                formPanel,
                constraints,
                row++,
                "Số điện thoại:",
                phoneField);

        addFormRow(
                formPanel,
                constraints,
                row++,
                "Vai trò:",
                roleComboBox);

        addFormRow(
                formPanel,
                constraints,
                row++,
                "Trạng thái:",
                statusComboBox);

        if (editingUser == null) {
            addFormRow(
                    formPanel,
                    constraints,
                    row++,
                    "Mật khẩu:",
                    passwordField);

            addFormRow(
                    formPanel,
                    constraints,
                    row,
                    "Xác nhận mật khẩu:",
                    confirmPasswordField);
        }

        JPanel buttonPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT));

        JButton cancelButton = new JButton("Hủy");

        JButton saveButton = new JButton(
                editingUser == null
                        ? "Thêm tài khoản"
                        : "Lưu thay đổi");

        cancelButton.addActionListener(
                event -> dispose());

        saveButton.addActionListener(
                event -> saveUser());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        rootPanel.add(
                formPanel,
                BorderLayout.CENTER);

        rootPanel.add(
                buttonPanel,
                BorderLayout.SOUTH);

        setContentPane(rootPanel);

        setPreferredSize(
                new Dimension(
                        540,
                        editingUser == null
                                ? 520
                                : 430));

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String label,
            java.awt.Component component) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0.3;

        panel.add(
                new JLabel(label),
                constraints);

        constraints.gridx = 1;
        constraints.weightx = 0.7;

        component.setPreferredSize(
                new Dimension(280, 35));

        panel.add(
                component,
                constraints);
    }

    private void fillEditingData() {
        statusComboBox.setSelectedItem(
                UserStatus.ACTIVE);

        if (editingUser == null) {
            return;
        }

        usernameField.setText(
                editingUser.getUsername());

        fullNameField.setText(
                editingUser.getFullName());

        emailField.setText(
                editingUser.getEmail() == null
                        ? ""
                        : editingUser.getEmail());

        phoneField.setText(
                editingUser.getPhone() == null
                        ? ""
                        : editingUser.getPhone());

        roleComboBox.setSelectedItem(
                editingUser.getRoleName());

        statusComboBox.setSelectedItem(
                editingUser.getStatus());
    }

    private void saveUser() {
        UserFormData formData = new UserFormData(
                usernameField
                        .getText()
                        .trim(),

                new String(
                        passwordField
                                .getPassword()),

                new String(
                        confirmPasswordField
                                .getPassword()),

                fullNameField
                        .getText()
                        .trim(),

                emailField
                        .getText()
                        .trim(),

                phoneField
                        .getText()
                        .trim(),

                (RoleName) roleComboBox
                        .getSelectedItem(),

                (UserStatus) statusComboBox
                        .getSelectedItem());

        try {
            if (editingUser == null) {
                userService.createUser(
                        formData);

                JOptionPane.showMessageDialog(
                        this,
                        "Thêm tài khoản thành công.",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

            } else {
                userService.updateUser(
                        editingUser.getId(),
                        formData,
                        currentLoggedInUserId);

                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật tài khoản thành công.",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            saved = true;
            dispose();

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Không thể lưu tài khoản",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}