package com.csms.view.admin.ingredient;

import com.csms.entity.Ingredient;
import com.csms.entity.IngredientStatus;
import com.csms.service.IngredientService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IngredientManagementPanel
        extends JPanel {

    private static final String ALL_STATUSES = "Tất cả trạng thái";

    private final IngredientService ingredientService;

    private final JTextField keywordField;
    private final JComboBox<Object> statusFilter;
    private final JCheckBox lowStockCheckBox;

    private final DefaultTableModel tableModel;
    private final JTable ingredientTable;

    private final JLabel resultCountLabel;

    private final NumberFormat currencyFormat;
    private final DateTimeFormatter dateFormatter;

    private List<Ingredient> displayedIngredients;

    private boolean loading;

    public IngredientManagementPanel() {
        ingredientService = new IngredientService();

        keywordField = new JTextField();

        statusFilter = new JComboBox<>();

        lowStockCheckBox = new JCheckBox(
                "Chỉ hiện sắp hết hàng");

        tableModel = createTableModel();

        ingredientTable = new JTable(tableModel);

        resultCountLabel = new JLabel("0 nguyên liệu");

        currencyFormat = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("vi-VN"));

        dateFormatter = DateTimeFormatter.ofPattern(
                "dd/MM/yyyy HH:mm");

        displayedIngredients = new ArrayList<>();

        loading = false;

        initializeFilters();
        initializeComponents();
        registerEvents();
        loadIngredients();
    }

    private void initializeFilters() {
        statusFilter.addItem(
                ALL_STATUSES);

        for (IngredientStatus status : IngredientStatus.values()) {

            statusFilter.addItem(status);
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
        JPanel wrapper = new JPanel(
                new BorderLayout(
                        0,
                        15));

        wrapper.setOpaque(false);

        JPanel titlePanel = new JPanel(
                new BorderLayout());

        titlePanel.setOpaque(false);

        JPanel titleTextPanel = new JPanel(
                new BorderLayout(
                        0,
                        4));

        titleTextPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(
                "QUẢN LÝ NGUYÊN LIỆU");

        titleLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        25));

        JLabel descriptionLabel = new JLabel(
                "Quản lý tồn kho, đơn vị tính và mức cảnh báo");

        descriptionLabel.setForeground(
                new Color(105, 113, 128));

        titleTextPanel.add(
                titleLabel,
                BorderLayout.NORTH);

        titleTextPanel.add(
                descriptionLabel,
                BorderLayout.SOUTH);

        JButton addButton = new JButton(
                "Thêm nguyên liệu");

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

        statusFilter.setPreferredSize(
                new Dimension(180, 36));

        lowStockCheckBox.setOpaque(false);

        JButton searchButton = new JButton("Tìm kiếm");

        JButton refreshButton = new JButton("Làm mới");

        searchButton.addActionListener(
                event -> loadIngredients());

        refreshButton.addActionListener(
                event -> resetFilters());

        filterPanel.add(
                new JLabel("Từ khóa:"));

        filterPanel.add(keywordField);

        filterPanel.add(
                new JLabel("Trạng thái:"));

        filterPanel.add(statusFilter);
        filterPanel.add(lowStockCheckBox);
        filterPanel.add(searchButton);
        filterPanel.add(refreshButton);
        filterPanel.add(resultCountLabel);

        wrapper.add(
                titlePanel,
                BorderLayout.NORTH);

        wrapper.add(
                filterPanel,
                BorderLayout.SOUTH);

        return wrapper;
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

        JScrollPane scrollPane = new JScrollPane(
                ingredientTable);

        JPanel actionPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT));

        actionPanel.setOpaque(false);

        JButton editButton = new JButton("Sửa");

        JButton statusButton = new JButton(
                "Kích hoạt / Ngừng sử dụng");

        editButton.addActionListener(
                event -> openEditDialog());

        statusButton.addActionListener(
                event -> toggleSelectedStatus());

        actionPanel.add(editButton);
        actionPanel.add(statusButton);

        panel.add(
                scrollPane,
                BorderLayout.CENTER);

        panel.add(
                actionPanel,
                BorderLayout.SOUTH);

        return panel;
    }

    private void configureTable() {
        ingredientTable.setRowHeight(34);

        ingredientTable.setFillsViewportHeight(
                true);

        ingredientTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        ingredientTable.setAutoCreateRowSorter(
                true);

        ingredientTable.getTableHeader()
                .setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();

        centerRenderer.setHorizontalAlignment(
                SwingConstants.CENTER);

        ingredientTable.getColumnModel()
                .getColumn(0)
                .setCellRenderer(centerRenderer);

        ingredientTable.getColumnModel()
                .getColumn(2)
                .setCellRenderer(centerRenderer);

        ingredientTable.getColumnModel()
                .getColumn(3)
                .setCellRenderer(centerRenderer);

        ingredientTable.getColumnModel()
                .getColumn(4)
                .setCellRenderer(centerRenderer);

        ingredientTable.getColumnModel()
                .getColumn(6)
                .setCellRenderer(centerRenderer);

        ingredientTable.getColumnModel()
                .getColumn(7)
                .setCellRenderer(centerRenderer);
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(
                new Object[] {
                        "ID",
                        "Tên nguyên liệu",
                        "Đơn vị",
                        "Tồn kho",
                        "Mức tối thiểu",
                        "Giá nhập",
                        "Trạng thái",
                        "Cảnh báo",
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

    private void registerEvents() {
        keywordField.addActionListener(
                event -> loadIngredients());

        statusFilter.addActionListener(
                event -> {
                    if (!loading) {
                        loadIngredients();
                    }
                });

        lowStockCheckBox.addActionListener(
                event -> loadIngredients());

        ingredientTable.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent event) {
                        if (event.getClickCount() == 2
                                && ingredientTable
                                        .getSelectedRow() >= 0) {

                            openEditDialog();
                        }
                    }
                });
    }

    private void loadIngredients() {
        if (loading) {
            return;
        }

        setLoading(true);

        String keyword = keywordField.getText().trim();

        IngredientStatus status = getSelectedStatus();

        boolean lowStockOnly = lowStockCheckBox.isSelected();

        SwingWorker<List<Ingredient>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<Ingredient> doInBackground() {

                return ingredientService.search(
                        keyword,
                        status,
                        lowStockOnly);
            }

            @Override
            protected void done() {
                try {
                    displayedIngredients = get();
                    displayIngredients();

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

    private void displayIngredients() {
        tableModel.setRowCount(0);

        for (Ingredient ingredient : displayedIngredients) {

            tableModel.addRow(
                    new Object[] {
                            ingredient.getId(),
                            ingredient.getName(),
                            ingredient.getUnit()
                                    .getDisplayName(),
                            formatNumber(
                                    ingredient.getQuantity()),
                            formatNumber(
                                    ingredient.getMinimumStock()),
                            currencyFormat.format(
                                    safeAmount(
                                            ingredient.getImportPrice())),
                            ingredient.getStatus()
                                    .getDisplayName(),
                            ingredient.isLowStock()
                                    ? "Sắp hết"
                                    : "Bình thường",
                            ingredient.getUpdatedAt() == null
                                    ? "-"
                                    : ingredient
                                            .getUpdatedAt()
                                            .format(dateFormatter)
                    });
        }

        resultCountLabel.setText(
                displayedIngredients.size()
                        + " nguyên liệu");
    }

    private void openCreateDialog() {
        Window owner = SwingUtilities
                .getWindowAncestor(this);

        IngredientDialog dialog = new IngredientDialog(
                owner,
                null);

        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadIngredients();
        }
    }

    private void openEditDialog() {
        Ingredient ingredient = getSelectedIngredient();

        if (ingredient == null) {
            showWarning(
                    "Vui lòng chọn nguyên liệu cần sửa.");
            return;
        }

        Window owner = SwingUtilities
                .getWindowAncestor(this);

        IngredientDialog dialog = new IngredientDialog(
                owner,
                ingredient);

        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadIngredients();
        }
    }

    private void toggleSelectedStatus() {
        Ingredient ingredient = getSelectedIngredient();

        if (ingredient == null) {
            showWarning(
                    "Vui lòng chọn nguyên liệu cần thay đổi trạng thái.");
            return;
        }

        boolean activating = ingredient.getStatus() == IngredientStatus.INACTIVE;

        String action = activating
                ? "kích hoạt"
                : "ngừng sử dụng";

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn "
                        + action
                        + " nguyên liệu \""
                        + ingredient.getName()
                        + "\"?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            ingredientService.toggleStatus(
                    ingredient.getId());

            JOptionPane.showMessageDialog(
                    this,
                    activating
                            ? "Kích hoạt nguyên liệu thành công."
                            : "Đã ngừng sử dụng nguyên liệu.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            loadIngredients();

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            showError(
                    exception.getMessage());
        }
    }

    private Ingredient getSelectedIngredient() {
        int selectedViewRow = ingredientTable.getSelectedRow();

        if (selectedViewRow < 0) {
            return null;
        }

        int selectedModelRow = ingredientTable.convertRowIndexToModel(
                selectedViewRow);

        if (selectedModelRow < 0
                || selectedModelRow >= displayedIngredients.size()) {

            return null;
        }

        return displayedIngredients.get(
                selectedModelRow);
    }

    private IngredientStatus getSelectedStatus() {
        Object selected = statusFilter.getSelectedItem();

        return selected instanceof IngredientStatus status
                ? status
                : null;
    }

    private void resetFilters() {
        loading = true;

        keywordField.setText("");
        statusFilter.setSelectedIndex(0);
        lowStockCheckBox.setSelected(false);

        loading = false;

        loadIngredients();
    }

    private void setLoading(
            boolean loading) {
        this.loading = loading;

        setCursor(
                loading
                        ? java.awt.Cursor
                                .getPredefinedCursor(
                                        java.awt.Cursor.WAIT_CURSOR)
                        : java.awt.Cursor
                                .getDefaultCursor());
    }

    private String formatNumber(
            BigDecimal value) {
        return value == null
                ? "0"
                : value.stripTrailingZeros()
                        .toPlainString();
    }

    private BigDecimal safeAmount(
            BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO
                : value;
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