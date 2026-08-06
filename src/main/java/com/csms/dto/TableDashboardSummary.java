package com.csms.dto;

public class TableDashboardSummary {

    private int totalTables;
    private int availableTables;
    private int inProgressTables;
    private int preparedTables;
    private int servedTables;
    private int inactiveTables;

    public TableDashboardSummary() {
    }

    public int getTotalTables() {
        return totalTables;
    }

    public void setTotalTables(int totalTables) {
        this.totalTables = totalTables;
    }

    public int getAvailableTables() {
        return availableTables;
    }

    public void setAvailableTables(int availableTables) {
        this.availableTables = availableTables;
    }

    public int getInProgressTables() {
        return inProgressTables;
    }

    public void setInProgressTables(int inProgressTables) {
        this.inProgressTables = inProgressTables;
    }

    public int getPreparedTables() {
        return preparedTables;
    }

    public void setPreparedTables(int preparedTables) {
        this.preparedTables = preparedTables;
    }

    public int getServedTables() {
        return servedTables;
    }

    public void setServedTables(int servedTables) {
        this.servedTables = servedTables;
    }

    public int getInactiveTables() {
        return inactiveTables;
    }

    public void setInactiveTables(int inactiveTables) {
        this.inactiveTables = inactiveTables;
    }
}