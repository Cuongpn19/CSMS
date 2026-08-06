package com.csms;

import com.csms.dao.CoffeeTableDAO;
import com.csms.entity.TableDashboardItem;

public class TableDashboardTest {

    public static void main(String[] args) {
        CoffeeTableDAO coffeeTableDAO = new CoffeeTableDAO();

        for (TableDashboardItem item : coffeeTableDAO.findAllDashboard()) {

            System.out.println(
                    "Bàn "
                            + item.getTableNumber()
                            + " | "
                            + item.getTableStatus()
                            + " | "
                            + item.getDisplayStatus()
                            + " | Số món: "
                            + item.getTotalQuantity()
                            + " | Tổng: "
                            + item.getTotalAmount());
        }
    }
}