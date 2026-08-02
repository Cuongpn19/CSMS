package com.csms.view.admin.vat;

import com.csms.entity.VatScopeType;
import com.csms.entity.VatSetting;
import com.csms.service.VatService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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

public class VatSettingPanel extends JPanel {

    private static final String ALL_SCOPES = "Tất cả phạm vi";

    private static final String ALL_STATUSES = "Tất cả trạng thái";

    private static final String ENABLED_STATUS = "Đang áp dụng";

    private static final String DISABLED_STATUS = "Ngừng áp dụng";

    private final VatService vatService;

    private final JComboBox<Object> scopeFilterComboBox;

    private final JComboBox<String> statusFilterComboBox;

    private final DefaultTableModel tableModel;
    private final JTable vatTable;

    private final JLabel resultCountLabel;

    private final JButton refreshButton;
    private final JButton addButton;

    private final DateTimeFormatter dateFormatter;

    private List<VatSetting> allSettings;
    private List<VatSetting> displayedSettings;

    private boolean loading;

    public VatSettingPanel() {
        vatService = new VatService();

        scopeFilterComboBox = new JComboBox<>();

        statusFilterComboBox = new JComboBox<>();

        tableModel = createTableModel();

        vatTable = new JTable(tableModel);

        resultCountLabel = new JLabel("0 cấu hình");

        refreshButton = new JButton("Làm mới");

        addButton = new JButton("Thêm cấu hình");

        dateFormatter = DateTimeFormatter.ofPattern(
                "dd/MM/yyyy HH:mm");

        allSettings = new ArrayList<>();

        displayedSettings = new ArrayList<>();

        loading = false;

        initializeFilters();
        initializeComponents();
        registerEvents();

        loadSettings();
    }

    private void initializeFilters() {
        scopeFilterComboBox.addItem(
                ALL_SCOPES);

        for (VatScopeType scopeType : VatScopeType.values()) {

            scopeFilterComboBox.addItem(
                    scopeType);
        }

        statusFilterComboBox.addItem(
                ALL_STATUSES);

        statusFilterComboBox.addItem(
                ENABLED_STATUS);

        statusFilterComboBox.addItem(
                DISABLED_STATUS);
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
                "CẤU HÌNH VAT");

        titleLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        25));

        JLabel descriptionLabel = new JLabel(
                "Thiết lập mức thuế toàn hệ thống, theo danh mục hoặc theo món");

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

        scopeFilterComboBox.setPreferredSize(
                new Dimension(
                        190,
                        36));

        statusFilterComboBox.setPreferredSize(
                new Dimension(
                        180,
                        36));

        filterPanel.add(
                new JLabel("Phạm vi:"));

        filterPanel.add(
                scopeFilterComboBox);

        filterPanel.add(
                new JLabel("Trạng thái:"));

        filterPanel.add(
                statusFilterComboBox);

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

        JScrollPane scrollPane = new JScrollPane(vatTable);

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder());

        JPanel actionPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT));

        actionPanel.setOpaque(false);

        JButton editButton = new JButton("Sửa");

        JButton toggleButton = new JButton("Bật / Tắt");

        JButton deleteButton = new JButton("Xóa");

        editButton.addActionListener(
                event -> openEditDialog());

        toggleButton.addActionListener(
                event -> toggleSelectedSetting());

        deleteButton.addActionListener(
                event -> deleteSelectedSetting());

        actionPanel.add(editButton);
        actionPanel.add(toggleButton);
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
                        "Phạm vi",
                        "Đối tượng áp dụng",
                        "VAT",
                        "Hiệu lực từ",
                        "Hiệu lực đến",
                        "Trạng thái",
                        "Người tạo",
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
        vatTable.setRowHeight(34);

        vatTable.setFillsViewportHeight(
                true);

        vatTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        vatTable.setAutoCreateRowSorter(
                true);

        vatTable.getTableHeader()
                .setReorderingAllowed(false);

        vatTable.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(45);

        vatTable.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(130);

        vatTable.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(200);

        vatTable.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(70);

        vatTable.getColumnModel()
                .getColumn(4)
                .setPreferredWidth(145);

        vatTable.getColumnModel()
                .getColumn(5)
                .setPreferredWidth(145);

        vatTable.getColumnModel()
                .getColumn(6)
                .setPreferredWidth(120);
    }

    private void registerEvents() {
        refreshButton.addActionListener(
                event -> resetFilters());

        scopeFilterComboBox.addActionListener(
                event -> {
                    if (!loading) {
                        applyFilters();
                    }
                });

        statusFilterComboBox.addActionListener(
                event -> {
                    if (!loading) {
                        applyFilters();
                    }
                });

        vatTable.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent event) {
                        if (event.getClickCount() == 2
                                && vatTable
                                        .getSelectedRow() >= 0) {

                            openEditDialog();
                        }
                    }
                });
    }

    private void loadSettings() {
        if (loading) {
            return;
        }

        setLoading(true);

        SwingWorker<List<VatSetting>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<VatSetting> doInBackground() {

                return vatService.findAll();
            }

            @Override
            protected void done() {
                try {
                    allSettings = get();
                    applyFilters();

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

    private void applyFilters() {
        VatScopeType selectedScope = getSelectedScope();

        String selectedStatus = (String) statusFilterComboBox
                .getSelectedItem();

        displayedSettings = new ArrayList<>();

        for (VatSetting setting : allSettings) {
            boolean matchesScope = selectedScope == null
                    || setting.getScopeType() == selectedScope;

            boolean matchesStatus = matchesStatus(
                    setting,
                    selectedStatus);

            if (matchesScope && matchesStatus) {
                displayedSettings.add(setting);
            }
        }

        displaySettings();
    }

    private boolean matchesStatus(
            VatSetting setting,
            String selectedStatus) {
        if (selectedStatus == null
                || ALL_STATUSES.equals(
                        selectedStatus)) {

            return true;
        }

        if (ENABLED_STATUS.equals(
                selectedStatus)) {
            return setting.isEnabled();
        }

        return !setting.isEnabled();
    }

    private void displaySettings() {
        tableModel.setRowCount(0);

        for (VatSetting setting : displayedSettings) {

            tableModel.addRow(
                    new Object[] {
                            setting.getId(),

                            setting
                                    .getScopeType()
                                    .getDisplayName(),

                            setting.getTargetName(),

                            setting.getVatRate()
                                    .stripTrailingZeros()
                                    .toPlainString()
                                    + "%",

                            formatDateTime(
                                    setting
                                            .getEffectiveFrom()),

                            setting.getEffectiveTo() == null
                                    ? "Không thời hạn"
                                    : formatDateTime(
                                            setting
                                                    .getEffectiveTo()),

                            setting.isEnabled()
                                    ? "Đang áp dụng"
                                    : "Ngừng áp dụng",

                            setting.getCreatedByName() == null
                                    ? "-"
                                    : setting
                                            .getCreatedByName(),

                            formatDateTime(
                                    setting.getUpdatedAt())
                    });
        }

        resultCountLabel.setText(
                displayedSettings.size()
                        + " cấu hình");
    }

    private void openCreateDialog() {
        Window owner = SwingUtilities
                .getWindowAncestor(this);

        VatSettingDialog dialog = new VatSettingDialog(
                owner,
                null);

        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadSettings();
        }
    }

    private void openEditDialog() {
        VatSetting setting = getSelectedSetting();

        if (setting == null) {
            showWarning(
                    "Vui lòng chọn cấu hình VAT cần sửa.");
            return;
        }

        Window owner = SwingUtilities
                .getWindowAncestor(this);

        VatSettingDialog dialog = new VatSettingDialog(
                owner,
                setting);

        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadSettings();
        }
    }

    private void toggleSelectedSetting() {
        VatSetting setting = getSelectedSetting();

        if (setting == null) {
            showWarning(
                    "Vui lòng chọn cấu hình VAT cần bật hoặc tắt.");
            return;
        }

        String action = setting.isEnabled()
                ? "ngừng áp dụng"
                : "kích hoạt";

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn "
                        + action
                        + " cấu hình VAT "
                        + setting
                                .getVatRate()
                                .stripTrailingZeros()
                                .toPlainString()
                        + "% cho \""
                        + setting.getTargetName()
                        + "\"?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            vatService.toggleEnabled(
                    setting.getId());

            JOptionPane.showMessageDialog(
                    this,
                    setting.isEnabled()
                            ? "Đã ngừng áp dụng cấu hình VAT."
                            : "Kích hoạt cấu hình VAT thành công.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            loadSettings();

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            showError(
                    exception.getMessage());
        }
    }

    private void deleteSelectedSetting() {
        VatSetting setting = getSelectedSetting();

        if (setting == null) {
            showWarning(
                    "Vui lòng chọn cấu hình VAT cần xóa.");
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa cấu hình VAT "
                        + setting
                                .getVatRate()
                                .stripTrailingZeros()
                                .toPlainString()
                        + "% của \""
                        + setting.getTargetName()
                        + "\"?\n"
                        + "Thao tác này không thể hoàn tác.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            vatService.delete(
                    setting.getId());

            JOptionPane.showMessageDialog(
                    this,
                    "Xóa cấu hình VAT thành công.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            loadSettings();

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            showError(
                    exception.getMessage());
        }
    }

    private VatSetting getSelectedSetting() {
        int selectedViewRow = vatTable.getSelectedRow();

        if (selectedViewRow < 0) {
            return null;
        }

        int selectedModelRow = vatTable.convertRowIndexToModel(
                selectedViewRow);

        if (selectedModelRow < 0
                || selectedModelRow >= displayedSettings.size()) {

            return null;
        }

        return displayedSettings.get(
                selectedModelRow);
    }

    private VatScopeType getSelectedScope() {
        Object selectedItem = scopeFilterComboBox
                .getSelectedItem();

        return selectedItem instanceof VatScopeType scopeType
                ? scopeType
                : null;
    }

    private void resetFilters() {
        loading = true;

        scopeFilterComboBox
                .setSelectedIndex(0);

        statusFilterComboBox
                .setSelectedIndex(0);

        loading = false;

        loadSettings();
    }

    private void setLoading(
            boolean loading) {
        this.loading = loading;

        addButton.setEnabled(!loading);
        refreshButton.setEnabled(!loading);

        setCursor(
                loading
                        ? Cursor.getPredefinedCursor(
                                Cursor.WAIT_CURSOR)
                        : Cursor.getDefaultCursor());
    }

    private String formatDateTime(
            java.time.LocalDateTime dateTime) {
        return dateTime == null
                ? "-"
                : dateTime.format(
                        dateFormatter);
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