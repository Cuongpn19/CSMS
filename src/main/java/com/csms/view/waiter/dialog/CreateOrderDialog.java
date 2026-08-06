package com.csms.view.waiter.dialog;

import com.csms.dao.CategoryDAO;
import com.csms.dao.ProductDAO;
import com.csms.dto.CreateTableOrderRequest;
// import com.csms.dto.OrderResult;
import com.csms.entity.Category;
import com.csms.entity.Product;
import com.csms.entity.TableDashboardItem;
import com.csms.service.CoffeeTableService;
import com.csms.service.TableOrderService;
import com.csms.utils.SessionManager;
import com.csms.view.waiter.model.CartItem;
import com.csms.view.waiter.model.CartModel;
import com.csms.view.waiter.panel.CartPanel;
import com.csms.view.waiter.panel.CategoryPanel;
import com.csms.view.waiter.panel.OrderSummaryPanel;
import com.csms.view.waiter.panel.ProductGridPanel;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class CreateOrderDialog extends JDialog {

        private final TableDashboardItem tableItem;
        private final Consumer<CreateTableOrderRequest> submitHandler;

        private final CategoryDAO categoryDAO;
        private final ProductDAO productDAO;
        private final CoffeeTableService coffeeTableService;
        // private final TableOrderService tableOrderService;

        private final CartModel cartModel;

        private final CategoryPanel categoryPanel;
        private final ProductGridPanel productGridPanel;
        private final CartPanel cartPanel;
        private final OrderSummaryPanel orderSummaryPanel;

        private final JTextField searchField;
        private final JLabel loadingLabel;

        private List<Product> allProducts;

        private Integer selectedCategoryId;
        private boolean loading;
        private boolean submitted;

        public CreateOrderDialog(
                        Window owner,
                        TableDashboardItem tableItem,
                        Consumer<CreateTableOrderRequest> submitHandler) {
                super(
                                owner,
                                "Tạo đơn - Bàn "
                                                + tableItem.getTableNumber(),
                                ModalityType.APPLICATION_MODAL);

                this.tableItem = tableItem;
                this.submitHandler = submitHandler;

                categoryDAO = new CategoryDAO();
                productDAO = new ProductDAO();

                coffeeTableService = new CoffeeTableService();
                // tableOrderService = new TableOrderService();

                cartModel = new CartModel();

                categoryPanel = new CategoryPanel();

                productGridPanel = new ProductGridPanel();

                cartPanel = new CartPanel(cartModel);

                orderSummaryPanel = new OrderSummaryPanel(
                                cartModel,
                                this::submitOrder);

                searchField = new JTextField();
                loadingLabel = new JLabel(" ");

                allProducts = new ArrayList<>();

                selectedCategoryId = null;
                loading = false;
                submitted = false;

                initializeComponents();
                registerEvents();
                loadInitialData();
        }

        private void initializeComponents() {
                setDefaultCloseOperation(
                                DISPOSE_ON_CLOSE);

                setMinimumSize(
                                new Dimension(1180, 720));

                setPreferredSize(
                                new Dimension(1450, 850));

                JPanel rootPanel = new JPanel(
                                new BorderLayout(0, 0));

                rootPanel.setBackground(
                                new Color(246, 248, 252));

                rootPanel.add(
                                createHeaderPanel(),
                                BorderLayout.NORTH);

                rootPanel.add(
                                createMainContentPanel(),
                                BorderLayout.CENTER);

                setContentPane(rootPanel);

                pack();
                setLocationRelativeTo(getOwner());
        }

        private JPanel createHeaderPanel() {
                JPanel panel = new JPanel(
                                new BorderLayout());

                panel.setBackground(Color.WHITE);

                panel.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createMatteBorder(
                                                                0,
                                                                0,
                                                                1,
                                                                0,
                                                                new Color(226, 230, 237)),
                                                BorderFactory.createEmptyBorder(
                                                                14,
                                                                20,
                                                                14,
                                                                20)));

                JPanel titlePanel = new JPanel(
                                new BorderLayout(0, 3));

                titlePanel.setOpaque(false);

                JLabel titleLabel = new JLabel(
                                String.format(
                                                "TẠO ĐƠN - BÀN %02d",
                                                tableItem.getTableNumber()));

                titleLabel.setFont(
                                new Font(
                                                Font.SANS_SERIF,
                                                Font.BOLD,
                                                22));

                JLabel waiterLabel = new JLabel(
                                "Waiter: "
                                                + getCurrentUserName());

                waiterLabel.setForeground(
                                new Color(107, 114, 128));

                titlePanel.add(
                                titleLabel,
                                BorderLayout.NORTH);

                titlePanel.add(
                                waiterLabel,
                                BorderLayout.SOUTH);

                JPanel searchPanel = new JPanel(
                                new BorderLayout(10, 0));

                searchPanel.setOpaque(false);

                searchField.setPreferredSize(
                                new Dimension(300, 38));

                searchField.putClientProperty(
                                "JTextField.placeholderText",
                                "Tìm món...");

                loadingLabel.setForeground(
                                new Color(37, 99, 235));

                searchPanel.add(
                                searchField,
                                BorderLayout.CENTER);

                searchPanel.add(
                                loadingLabel,
                                BorderLayout.EAST);

                panel.add(
                                titlePanel,
                                BorderLayout.WEST);

                panel.add(
                                searchPanel,
                                BorderLayout.EAST);

                return panel;
        }

        private JSplitPane createMainContentPanel() {
                JPanel leftContentPanel = new JPanel(
                                new BorderLayout());

                leftContentPanel.setBackground(
                                new Color(246, 248, 252));

                JScrollPane categoryScrollPane = new JScrollPane(categoryPanel);

                categoryScrollPane.setBorder(
                                BorderFactory.createEmptyBorder());

                categoryScrollPane.setPreferredSize(
                                new Dimension(180, 0));

                categoryScrollPane
                                .getVerticalScrollBar()
                                .setUnitIncrement(14);

                JScrollPane productScrollPane = new JScrollPane(productGridPanel);

                productScrollPane.setBorder(
                                BorderFactory.createEmptyBorder(
                                                12,
                                                12,
                                                12,
                                                12));

                productScrollPane
                                .getVerticalScrollBar()
                                .setUnitIncrement(18);

                productScrollPane
                                .getViewport()
                                .setBackground(
                                                new Color(246, 248, 252));

                JSplitPane menuSplitPane = new JSplitPane(
                                JSplitPane.HORIZONTAL_SPLIT,
                                categoryScrollPane,
                                productScrollPane);

                menuSplitPane.setDividerLocation(180);
                menuSplitPane.setDividerSize(5);
                menuSplitPane.setBorder(null);

                leftContentPanel.add(
                                menuSplitPane,
                                BorderLayout.CENTER);

                JPanel rightPanel = new JPanel(
                                new BorderLayout());

                rightPanel.setBackground(Color.WHITE);

                rightPanel.setPreferredSize(
                                new Dimension(440, 0));

                rightPanel.setBorder(
                                BorderFactory.createMatteBorder(
                                                0,
                                                1,
                                                0,
                                                0,
                                                new Color(226, 230, 237)));

                rightPanel.add(
                                cartPanel,
                                BorderLayout.CENTER);

                rightPanel.add(
                                orderSummaryPanel,
                                BorderLayout.SOUTH);

                JSplitPane rootSplitPane = new JSplitPane(
                                JSplitPane.HORIZONTAL_SPLIT,
                                leftContentPanel,
                                rightPanel);

                rootSplitPane.setResizeWeight(0.72);
                rootSplitPane.setDividerSize(6);
                rootSplitPane.setBorder(null);

                return rootSplitPane;
        }

        private void registerEvents() {
                searchField.getDocument()
                                .addDocumentListener(
                                                new javax.swing.event.DocumentListener() {
                                                        @Override
                                                        public void insertUpdate(
                                                                        javax.swing.event.DocumentEvent event) {
                                                                applyProductFilter();
                                                        }

                                                        @Override
                                                        public void removeUpdate(
                                                                        javax.swing.event.DocumentEvent event) {
                                                                applyProductFilter();
                                                        }

                                                        @Override
                                                        public void changedUpdate(
                                                                        javax.swing.event.DocumentEvent event) {
                                                                applyProductFilter();
                                                        }
                                                });
        }

        private void loadInitialData() {
                setLoading(
                                true,
                                "Đang tải menu...");

                SwingWorker<InitialData, Void> worker = new SwingWorker<>() {

                        @Override
                        protected InitialData doInBackground() {

                                List<Category> categories = categoryDAO
                                                .findAllActive();

                                /*
                                 * Nếu ProductDAO của bạn dùng findAll()
                                 * hoặc filter(), thay method tại đây.
                                 */
                                List<Product> products = productDAO
                                                .findAvailable();

                                return new InitialData(
                                                categories,
                                                products);
                        }

                        @Override
                        protected void done() {
                                try {
                                        InitialData data = get();

                                        allProducts = new ArrayList<>(
                                                        data.products());

                                        allProducts.sort(
                                                        Comparator.comparing(
                                                                        Product::getName,
                                                                        String.CASE_INSENSITIVE_ORDER));

                                        categoryPanel
                                                        .displayCategories(
                                                                        data.categories(),
                                                                        categoryId -> {
                                                                                selectedCategoryId = categoryId;

                                                                                applyProductFilter();
                                                                        });

                                        applyProductFilter();

                                } catch (InterruptedException exception) {
                                        Thread.currentThread()
                                                        .interrupt();

                                        showError(
                                                        "Quá trình tải menu đã bị gián đoạn.");

                                } catch (ExecutionException exception) {
                                        Throwable cause = exception.getCause() == null
                                                        ? exception
                                                        : exception.getCause();

                                        showError(
                                                        cause.getMessage());

                                } finally {
                                        setLoading(false, " ");
                                }
                        }
                };

                worker.execute();
        }

        private void applyProductFilter() {
                String keyword = searchField.getText()
                                .trim()
                                .toLowerCase();

                List<Product> filtered = new ArrayList<>();

                for (Product product : allProducts) {
                        boolean matchesCategory = selectedCategoryId == null
                                        || product.getCategoryId() == selectedCategoryId;

                        boolean matchesKeyword = keyword.isBlank()
                                        || product.getName()
                                                        .toLowerCase()
                                                        .contains(keyword);

                        if (matchesCategory
                                        && matchesKeyword) {

                                filtered.add(product);
                        }
                }

                productGridPanel.displayProducts(
                                filtered,
                                this::addProductToCart);
        }

        private void addProductToCart(
                        Product product) {
                try {
                        cartModel.addProduct(product);

                } catch (
                                IllegalArgumentException
                                | IllegalStateException exception) {
                        showWarning(
                                        exception.getMessage());
                }
        }

        private void submitOrder() {
                if (loading) {
                        return;
                }

                if (cartModel.isEmpty()) {
                        showWarning(
                                        "Vui lòng chọn ít nhất một món.");
                        return;
                }

                try {
                        coffeeTableService
                                        .validateCanCreateOrder(
                                                        tableItem.getTableId());

                } catch (
                                IllegalArgumentException
                                | IllegalStateException exception) {
                        showWarning(
                                        exception.getMessage());

                        return;
                }

                int confirmation = JOptionPane.showConfirmDialog(
                                this,
                                "Xác nhận gửi "
                                                + cartModel.getTotalQuantity()
                                                + " món của bàn "
                                                + tableItem.getTableNumber()
                                                + " sang Barista?\n\n"
                                                + "Tổng tiền tạm tính: "
                                                + cartModel.getTotalAmount(),
                                "Xác nhận gửi đơn",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);

                if (confirmation != JOptionPane.YES_OPTION) {
                        return;
                }

                if (SessionManager.getCurrentUser() == null) {

                        showError(
                                        "Không tìm thấy phiên đăng nhập.");

                        return;
                }

                CreateTableOrderRequest request = new CreateTableOrderRequest(
                                tableItem.getTableId(),
                                tableItem.getTableNumber(),

                                SessionManager
                                                .getCurrentUser()
                                                .getId(),

                                orderSummaryPanel
                                                .getOrderNote(),

                                copyCartItems(),

                                cartModel.getSubtotal(),
                                cartModel.getVatAmount(),
                                cartModel.getTotalAmount());

                try {
                        setLoading(
                                        true,
                                        "Đang gửi đơn...");

                        if (submitHandler != null) {
                                submitHandler.accept(request);
                        }

                        submitted = true;
                        dispose();

                } catch (
                                IllegalArgumentException
                                | IllegalStateException
                                | SecurityException exception) {
                        showError(
                                        exception.getMessage());

                } finally {
                        if (isDisplayable()) {
                                setLoading(false, " ");
                        }
                }
        }

        private List<CartItem> copyCartItems() {
                List<CartItem> copiedItems = new ArrayList<>();

                for (CartItem item : cartModel.getItems()) {

                        CartItem copiedItem = new CartItem(
                                        item.getProduct(),
                                        item.getQuantity());

                        copiedItem.setNote(
                                        item.getNote());

                        copiedItem.setVatRate(
                                        item.getVatRate());

                        copiedItem.setVatAmount(
                                        item.getVatAmount());

                        copiedItems.add(copiedItem);
                }

                return List.copyOf(copiedItems);
        }

        private String getCurrentUserName() {
                if (SessionManager.getCurrentUser() == null) {

                        return "-";
                }

                String fullName = SessionManager
                                .getCurrentUser()
                                .getFullName();

                if (fullName == null
                                || fullName.isBlank()) {

                        return SessionManager
                                        .getCurrentUser()
                                        .getUsername();
                }

                return fullName;
        }

        private void setLoading(
                        boolean loading,
                        String message) {
                this.loading = loading;

                searchField.setEnabled(!loading);

                loadingLabel.setText(
                                message == null
                                                ? " "
                                                : message);

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
                                message == null
                                                ? "Đã xảy ra lỗi không xác định."
                                                : message,
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
        }

        public boolean isSubmitted() {
                return submitted;
        }

        private record InitialData(
                        List<Category> categories,
                        List<Product> products) {
        }
}