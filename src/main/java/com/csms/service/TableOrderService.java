package com.csms.service;

import com.csms.config.DatabaseConnection;
import com.csms.dao.CoffeeTableDAO;
import com.csms.dao.OrderDAO;
import com.csms.dao.OrderDetailDAO;
import com.csms.dto.CreateTableOrderRequest;
import com.csms.dto.IngredientRequirement;
import com.csms.dto.OrderResult;
import com.csms.entity.CoffeeTable;
import com.csms.entity.Order;
import com.csms.entity.OrderDetail;
import com.csms.entity.OrderItemStatus;
import com.csms.entity.OrderStatus;
import com.csms.entity.OrderType;
import com.csms.entity.RoleName;
import com.csms.entity.TableStatus;
import com.csms.entity.User;
import com.csms.view.waiter.model.CartItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TableOrderService {

    private final CoffeeTableDAO coffeeTableDAO;
    private final OrderDAO orderDAO;
    private final OrderDetailDAO orderDetailDAO;
    private final VatService vatService;
    private final RoleAuthorizationService authorizationService;
    private final IngredientConsumptionService ingredientConsumptionService;

    public TableOrderService() {
        this.coffeeTableDAO = new CoffeeTableDAO();

        this.orderDAO = new OrderDAO();

        this.orderDetailDAO = new OrderDetailDAO();

        this.vatService = new VatService();
        authorizationService = new RoleAuthorizationService();

        ingredientConsumptionService = new IngredientConsumptionService();
    }

    public OrderResult createAndSendOrder(
            CreateTableOrderRequest request) {

        User waiter = authorizationService
                .requireRole(
                        RoleName.WAITER);

        validateRequest(request);

        if (request.waiterId() != waiter.getId()) {

            throw new SecurityException(
                    "Waiter tạo đơn không khớp với tài khoản đang đăng nhập.");
        }

        Connection connection = null;

        try {
            connection = DatabaseConnection.getConnection();

            connection.setAutoCommit(false);

            /*
             * 1. Khóa bàn để tránh hai Waiter
             * cùng tạo đơn cho một bàn.
             */
            CoffeeTable table = coffeeTableDAO
                    .findByIdForUpdate(
                            connection,
                            request.tableId())
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    "Không tìm thấy bàn."));

            /*
             * 2. Kiểm tra trạng thái bàn.
             */
            validateAvailableTable(
                    connection,
                    table);

            List<IngredientRequirement> requirements = ingredientConsumptionService
                    .lockAndValidateRequirements(
                            connection,
                            request.items());

            /*
             * 3. Không tin hoàn toàn số tiền gửi từ UI.
             * Tính lại từ CartItem trước khi lưu.
             */
            CalculatedOrder calculated = calculateOrder(
                    request.items());

            /*
             * 4. Tạo entity Order.
             */
            Order order = buildOrder(
                    request,
                    calculated);

            /*
             * 5. Insert orders và lấy ID.
             */
            int orderId = orderDAO.insertTableOrder(
                    connection,
                    order);

            /*
             * 6. Tạo và insert order_details.
             */
            List<OrderDetail> details = buildOrderDetails(
                    orderId,
                    request.items());

            orderDetailDAO.insertBatch(
                    connection,
                    details);

            // * 7. Sau này đặt bước trừ nguyên liệu tại đây.

            ingredientConsumptionService
                    .consumeLockedRequirements(
                            connection,
                            orderId,
                            request.waiterId(),
                            requirements);

            /*
             * 8. Chuyển bàn sang OCCUPIED.
             */
            coffeeTableDAO.updateStatus(
                    connection,
                    request.tableId(),
                    TableStatus.OCCUPIED);

            /*
             * 9. Commit toàn bộ.
             */
            connection.commit();

            return new OrderResult(
                    orderId,
                    order.getOrderCode(),
                    "Gửi đơn sang Barista thành công.");

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            rollbackQuietly(connection);
            throw exception;

        } catch (SQLException exception) {
            rollbackQuietly(connection);

            throw new IllegalStateException(
                    "Không thể tạo đơn hàng: "
                            + exception.getMessage(),
                    exception);

        } catch (RuntimeException exception) {
            rollbackQuietly(connection);
            throw exception;

        } finally {
            closeConnectionQuietly(connection);
        }
    }

    private void validateRequest(
            CreateTableOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Thông tin tạo đơn không hợp lệ.");
        }

        if (request.tableId() <= 0) {
            throw new IllegalArgumentException(
                    "Bàn được chọn không hợp lệ.");
        }

        if (request.waiterId() <= 0) {
            throw new IllegalArgumentException(
                    "Không xác định được Waiter tạo đơn.");
        }

        if (request.items() == null
                || request.items().isEmpty()) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn ít nhất một món.");
        }

        for (CartItem item : request.items()) {
            if (item == null
                    || item.getProduct() == null) {

                throw new IllegalArgumentException(
                        "Có món không hợp lệ trong đơn.");
            }

            if (item.getProduct().getId() <= 0) {
                throw new IllegalArgumentException(
                        "Mã sản phẩm không hợp lệ.");
            }

            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "Số lượng món phải lớn hơn 0.");
            }

            if (item.getUnitPrice() == null
                    || item.getUnitPrice()
                            .compareTo(BigDecimal.ZERO) < 0) {

                throw new IllegalArgumentException(
                        "Giá sản phẩm không hợp lệ.");
            }
        }
    }

    private void validateAvailableTable(
            Connection connection,
            CoffeeTable table) throws SQLException {

        if (table.getStatus() == TableStatus.INACTIVE) {

            throw new IllegalStateException(
                    "Bàn đang ngừng sử dụng.");
        }

        if (table.getStatus() != TableStatus.AVAILABLE) {

            throw new IllegalStateException(
                    "Bàn đang phục vụ, không thể tạo đơn mới.");
        }

        if (coffeeTableDAO.hasActiveOrder(
                connection,
                table.getId())) {
            throw new IllegalStateException(
                    "Bàn đã có một đơn hàng đang hoạt động.");
        }
    }

    private CalculatedOrder calculateOrder(
            List<CartItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;

        BigDecimal vatAmount = BigDecimal.ZERO;

        for (CartItem item : items) {
            BigDecimal lineSubtotal = item.getUnitPrice()
                    .multiply(
                            BigDecimal.valueOf(
                                    item.getQuantity()))
                    .setScale(
                            2,
                            RoundingMode.HALF_UP);

            BigDecimal vatRate = vatService.resolveRateForProduct(
                    item.getProduct().getId());

            BigDecimal lineVat = lineSubtotal
                    .multiply(vatRate)
                    .divide(
                            BigDecimal.valueOf(100),
                            2,
                            RoundingMode.HALF_UP);

            item.setVatRate(vatRate);
            item.setVatAmount(lineVat);

            subtotal = subtotal.add(
                    lineSubtotal);

            vatAmount = vatAmount.add(
                    lineVat);
        }

        BigDecimal discount = BigDecimal.ZERO;

        BigDecimal totalAmount = subtotal
                .subtract(discount)
                .add(vatAmount)
                .setScale(
                        2,
                        RoundingMode.HALF_UP);

        return new CalculatedOrder(
                subtotal,
                discount,
                vatAmount,
                totalAmount);
    }

    private Order buildOrder(
            CreateTableOrderRequest request,
            CalculatedOrder calculated) {
        Order order = new Order();

        order.setOrderCode(
                generateOrderCode());

        order.setTableId(
                request.tableId());

        order.setCashierId(
                request.waiterId());

        order.setOrderType(
                OrderType.DINE_IN);

        order.setStatus(
                OrderStatus.IN_PROGRESS);

        order.setSubtotal(
                calculated.subtotal());

        order.setDiscount(
                calculated.discount());

        order.setVatAmount(
                calculated.vatAmount());

        order.setTotalAmount(
                calculated.totalAmount());

        order.setNote(
                normalizeOptional(
                        request.orderNote()));

        return order;
    }

    private List<OrderDetail> buildOrderDetails(
            int orderId,
            List<CartItem> cartItems) {
        List<OrderDetail> details = new ArrayList<>();

        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();

            detail.setOrderId(orderId);

            detail.setProductId(
                    item.getProduct().getId());

            detail.setQuantity(
                    item.getQuantity());

            detail.setUnitPrice(
                    item.getUnitPrice());

            detail.setSubtotal(
                    item.getSubtotal()
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP));

            detail.setVatRate(
                    safeAmount(
                            item.getVatRate()));

            detail.setVatAmount(
                    safeAmount(
                            item.getVatAmount())
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP));

            detail.setStatus(
                    OrderItemStatus.IN_PROGRESS);

            detail.setNote(
                    normalizeOptional(
                            item.getNote()));

            details.add(detail);
        }

        return details;
    }

    private String generateOrderCode() {
        return "ORD-"
                + java.time.LocalDateTime
                        .now()
                        .format(
                                java.time.format.DateTimeFormatter
                                        .ofPattern(
                                                "yyyyMMddHHmmssSSS"));
    }

    // private void consumeLockedRequirements(
    // Connection connection,
    // int orderId,
    // int createdBy,
    // List<IngredientRequirement> requirements)
    // throws SQLException {

    // for (IngredientRequirement requirement : requirements) {

    // inventoryTransactionDAO.insert(
    // connection,
    // requirement.getIngredientId(),
    // orderId,
    // InventoryTransactionType.SALE,
    // requirement.getQuantityRequired(),
    // requirement.getQuantityBefore(),
    // requirement.getQuantityAfter(),
    // createdBy,
    // "Xuất nguyên liệu cho đơn #" + orderId);
    // }
    // }

    private BigDecimal safeAmount(
            BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private String normalizeOptional(
            String value) {
        if (value == null
                || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private void rollbackQuietly(
            Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void closeConnectionQuietly(
            Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.setAutoCommit(true);
        } catch (SQLException ignored) {
        }

        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }

    private record CalculatedOrder(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vatAmount,
            BigDecimal totalAmount) {
    }
}