package com.csms.view.admin.branch;

import com.csms.dto.BranchFormData;
import com.csms.entity.Branch;
import com.csms.entity.BranchStatus;
import com.csms.service.BranchService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class BranchDialog extends JDialog {

    private final BranchService branchService;
    private final Branch editingBranch;

    private final JTextField nameField;
    private final JTextField addressField;
    private final JTextField phoneField;

    private final JSpinner openingTimeSpinner;
    private final JSpinner closingTimeSpinner;

    private final JComboBox<BranchStatus> statusComboBox;

    private boolean saved;

    public BranchDialog(
            Window owner,
            Branch editingBranch) {
        super(
                owner,
                editingBranch == null
                        ? "Thêm chi nhánh"
                        : "Cập nhật chi nhánh",
                ModalityType.APPLICATION_MODAL);

        this.branchService = new BranchService();

        this.editingBranch = editingBranch;

        nameField = new JTextField();
        addressField = new JTextField();
        phoneField = new JTextField();

        openingTimeSpinner = createTimeSpinner(
                7,
                0);

        closingTimeSpinner = createTimeSpinner(
                22,
                0);

        statusComboBox = new JComboBox<>(
                BranchStatus.values());

        saved = false;

        initializeComponents();
        fillEditingData();
    }

    private void initializeComponents() {
        setDefaultCloseOperation(
                DISPOSE_ON_CLOSE);

        setResizable(false);

        JPanel rootPanel = new JPanel(
                new BorderLayout(
                        0,
                        18));

        rootPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        22,
                        24,
                        22,
                        24));

        JPanel formPanel = new JPanel(
                new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.insets = new Insets(
                8,
                8,
                8,
                8);

        constraints.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        addFormRow(
                formPanel,
                constraints,
                row++,
                "Tên chi nhánh:",
                nameField);

        addFormRow(
                formPanel,
                constraints,
                row++,
                "Địa chỉ:",
                addressField);

        addFormRow(
                formPanel,
                constraints,
                row++,
                "Số điện thoại:",
                phoneField);

        addFormRow(
                formPanel,
                constraints,
                row++,
                "Giờ mở cửa:",
                openingTimeSpinner);

        addFormRow(
                formPanel,
                constraints,
                row++,
                "Giờ đóng cửa:",
                closingTimeSpinner);

        addFormRow(
                formPanel,
                constraints,
                row,
                "Trạng thái:",
                statusComboBox);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT));

        JButton cancelButton = new JButton("Hủy");

        JButton saveButton = new JButton(
                editingBranch == null
                        ? "Thêm chi nhánh"
                        : "Lưu thay đổi");

        cancelButton.addActionListener(
                event -> dispose());

        saveButton.addActionListener(
                event -> saveBranch());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        rootPanel.add(
                formPanel,
                BorderLayout.CENTER);

        rootPanel.add(
                buttonPanel,
                BorderLayout.SOUTH);

        setContentPane(rootPanel);

        setPreferredSize(
                new Dimension(
                        600,
                        480));

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String label,
            java.awt.Component component) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0.30;

        panel.add(
                new JLabel(label),
                constraints);

        constraints.gridx = 1;
        constraints.weightx = 0.70;

        component.setPreferredSize(
                new Dimension(
                        330,
                        36));

        panel.add(
                component,
                constraints);
    }

    private JSpinner createTimeSpinner(
            int hour,
            int minute) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(
                Calendar.HOUR_OF_DAY,
                hour);

        calendar.set(
                Calendar.MINUTE,
                minute);

        calendar.set(
                Calendar.SECOND,
                0);

        SpinnerDateModel model = new SpinnerDateModel(
                calendar.getTime(),
                null,
                null,
                Calendar.MINUTE);

        JSpinner spinner = new JSpinner(model);

        spinner.setEditor(
                new JSpinner.DateEditor(
                        spinner,
                        "HH:mm"));

        return spinner;
    }

    private void fillEditingData() {
        statusComboBox.setSelectedItem(
                BranchStatus.ACTIVE);

        if (editingBranch == null) {
            return;
        }

        nameField.setText(
                editingBranch.getName());

        addressField.setText(
                editingBranch.getAddress());

        phoneField.setText(
                editingBranch.getPhone() == null
                        ? ""
                        : editingBranch.getPhone());

        if (editingBranch.getOpeningTime() != null) {

            openingTimeSpinner.setValue(
                    convertLocalTimeToDate(
                            editingBranch
                                    .getOpeningTime()));
        }

        if (editingBranch.getClosingTime() != null) {

            closingTimeSpinner.setValue(
                    convertLocalTimeToDate(
                            editingBranch
                                    .getClosingTime()));
        }

        statusComboBox.setSelectedItem(
                editingBranch.getStatus());
    }

    private void saveBranch() {
        BranchFormData formData = new BranchFormData(
                nameField
                        .getText()
                        .trim(),

                addressField
                        .getText()
                        .trim(),

                phoneField
                        .getText()
                        .trim(),

                convertDateToLocalTime(
                        (Date) openingTimeSpinner
                                .getValue()),

                convertDateToLocalTime(
                        (Date) closingTimeSpinner
                                .getValue()),

                (BranchStatus) statusComboBox
                        .getSelectedItem());

        try {
            if (editingBranch == null) {
                branchService.create(
                        formData);

                JOptionPane.showMessageDialog(
                        this,
                        "Thêm chi nhánh thành công.",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

            } else {
                branchService.update(
                        editingBranch.getId(),
                        formData);

                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật chi nhánh thành công.",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            saved = true;
            dispose();

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Không thể lưu chi nhánh",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private LocalTime convertDateToLocalTime(
            Date date) {
        return date.toInstant()
                .atZone(
                        ZoneId.systemDefault())
                .toLocalTime()
                .withSecond(0)
                .withNano(0);
    }

    private Date convertLocalTimeToDate(
            LocalTime localTime) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(
                Calendar.HOUR_OF_DAY,
                localTime.getHour());

        calendar.set(
                Calendar.MINUTE,
                localTime.getMinute());

        calendar.set(
                Calendar.SECOND,
                0);

        calendar.set(
                Calendar.MILLISECOND,
                0);

        return calendar.getTime();
    }

    public boolean isSaved() {
        return saved;
    }
}