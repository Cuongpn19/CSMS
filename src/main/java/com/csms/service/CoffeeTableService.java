package com.csms.service;

import com.csms.dao.CoffeeTableDAO;
import com.csms.dto.TableDashboardSummary;
import com.csms.entity.CoffeeTable;
import com.csms.entity.OrderStatus;
import com.csms.entity.TableDashboardItem;
import com.csms.entity.TableStatus;

import java.util.List;

public class CoffeeTableService {

    private final CoffeeTableDAO coffeeTableDAO;

    public CoffeeTableService() {
        this.coffeeTableDAO = new CoffeeTableDAO();
    }

    public List<TableDashboardItem> getDashboardTables() {
        return coffeeTableDAO.findAllDashboard();
    }

    public List<CoffeeTable> getAvailableTables() {
        return coffeeTableDAO.findAvailableTables();
    }

    public CoffeeTable getTableById(int tableId) {
        validateTableId(tableId);

        return coffeeTableDAO.findById(tableId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy bàn."));
    }

    public boolean hasActiveOrder(int tableId) {
        validateTableId(tableId);

        return coffeeTableDAO.hasActiveOrder(tableId);
    }

    public boolean canCreateOrder(int tableId) {
        CoffeeTable table = getTableById(tableId);

        return table.getStatus() == TableStatus.AVAILABLE
                && !coffeeTableDAO.hasActiveOrder(tableId);
    }

    public void validateCanCreateOrder(int tableId) {
        CoffeeTable table = getTableById(tableId);

        if (table.getStatus() == TableStatus.INACTIVE) {
            throw new IllegalStateException(
                    "Bàn đang ngừng sử dụng.");
        }

        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Bàn đang phục vụ và không thể tạo đơn mới.");
        }

        if (coffeeTableDAO.hasActiveOrder(tableId)) {
            throw new IllegalStateException(
                    "Bàn đã có một đơn hàng đang hoạt động.");
        }
    }

    public void occupyTable(int tableId) {
        validateTableId(tableId);

        CoffeeTable table = getTableById(tableId);

        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Chỉ có thể chuyển bàn trống sang đang phục vụ.");
        }

        coffeeTableDAO.updateStatus(
                tableId,
                TableStatus.OCCUPIED);
    }

    public void releaseTable(int tableId) {
        validateTableId(tableId);

        if (coffeeTableDAO.hasActiveOrder(tableId)) {
            throw new IllegalStateException(
                    "Không thể giải phóng bàn vì vẫn còn đơn hàng đang hoạt động.");
        }

        coffeeTableDAO.updateStatus(
                tableId,
                TableStatus.AVAILABLE);
    }

    public TableDashboardSummary calculateSummary(
            List<TableDashboardItem> items) {
        TableDashboardSummary summary = new TableDashboardSummary();

        if (items == null || items.isEmpty()) {
            return summary;
        }

        summary.setTotalTables(items.size());

        for (TableDashboardItem item : items) {
            if (item.getTableStatus() == TableStatus.INACTIVE) {
                summary.setInactiveTables(
                        summary.getInactiveTables() + 1);

                continue;
            }

            if (item.getTableStatus() == TableStatus.AVAILABLE
                    && !item.hasActiveOrder()) {

                summary.setAvailableTables(
                        summary.getAvailableTables() + 1);

                continue;
            }

            OrderStatus orderStatus = item.getOrderStatus();

            if (orderStatus == null) {
                continue;
            }

            switch (orderStatus) {
                case IN_PROGRESS, PREPARING ->
                    summary.setInProgressTables(
                            summary.getInProgressTables() + 1);

                case PREPARED ->
                    summary.setPreparedTables(
                            summary.getPreparedTables() + 1);

                case SERVED, WAITING_PAYMENT ->
                    summary.setServedTables(
                            summary.getServedTables() + 1);

                default -> {
                    // Không cộng các trạng thái khác vào thống kê vận hành.
                }
            }
        }

        return summary;
    }

    private void validateTableId(int tableId) {
        if (tableId <= 0) {
            throw new IllegalArgumentException(
                    "Mã bàn không hợp lệ.");
        }
    }
}