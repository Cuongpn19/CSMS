package com.csms.view.admin.user;

import com.csms.entity.RoleName;
import com.csms.entity.User;
import com.csms.entity.UserStatus;
import com.csms.service.UserService;
import com.csms.utils.SessionManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UserManagementPanel
        extends JPanel {

    private static final String ALL_ROLES = "Tất cả vai trò";

    private static final String ALL_STATUSES = "Tất cả trạng thái";

    private final UserService userService;

    private final JTextField keywordField;

    private final JComboBox<Object> roleFilterComboBox;
    private final JComboBox<Object> statusFilterComboBox;

    private final DefaultTableModel tableModel;
    private final JTable userTable;

    private final JButton searchButton;
    private final JButton refreshButton;

    private final JLabel resultCountLabel;

    private final DateTimeFormatter dateFormatter;

    private List<User> displayedUsers;

    private boolean loading;

    public UserManagementPanel() {
        userService = new UserService();

        keywordField = new JTextField();

        roleFilterComboBox = new JComboBox<>();

        statusFilterComboBox = new JComboBox<>();

        tableModel = createTableModel();

        userTable = new JTable(tableModel);

        searchButton = new JButton("Tìm kiếm");

        refreshButton = new JButton("Làm mới");

        resultCountLabel = new JLabel("0 tài khoản");

        dateFormatter = DateTimeFormatter.ofPattern(
                "dd/MM/yyyy HH:mm");

        displayedUsers = new ArrayList<>();

        loading = false;

        initializeFilters();
        initializeComponents();
        registerEvents();

        loadUsers();
    }

    private void initializeFilters() {
        roleFilterComboBox.addItem(
                ALL_ROLES);

        for (RoleName roleName : RoleName.values()) {

            roleFilterComboBox.addItem(
                    roleName);
        }

        statusFilterComboBox.addItem(
                ALL_STATUSES);

        for (UserStatus status : UserStatus.values()) {

            statusFilterComboBox.addItem(
                    status);
        }
    }

    private void initializeComponents() {
        setLayout(
                new BorderLayout(
                        0,
                        16));

        setBackground(
                new Color(245, 247, 251));

        setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20));

        add(
                createHeaderPanel(),
                BorderLayout.NORTH);

        add(
                createTablePanel(),
                BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel wrapperPanel = new JPanel(
                new BorderLayout(
                        0,
                        15));

        wrapperPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(
                new BorderLayout());

        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel(
                "QUẢN LÝ NGƯỜI DÙNG");

        titleLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        24));

        JLabel descriptionLabel = new JLabel(
                "Quản lý tài khoản Manager, Waiter, Barista và Cashier");

        descriptionLabel.setForeground(
                new Color(105, 113, 128));

        JPanel titleTextPanel = new JPanel(
                new BorderLayout(
                        0,
                        4));

        titleTextPanel.setOpaque(false);

        titleTextPanel.add(
                titleLabel,
                BorderLayout.NORTH);

        titleTextPanel.add(
                descriptionLabel,
                BorderLayout.SOUTH);

        JButton addButton = new JButton("Thêm tài khoản");

        addButton.addActionListener(
                event -> openCreateDialog());

        titlePanel.add(
                titleTextPanel,
                BorderLayout.WEST);

        titlePanel.add(
                addButton,
                BorderLayout.EAST);

        JPanel filterPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        0));

        filterPanel.setOpaque(false);

        keywordField.setPreferredSize(
                new Dimension(260, 36));

        roleFilterComboBox.setPreferredSize(
                new Dimension(155, 36));

        statusFilterComboBox.setPreferredSize(
                new Dimension(170, 36));

        filterPanel.add(
                new JLabel("Từ khóa:"));

        filterPanel.add(keywordField);

        filterPanel.add(
                new JLabel("Vai trò:"));

        filterPanel.add(
                roleFilterComboBox);

        filterPanel.add(
                new JLabel("Trạng thái:"));

        filterPanel.add(
                statusFilterComboBox);

        filterPanel.add(searchButton);
        filterPanel.add(refreshButton);
        filterPanel.add(resultCountLabel);

        wrapperPanel.add(
                titlePanel,
                BorderLayout.NORTH);

        wrapperPanel.add(
                filterPanel,
                BorderLayout.SOUTH);

        return wrapperPanel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(
                new BorderLayout(
                        0,
                        12));

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(226, 230, 237)),
                        BorderFactory.createEmptyBorder(
                                15,
                                15,
                                15,
                                15)));

        configureTable();

        JScrollPane scrollPane = new JScrollPane(userTable);

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder());

        JPanel actionPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT));

        actionPanel.setOpaque(false);

        JButton editButton = new JButton("Sửa");

        JButton lockButton = new JButton("Khóa/Mở khóa");

        JButton statusButton = new JButton("Đổi trạng thái");

        JButton resetPasswordButton = new JButton("Đặt lại mật khẩu");

        editButton.addActionListener(
                event -> openEditDialog());

        lockButton.addActionListener(
                event -> toggleSelectedUserLock());

        statusButton.addActionListener(
                event -> toggleSelectedUserStatus());

        resetPasswordButton.addActionListener(
                event -> openResetPasswordDialog());

        actionPanel.add(editButton);
        actionPanel.add(lockButton);
        actionPanel.add(statusButton);
        actionPanel.add(resetPasswordButton);

        panel.add(
                scrollPane,
                BorderLayout.CENTER);

        panel.add(
                actionPanel,
                BorderLayout.SOUTH);

        return panel;
    }

    private void configureTable() {
        userTable.setRowHeight(32);

        userTable.setFillsViewportHeight(true);

        userTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        userTable.getTableHeader()
                .setReorderingAllowed(false);

        userTable.setAutoCreateRowSorter(true);

        userTable.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(45);

        userTable.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(120);

        userTable.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(180);

        userTable.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(170);

        userTable.getColumnModel()
                .getColumn(4)
                .setPreferredWidth(110);

        userTable.getColumnModel()
                .getColumn(5)
                .setPreferredWidth(110);

        userTable.getColumnModel()
                .getColumn(6)
                .setPreferredWidth(125);

        userTable.getColumnModel()
                .getColumn(7)
                .setPreferredWidth(145);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();

        centerRenderer.setHorizontalAlignment(
                JLabel.CENTER);

        userTable.getColumnModel()
                .getColumn(0)
                .setCellRenderer(centerRenderer);

        userTable.getColumnModel()
                .getColumn(5)
                .setCellRenderer(centerRenderer);

        userTable.getColumnModel()
                .getColumn(6)
                .setCellRenderer(centerRenderer);
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(
                new Object[] {
                        "ID",
                        "Tên đăng nhập",
                        "Họ và tên",
                        "Email",
                        "Số điện thoại",
                        "Vai trò",
                        "Trạng thái",
                        "Chi nhánh",
                        "Ngày tạo"
                },
                0) {
            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {
                return false;
            }
        };
    }

    private void registerEvents() {
        searchButton.addActionListener(
                event -> loadUsers());

        refreshButton.addActionListener(
                event -> resetFilters());

        keywordField.addActionListener(
                event -> loadUsers());

        roleFilterComboBox.addActionListener(
                event -> {
                    if (!loading) {
                        loadUsers();
                    }
                });

        statusFilterComboBox.addActionListener(
                event -> {
                    if (!loading) {
                        loadUsers();
                    }
                });

        userTable.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent event) {
                        if (event.getClickCount() == 2
                                && userTable
                                        .getSelectedRow() >= 0) {

                            openEditDialog();
                        }
                    }
                });
    }

    private void loadUsers() {
        if (loading) {
            return;
        }

        setLoading(true);

        String keyword = keywordField
                .getText()
                .trim();

        RoleName selectedRole = getSelectedRole();

        UserStatus selectedStatus = getSelectedStatus();

        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<User> doInBackground() {
                return userService.search(
                        keyword,
                        selectedRole,
                        selectedStatus);
            }

            @Override
            protected void done() {
                try {
                    displayedUsers = get();
                    displayUsers();

                } catch (Exception exception) {
                    Throwable cause = exception.getCause() == null
                            ? exception
                            : exception.getCause();

                    showError(
                            cause.getMessage());

                } finally {
                    setLoading(false);
                }
            }
        };

        worker.execute();
    }

    private void displayUsers() {
        tableModel.setRowCount(0);

        for (User user : displayedUsers) {
            tableModel.addRow(
                    new Object[] {
                            user.getId(),
                            user.getUsername(),
                            user.getFullName(),
                            user.getEmail() == null
                                    ? "-"
                                    : user.getEmail(),
                            user.getPhone() == null
                                    ? "-"
                                    : user.getPhone(),
                            user.getRoleName()
                                    .getDisplayName(),
                            user.getStatus()
                                    .getDisplayName(),
                            user.getBranchName() == null
                                    ? "Chưa phân chi nhánh"
                                    : user.getBranchName(),
                            user.getCreatedAt() == null
                                    ? "-"
                                    : user.getCreatedAt()
                                            .format(dateFormatter)
                    });
        }

        resultCountLabel.setText(
                displayedUsers.size()
                        + " tài khoản");
    }

    private void openCreateDialog() {
        Window owner = SwingUtilities.getWindowAncestor(
                this);

        UserDialog dialog = new UserDialog(
                owner,
                null,
                getCurrentUserId());

        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadUsers();
        }
    }

    private void openEditDialog() {
        User selectedUser = getSelectedUser();

        if (selectedUser == null) {
            showWarning(
                    "Vui lòng chọn tài khoản cần sửa.");
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(
                this);

        UserDialog dialog = new UserDialog(
                owner,
                selectedUser,
                getCurrentUserId());

        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadUsers();
        }
    }

    private void toggleSelectedUserLock() {
        User selectedUser = getSelectedUser();

        if (selectedUser == null) {
            showWarning(
                    "Vui lòng chọn tài khoản cần khóa hoặc mở khóa.");
            return;
        }

        boolean unlocking = selectedUser.getStatus() == UserStatus.INACTIVE;

        String action = unlocking
                ? "mở khóa"
                : "khóa";

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn "
                        + action
                        + " tài khoản "
                        + selectedUser
                                .getUsername()
                        + "?",
                "Xác nhận " + action,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            userService.toggleStatus(
                    selectedUser.getId(),
                    getCurrentUserId());

            JOptionPane.showMessageDialog(
                    this,
                    unlocking
                            ? "Mở khóa tài khoản thành công."
                            : "Khóa tài khoản thành công.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            loadUsers();

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            showError(
                    exception.getMessage());
        }
    }

    private void toggleSelectedUserStatus() {
        User selectedUser = getSelectedUser();

        if (selectedUser == null) {
            showWarning(
                    "Vui lòng chọn tài khoản cần thay đổi trạng thái.");
            return;
        }

        boolean activating = selectedUser.getStatus() == UserStatus.INACTIVE;

        String actionText = activating
                ? "kích hoạt"
                : "vô hiệu hóa";

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn "
                        + actionText
                        + " tài khoản "
                        + selectedUser.getUsername()
                        + "?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            userService.toggleStatus(
                    selectedUser.getId(),
                    getCurrentUserId());

            JOptionPane.showMessageDialog(
                    this,
                    activating
                            ? "Kích hoạt tài khoản thành công."
                            : "Vô hiệu hóa tài khoản thành công.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            loadUsers();

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            showError(
                    exception.getMessage());
        }
    }

    private void openResetPasswordDialog() {
        User selectedUser = getSelectedUser();

        if (selectedUser == null) {
            showWarning(
                    "Vui lòng chọn tài khoản cần đặt lại mật khẩu.");
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(
                this);

        ResetPasswordDialog dialog = new ResetPasswordDialog(
                owner,
                selectedUser);

        dialog.setVisible(true);
    }

    private User getSelectedUser() {
        int selectedViewRow = userTable.getSelectedRow();

        if (selectedViewRow < 0) {
            return null;
        }

        int selectedModelRow = userTable.convertRowIndexToModel(
                selectedViewRow);

        if (selectedModelRow < 0
                || selectedModelRow >= displayedUsers.size()) {
            return null;
        }

        return displayedUsers.get(
                selectedModelRow);
    }

    private RoleName getSelectedRole() {
        Object selectedItem = roleFilterComboBox
                .getSelectedItem();

        return selectedItem instanceof RoleName role
                ? role
                : null;
    }

    private UserStatus getSelectedStatus() {
        Object selectedItem = statusFilterComboBox
                .getSelectedItem();

        return selectedItem instanceof UserStatus status
                ? status
                : null;
    }

    private void resetFilters() {
        keywordField.setText("");

        roleFilterComboBox
                .setSelectedIndex(0);

        statusFilterComboBox
                .setSelectedIndex(0);

        loadUsers();
    }

    private int getCurrentUserId() {
        /*
         * Nếu SessionManager của bạn trả về
         * User bằng getCurrentUser().
         */
        if (SessionManager.getCurrentUser() == null) {
            return 0;
        }

        return SessionManager
                .getCurrentUser()
                .getId();
    }

    private void setLoading(boolean loading) {
        this.loading = loading;

        searchButton.setEnabled(!loading);
        refreshButton.setEnabled(!loading);

        setCursor(
                loading
                        ? Cursor.getPredefinedCursor(
                                Cursor.WAIT_CURSOR)
                        : Cursor.getDefaultCursor());
    }

    private void showWarning(
            String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
    }

    private void showError(
            String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
    }
}