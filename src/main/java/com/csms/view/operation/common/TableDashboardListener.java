package com.csms.view.operation.common;

import com.csms.entity.TableDashboardItem;

public interface TableDashboardListener {

    void onCreateOrderRequested(
            TableDashboardItem tableItem);

    void onViewTableRequested(
            TableDashboardItem tableItem);
}