package com.csms.view.admin.recipe;

import com.csms.dao.IngredientDAO;
import com.csms.dao.ProductDAO;
import com.csms.entity.Ingredient;
import com.csms.entity.Product;
import com.csms.entity.ProductRecipe;
import com.csms.service.RecipeService;

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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

public class RecipeManagementPanel extends JPanel {

    private final ProductDAO productDAO;
    private final IngredientDAO ingredientDAO;
    private final RecipeService recipeService;

    private final JComboBox<Product> productComboBox;

    private final DefaultTableModel tableModel;
    private final JTable recipeTable;

    private final JLabel recipeCountLabel;

    private List<ProductRecipe> displayedRecipes;

    private boolean loadingProducts;

    public RecipeManagementPanel() {
        productDAO = new ProductDAO();
        ingredientDAO = new IngredientDAO();
        recipeService = new RecipeService();

        productComboBox = new JComboBox<>();

        tableModel = createTableModel();
        recipeTable = new JTable(tableModel);

        recipeCountLabel = new JLabel("0 nguyên liệu");

        displayedRecipes = new ArrayList<>();

        loadingProducts = false;

        initializeComponents();
        registerEvents();
        loadProducts();
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
                "QUẢN LÝ CÔNG THỨC");

        titleLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        25));

        JLabel descriptionLabel = new JLabel(
                "Thiết lập nguyên liệu và định lượng cho từng món");

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

        productComboBox.setPreferredSize(
                new Dimension(330, 36));

        JButton refreshButton = new JButton("Làm mới");

        refreshButton.addActionListener(
                event -> {
                    loadProducts();
                });

        filterPanel.add(
                new JLabel("Chọn món:"));

        filterPanel.add(
                productComboBox);

        filterPanel.add(
                refreshButton);

        filterPanel.add(
                recipeCountLabel);

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

        JScrollPane scrollPane = new JScrollPane(recipeTable);

        JPanel actionPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT));

        actionPanel.setOpaque(false);

        JButton editButton = new JButton(
                "Sửa định lượng");

        JButton deleteButton = new JButton(
                "Xóa khỏi công thức");

        editButton.addActionListener(
                event -> openEditDialog());

        deleteButton.addActionListener(
                event -> deleteSelectedRecipe());

        actionPanel.add(editButton);
        actionPanel.add(deleteButton);

        panel.add(
                scrollPane,
                BorderLayout.CENTER);

        panel.add(
                actionPanel,
                BorderLayout.SOUTH);

        return panel;
    }

    private void configureTable() {
        recipeTable.setRowHeight(34);

        recipeTable.setFillsViewportHeight(
                true);

        recipeTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        recipeTable.setAutoCreateRowSorter(
                true);

        recipeTable.getTableHeader()
                .setReorderingAllowed(false);

        recipeTable.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(50);

        recipeTable.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(260);

        recipeTable.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(150);

        recipeTable.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(130);
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(
                new Object[] {
                        "ID",
                        "Nguyên liệu",
                        "Định lượng",
                        "Đơn vị"
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
        productComboBox.addActionListener(
                event -> {
                    if (!loadingProducts) {
                        loadRecipes();
                    }
                });

        recipeTable.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent event) {
                        if (event.getClickCount() == 2
                                && recipeTable
                                        .getSelectedRow() >= 0) {

                            openEditDialog();
                        }
                    }
                });
    }

    private void loadProducts() {
        loadingProducts = true;

        productComboBox.removeAllItems();

        try {
            for (Product product : productDAO.findAll()) {

                productComboBox.addItem(
                        product);
            }

        } catch (IllegalStateException exception) {
            showError(
                    exception.getMessage());

        } finally {
            loadingProducts = false;
        }

        loadRecipes();
    }

    private void loadRecipes() {
        Product selectedProduct = getSelectedProduct();

        tableModel.setRowCount(0);
        displayedRecipes.clear();

        if (selectedProduct == null) {
            recipeCountLabel.setText(
                    "0 nguyên liệu");

            return;
        }

        SwingWorker<List<ProductRecipe>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<ProductRecipe> doInBackground() {

                return recipeService.findByProductId(
                                selectedProduct.getId());
            }

            @Override
            protected void done() {
                try {
                    displayedRecipes = get();
                    displayRecipes();

                } catch (Exception exception) {
                    Throwable cause = exception.getCause() == null
                            ? exception
                            : exception.getCause();

                    showError(
                            cause.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void displayRecipes() {
        tableModel.setRowCount(0);

        for (ProductRecipe recipe : displayedRecipes) {

            tableModel.addRow(
                    new Object[] {
                            recipe.getId(),
                            recipe.getIngredientName(),
                            recipe
                                    .getQuantityRequired()
                                    .stripTrailingZeros()
                                    .toPlainString(),
                            recipe.getUnit()
                    });
        }

        recipeCountLabel.setText(
                displayedRecipes.size()
                        + " nguyên liệu");
    }

    private void openCreateDialog() {
        Product product = getSelectedProduct();

        if (product == null) {
            showWarning(
                    "Vui lòng chọn món.");
            return;
        }

        List<Ingredient> ingredients = ingredientDAO.findAllActive();

        if (ingredients.isEmpty()) {
            showWarning(
                    "Chưa có nguyên liệu đang hoạt động.");
            return;
        }

        Window owner = SwingUtilities
                .getWindowAncestor(this);

        RecipeDialog dialog = new RecipeDialog(
                owner,
                product.getId(),
                product.getName(),
                ingredients,
                null);

        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadRecipes();
        }
    }

    private void openEditDialog() {
        ProductRecipe recipe = getSelectedRecipe();

        if (recipe == null) {
            showWarning(
                    "Vui lòng chọn nguyên liệu cần sửa.");
            return;
        }

        Product product = getSelectedProduct();

        List<Ingredient> ingredients = ingredientDAO.findAllActive();

        Window owner = SwingUtilities
                .getWindowAncestor(this);

        RecipeDialog dialog = new RecipeDialog(
                owner,
                product.getId(),
                product.getName(),
                ingredients,
                recipe);

        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadRecipes();
        }
    }

    private void deleteSelectedRecipe() {
        ProductRecipe recipe = getSelectedRecipe();

        if (recipe == null) {
            showWarning(
                    "Vui lòng chọn nguyên liệu cần xóa.");
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Xóa nguyên liệu \""
                        + recipe.getIngredientName()
                        + "\" khỏi công thức?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            recipeService.delete(
                    recipe.getId());

            JOptionPane.showMessageDialog(
                    this,
                    "Đã xóa nguyên liệu khỏi công thức.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            loadRecipes();

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            showError(
                    exception.getMessage());
        }
    }

    private Product getSelectedProduct() {
        return (Product) productComboBox
                .getSelectedItem();
    }

    private ProductRecipe getSelectedRecipe() {
        int selectedViewRow = recipeTable.getSelectedRow();

        if (selectedViewRow < 0) {
            return null;
        }

        int selectedModelRow = recipeTable.convertRowIndexToModel(
                selectedViewRow);

        if (selectedModelRow < 0
                || selectedModelRow >= displayedRecipes.size()) {

            return null;
        }

        return displayedRecipes.get(
                selectedModelRow);
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