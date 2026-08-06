package com.csms.service;

import com.csms.dao.RevenueReportDAO;
import com.csms.dto.RevenueReportRow;

import java.time.Year;
import java.util.List;

public class RevenueReportService {

    private final RevenueReportDAO revenueReportDAO;

    public RevenueReportService() {
        revenueReportDAO = new RevenueReportDAO();
    }

    public List<RevenueReportRow> getMonthlyReport(
            int year,
            int month) {
        validateYear(year);

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Tháng báo cáo phải từ 1 đến 12.");
        }

        return revenueReportDAO
                .findMonthlyReport(
                        year,
                        month);
    }

    public List<RevenueReportRow> getYearlyReport(
            int year) {
        validateYear(year);

        return revenueReportDAO
                .findYearlyReport(year);
    }

    private void validateYear(
            int year) {
        int currentYear = Year.now().getValue();

        if (year < 2000
                || year > currentYear + 1) {

            throw new IllegalArgumentException(
                    "Năm báo cáo không hợp lệ.");
        }
    }
}