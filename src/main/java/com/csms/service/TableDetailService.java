package com.csms.service;

import com.csms.dao.TableDetailDAO;
import com.csms.dto.TableOrderDetailView;

public class TableDetailService {

    private final TableDetailDAO tableDetailDAO;

    public TableDetailService() {
        tableDetailDAO = new TableDetailDAO();
    }

    public TableOrderDetailView getByTableId(
            int tableId) {
        if (tableId <= 0) {
            throw new IllegalArgumentException(
                    "Mã bàn không hợp lệ.");
        }

        return tableDetailDAO
                .findByTableId(tableId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy bàn."));
    }
}