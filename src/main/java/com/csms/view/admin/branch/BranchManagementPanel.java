package com.csms.view.admin.branch;

import com.csms.entity.Branch;
import com.csms.entity.BranchStatus;
import com.csms.service.BranchService;

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

public class BranchManagementPanel
        extends JPanel {

    private static final String ALL_STATUSES = "Tất cả trạng thái";

    private final BranchService branchService;

    private final JTextField keywordField;

    private final JComboBox<Object> statusFilterComboBox;

    private final DefaultTableModel tableModel;
    private final JTable branchTable;

    private final JLabel resultCountLabel;

    private final JButton searchButton;
    private final JButton refreshButton;

    private final DateTimeFormatter timeFormatter;
    private final DateTimeFormatter dateFormatter;

    private List<Branch> displayedBranches;

    private boolean loading;

    public BranchManagementPanel() {
        branchService = new BranchService();

        keywordField = new JTextField();

        statusFilterComboBox = new JComboBox<>();

        tableModel = createTableModel();

        branchTable = new JTable(tableModel);

        resultCountLabel = new JLabel("0 chi nhánh");

        searchButton = new JButton("Tìm kiếm");

        refreshButton = new JButton("Làm mới");

        timeFormatter = DateTimeFormatter.ofPattern(
                "HH:mm");

        dateFormatter = DateTimeFormatter.ofPattern(
                "dd/MM/yyyy HH:mm");

        displayedBranches = new ArrayList<>();

        loading = false;

        initializeFilters();
        initializeComponents();
        registerEvents();

        loadBranches();
    }

    private void initializeFilters() {
        statusFilterComboBox.addItem(
                ALL_STATUSES);

        for (BranchStatus status : BranchStatus.values()) {

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
                new Color(
                        245,
                        247,
                        251));

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

        JPanel titleTextPanel = new JPanel(
                new BorderLayout(
                        0,
                        4));

        titleTextPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(
                "QUẢN LÝ CHI NHÁNH");

        titleLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        25));

        JLabel descriptionLabel = new JLabel(
                "Quản lý thông tin, giờ hoạt động và nhân viên từng chi nhánh");

        descriptionLabel.setForeground(
                new Color(
                        105,
                        113,
                        128));

        titleTextPanel.add(
                titleLabel,
                BorderLayout.NORTH);

        titleTextPanel.add(
                descriptionLabel,
                BorderLayout.SOUTH);

        JButton addButton = new JButton(
                "Thêm chi nhánh");

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
                new Dimension(
                        280,
                        36));

        statusFilterComboBox
                .setPreferredSize(
                        new Dimension(
                                180,
                                36));

        filterPanel.add(
                new JLabel("Từ khóa:"));

        filterPanel.add(keywordField);

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
                                new Color(
                                        226,
                                        230,
                                        237)),
                        BorderFactory.createEmptyBorder(
                                15,
                                15,
                                15,
                                15)));

        configureTable();

        JScrollPane scrollPane = new JScrollPane(branchTable);

        JPanel actionPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT));

        actionPanel.setOpaque(false);

        JButton editButton = new JButton("Sửa");

        JButton employeeButton = new JButton("Xem nhân viên");

        JButton statusButton = new JButton(
                "Kích hoạt / Ngừng hoạt động");

        JButton deleteButton = new JButton("Xóa");

        editButton.addActionListener(
                event -> openEditDialog());

        employeeButton.addActionListener(
                event -> openEmployeesDialog());

        statusButton.addActionListener(
                event -> toggleSelectedStatus());

        deleteButton.addActionListener(
                event -> deleteSelectedBranch());

        actionPanel.add(editButton);
        actionPanel.add(employeeButton);
        actionPanel.add(statusButton);
        actionPanel.add(deleteButton);

        panel.add(
                scrollPane,
                BorderLayout.CENTER);

        panel.add(
                actionPanel,
                BorderLayout.SOUTH);

        return panel;
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(
                new Object[] {
                        "ID",
                        "Tên chi nhánh",
                        "Địa chỉ",
                        "Số điện thoại",
                        "Giờ hoạt động",
                        "Tổng NV",
                        "NV hoạt động",
                        "Trạng thái",
                        "Cập nhật"
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

    private void configureTable() {
        branchTable.setRowHeight(34);

        branchTable.setFillsViewportHeight(
                true);

        branchTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        branchTable.setAutoCreateRowSorter(
                true);

        branchTable.getTableHeader()
                .setReorderingAllowed(false);

        branchTable.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(45);

        branchTable.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(180);

        branchTable.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(300);

        branchTable.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(120);

        branchTable.getColumnModel()
                .getColumn(4)
                .setPreferredWidth(130);
    }

    private void registerEvents() {
        searchButton.addActionListener(
                event -> loadBranches());

        refreshButton.addActionListener(
                event -> resetFilters());

        keywordField.addActionListener(
                event -> loadBranches());

        statusFilterComboBox
                .addActionListener(
                        event -> {
                            if (!loading) {
                                loadBranches();
                            }
                        });

        branchTable.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent event) {
                        if (event.getClickCount() == 2
                                && branchTable
                                        .getSelectedRow() >= 0) {

                            openEditDialog();
                        }
                    }
                });
    }

    private void loadBranches() {
        if (loading) {
            return;
        }

        setLoading(true);

        String keyword = keywordField
                .getText()
                .trim();

        BranchStatus status = getSelectedStatus();

        SwingWorker<List<Branch>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<Branch> doInBackground() {

                return branchService.search(
                        keyword,
                        status);
            }

            @Override
            protected void done() {
                try {
                    displayedBranches = get();

                    displayBranches();

                } catch (Exception exception) {
                    Throwable cause = exception.getCause() == null
                            ? exception
                            : exception
                                    .getCause();

                    showError(
                            cause.getMessage());

                } finally {
                    setLoading(false);
                }
            }
        };

        worker.execute();
    }

    private void displayBranches() {
        tableModel.setRowCount(0);

        for (Branch branch : displayedBranches) {

            String businessHours = formatTime(
                    branch.getOpeningTime())
                    + " - "
                    + formatTime(
                            branch.getClosingTime());

            tableModel.addRow(
                    new Object[] {
                            branch.getId(),
                            branch.getName(),
                            branch.getAddress(),

                            branch.getPhone() == null
                                    ? "-"
                                    : branch.getPhone(),

                            businessHours,

                            branch.getEmployeeCount(),

                            branch
                                    .getActiveEmployeeCount(),

                            branch.getStatus()
                                    .getDisplayName(),

                            branch.getUpdatedAt() == null
                                    ? "-"
                                    : branch
                                            .getUpdatedAt()
                                            .format(
                                                    dateFormatter)
                    });
        }

        resultCountLabel.setText(
                displayedBranches.size()
                        + " chi nhánh");
    }

    private void openCreateDialog() {
        Window owner = SwingUtilities
                .getWindowAncestor(this);

        BranchDialog dialog = new BranchDialog(
                owner,
                null);

        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadBranches();
        }
    }

    private void openEditDialog() {
        Branch branch = getSelectedBranch();

        if (branch == null) {
            showWarning(
                    "Vui lòng chọn chi nhánh cần sửa.");
            return;
        }

        Window owner = SwingUtilities
                .getWindowAncestor(this);

        BranchDialog dialog = new BranchDialog(
                owner,
                branch);

        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadBranches();
        }
    }

    private void openEmployeesDialog() {
        Branch branch = getSelectedBranch();

        if (branch == null) {
            showWarning(
                    "Vui lòng chọn chi nhánh cần xem nhân viên.");
            return;
        }

        Window owner = SwingUtilities
                .getWindowAncestor(this);

        BranchEmployeesDialog dialog = new BranchEmployeesDialog(
                owner,
                branch);

        dialog.setVisible(true);
    }

    private void toggleSelectedStatus() {
        Branch branch = getSelectedBranch();

        if (branch == null) {
            showWarning(
                    "Vui lòng chọn chi nhánh cần thay đổi trạng thái.");
            return;
        }

        boolean activating = branch.getStatus() == BranchStatus.INACTIVE;

        String action = activating
                ? "kích hoạt"
                : "ngừng hoạt động";

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn "
                        + action
                        + " chi nhánh \""
                        + branch.getName()
                        + "\"?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            branchService.toggleStatus(
                    branch.getId());

            JOptionPane.showMessageDialog(
                    this,
                    activating
                            ? "Kích hoạt chi nhánh thành công."
                            : "Đã ngừng hoạt động chi nhánh.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            loadBranches();

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            showError(
                    exception.getMessage());
        }
    }

    private void deleteSelectedBranch() {
        Branch branch = getSelectedBranch();

        if (branch == null) {
            showWarning(
                    "Vui lòng chọn chi nhánh cần xóa.");
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa chi nhánh \""
                        + branch.getName()
                        + "\"?\n"
                        + "Thao tác này không thể hoàn tác.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            branchService.delete(
                    branch.getId());

            JOptionPane.showMessageDialog(
                    this,
                    "Xóa chi nhánh thành công.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            loadBranches();

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            showError(
                    exception.getMessage());
        }
    }

    private Branch getSelectedBranch() {
        int selectedViewRow = branchTable.getSelectedRow();

        if (selectedViewRow < 0) {
            return null;
        }

        int selectedModelRow = branchTable
                .convertRowIndexToModel(
                        selectedViewRow);

        if (selectedModelRow < 0
                || selectedModelRow >= displayedBranches.size()) {

            return null;
        }

        return displayedBranches.get(
                selectedModelRow);
    }

    private BranchStatus getSelectedStatus() {
        Object selectedItem = statusFilterComboBox
                .getSelectedItem();

        return selectedItem instanceof BranchStatus status
                ? status
                : null;
    }

    private void resetFilters() {
        loading = true;

        keywordField.setText("");

        statusFilterComboBox
                .setSelectedIndex(0);

        loading = false;

        loadBranches();
    }

    private void setLoading(
            boolean loading) {
        this.loading = loading;

        searchButton.setEnabled(!loading);
        refreshButton.setEnabled(!loading);

        setCursor(
                loading
                        ? Cursor
                                .getPredefinedCursor(
                                        Cursor.WAIT_CURSOR)
                        : Cursor
                                .getDefaultCursor());
    }

    private String formatTime(
            java.time.LocalTime time) {
        return time == null
                ? "--:--"
                : time.format(
                        timeFormatter);
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