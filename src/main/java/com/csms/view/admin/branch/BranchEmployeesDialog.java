package com.csms.view.admin.branch;

import com.csms.dto.BranchEmployee;
import com.csms.entity.Branch;
import com.csms.service.BranchService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.util.List;

public class BranchEmployeesDialog
        extends JDialog {

    private final BranchService branchService;
    private final Branch branch;

    private final DefaultTableModel tableModel;
    private final JTable employeeTable;

    public BranchEmployeesDialog(
            Window owner,
            Branch branch) {
        super(
                owner,
                "Nhân viên chi nhánh",
                ModalityType.APPLICATION_MODAL);

        this.branchService = new BranchService();

        this.branch = branch;

        tableModel = createTableModel();

        employeeTable = new JTable(tableModel);

        initializeComponents();
        loadEmployees();
    }

    private void initializeComponents() {
        setDefaultCloseOperation(
                DISPOSE_ON_CLOSE);

        JPanel rootPanel = new JPanel(
                new BorderLayout(
                        0,
                        15));

        rootPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20));

        JPanel headerPanel = new JPanel(
                new BorderLayout());

        JLabel titleLabel = new JLabel(
                branch.getName());

        titleLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        20));

        JLabel countLabel = new JLabel(
                branch.getEmployeeCount()
                        + " nhân viên");

        headerPanel.add(
                titleLabel,
                BorderLayout.WEST);

        headerPanel.add(
                countLabel,
                BorderLayout.EAST);

        configureTable();

        JScrollPane scrollPane = new JScrollPane(employeeTable);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT));

        JButton closeButton = new JButton("Đóng");

        closeButton.addActionListener(
                event -> dispose());

        buttonPanel.add(closeButton);

        rootPanel.add(
                headerPanel,
                BorderLayout.NORTH);

        rootPanel.add(
                scrollPane,
                BorderLayout.CENTER);

        rootPanel.add(
                buttonPanel,
                BorderLayout.SOUTH);

        setContentPane(rootPanel);

        setPreferredSize(
                new Dimension(
                        750,
                        480));

        pack();
        setLocationRelativeTo(getOwner());
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(
                new Object[] {
                        "ID",
                        "Tên đăng nhập",
                        "Họ và tên",
                        "Vai trò",
                        "Trạng thái"
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

    private void configureTable() {
        employeeTable.setRowHeight(32);

        employeeTable.setFillsViewportHeight(
                true);

        employeeTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        employeeTable.getTableHeader()
                .setReorderingAllowed(false);
    }

    private void loadEmployees() {
        try {
            List<BranchEmployee> employees = branchService.findEmployees(
                    branch.getId());

            tableModel.setRowCount(0);

            for (BranchEmployee employee : employees) {

                tableModel.addRow(
                        new Object[] {
                                employee.id(),
                                employee.username(),
                                employee.fullName(),

                                employee.roleName()
                                        .getDisplayName(),

                                employee.status()
                                        .getDisplayName()
                        });
            }

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}