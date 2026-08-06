package com.csms.view.admin.backup;

import com.csms.dto.BackupFileInfo;
import com.csms.service.BackupService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import com.csms.dto.RevenueReportRow;
import com.csms.service.RevenueReportService;

import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class BackupManagementPanel extends JPanel {

    private final BackupService backupService;

    private final DefaultTableModel tableModel;
    private final JTable backupTable;

    private final JButton createBackupButton;
    private final JButton restoreSelectedButton;
    private final JButton restoreExternalButton;
    private final JButton openFolderButton;
    private final JButton deleteButton;
    private final JButton refreshButton;

    private final JLabel backupCountLabel;
    private final JLabel statusLabel;
    private final JProgressBar progressBar;

    private final DateTimeFormatter dateTimeFormatter;

    private List<BackupFileInfo> displayedBackups;

    private boolean processing;

    public BackupManagementPanel() {
        backupService = new BackupService();

        tableModel = createTableModel();
        backupTable = new JTable(tableModel);

        createBackupButton = new JButton("Sao lưu toàn bộ");

        restoreSelectedButton = new JButton("Khôi phục bản đã chọn");

        restoreExternalButton = new JButton("Khôi phục từ file SQL");

        openFolderButton = new JButton("Mở thư mục");

        deleteButton = new JButton("Xóa");

        refreshButton = new JButton("Làm mới");

        backupCountLabel = new JLabel("0 bản sao lưu");

        statusLabel = new JLabel("Sẵn sàng");

        progressBar = new JProgressBar();

        dateTimeFormatter = DateTimeFormatter.ofPattern(
                "dd/MM/yyyy HH:mm:ss");

        displayedBackups = new ArrayList<>();

        processing = false;

        initializeComponents();
        registerEvents();
        loadBackupFiles();
    }

    private void initializeComponents() {
        setLayout(
                new BorderLayout(
                        0,
                        16));

        setBackground(
                new Color(
                        245,
                        247,
                        251));

        setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        22,
                        20,
                        22));

        add(
                createHeaderPanel(),
                BorderLayout.NORTH);

        add(
                createContentPanel(),
                BorderLayout.CENTER);

        add(
                createStatusPanel(),
                BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel wrapperPanel = new JPanel(
                new BorderLayout(
                        0,
                        15));

        wrapperPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(
                new BorderLayout());

        titlePanel.setOpaque(false);

        JPanel titleTextPanel = new JPanel(
                new BorderLayout(
                        0,
                        4));

        titleTextPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(
                "SAO LƯU VÀ KHÔI PHỤC DỮ LIỆU");

        titleLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        25));

        JLabel descriptionLabel = new JLabel(
                "Sao lưu toàn bộ dữ liệu bán hàng, kho, thực đơn, "
                        + "nhân sự và cấu hình hệ thống");

        descriptionLabel.setForeground(
                new Color(
                        105,
                        113,
                        128));

        titleTextPanel.add(
                titleLabel,
                BorderLayout.NORTH);

        titleTextPanel.add(
                descriptionLabel,
                BorderLayout.SOUTH);

        titlePanel.add(
                titleTextPanel,
                BorderLayout.WEST);

        titlePanel.add(
                backupCountLabel,
                BorderLayout.EAST);

        JPanel primaryActionPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        0));

        primaryActionPanel.setOpaque(false);

        createBackupButton.setPreferredSize(
                new Dimension(
                        170,
                        38));

        restoreExternalButton.setPreferredSize(
                new Dimension(
                        190,
                        38));

        primaryActionPanel.add(
                createBackupButton);

        primaryActionPanel.add(
                restoreExternalButton);

        primaryActionPanel.add(
                refreshButton);

        wrapperPanel.add(
                titlePanel,
                BorderLayout.NORTH);

        wrapperPanel.add(
                primaryActionPanel,
                BorderLayout.SOUTH);

        return wrapperPanel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(
                new BorderLayout(
                        0,
                        12));

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(
                                        226,
                                        230,
                                        237)),
                        BorderFactory.createEmptyBorder(
                                15,
                                15,
                                15,
                                15)));

        JLabel tableTitleLabel = new JLabel(
                "Danh sách bản sao lưu");

        tableTitleLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        17));

        configureTable();

        JScrollPane scrollPane = new JScrollPane(
                backupTable);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(
                                232,
                                235,
                                241)));

        JPanel actionPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        8,
                        0));

        actionPanel.setOpaque(false);

        actionPanel.add(
                openFolderButton);

        actionPanel.add(
                restoreSelectedButton);

        actionPanel.add(
                deleteButton);

        panel.add(
                tableTitleLabel,
                BorderLayout.NORTH);

        panel.add(
                scrollPane,
                BorderLayout.CENTER);

        panel.add(
                actionPanel,
                BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(
                new BorderLayout(
                        12,
                        0));

        panel.setOpaque(false);

        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        progressBar.setPreferredSize(
                new Dimension(
                        190,
                        18));

        statusLabel.setForeground(
                new Color(
                        90,
                        98,
                        112));

        panel.add(
                statusLabel,
                BorderLayout.WEST);

        panel.add(
                progressBar,
                BorderLayout.EAST);

        return panel;
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(
                new Object[] {
                        "STT",
                        "Tên file",
                        "Dung lượng",
                        "Thời gian tạo",
                        "Đường dẫn"
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
        backupTable.setRowHeight(34);

        backupTable.setFillsViewportHeight(
                true);

        backupTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        backupTable.setAutoCreateRowSorter(
                true);

        backupTable.getTableHeader()
                .setReorderingAllowed(false);

        backupTable.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(45);

        backupTable.getColumnModel()
                .getColumn(0)
                .setMaxWidth(60);

        backupTable.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(280);

        backupTable.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(100);

        backupTable.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(170);

        backupTable.getColumnModel()
                .getColumn(4)
                .setPreferredWidth(400);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();

        centerRenderer.setHorizontalAlignment(
                JLabel.CENTER);

        backupTable.getColumnModel()
                .getColumn(0)
                .setCellRenderer(
                        centerRenderer);

        backupTable.getColumnModel()
                .getColumn(2)
                .setCellRenderer(
                        centerRenderer);

        backupTable.getColumnModel()
                .getColumn(3)
                .setCellRenderer(
                        centerRenderer);
    }

    private void registerEvents() {
        createBackupButton.addActionListener(
                event -> createBackup());

        restoreSelectedButton.addActionListener(
                event -> restoreSelectedBackup());

        restoreExternalButton.addActionListener(
                event -> chooseExternalBackup());

        deleteButton.addActionListener(
                event -> deleteSelectedBackup());

        openFolderButton.addActionListener(
                event -> openBackupDirectory());

        refreshButton.addActionListener(
                event -> loadBackupFiles());

        backupTable.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent event) {
                        if (event.getClickCount() == 2
                                && backupTable
                                        .getSelectedRow() >= 0) {

                            restoreSelectedBackup();
                        }
                    }
                });
    }

    private void loadBackupFiles() {
        if (processing) {
            return;
        }

        setProcessing(
                true,
                "Đang tải danh sách bản sao lưu...");

        SwingWorker<List<BackupFileInfo>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<BackupFileInfo> doInBackground() {

                return backupService
                        .findBackupFiles();
            }

            @Override
            protected void done() {
                try {
                    displayedBackups = get();

                    displayBackupFiles();

                    statusLabel.setText(
                            "Đã tải "
                                    + displayedBackups.size()
                                    + " bản sao lưu.");

                } catch (InterruptedException exception) {
                    Thread.currentThread()
                            .interrupt();

                    showError(
                            "Quá trình tải danh sách đã bị gián đoạn.");

                } catch (ExecutionException exception) {
                    showWorkerError(
                            exception);

                } finally {
                    setProcessing(
                            false,
                            statusLabel.getText());
                }
            }
        };

        worker.execute();
    }

    private void displayBackupFiles() {
        tableModel.setRowCount(0);

        int index = 1;

        for (BackupFileInfo backup : displayedBackups) {

            tableModel.addRow(
                    new Object[] {
                            index++,
                            backup.fileName(),
                            formatFileSize(
                                    backup.fileSize()),
                            backup.createdAt()
                                    .format(
                                            dateTimeFormatter),
                            backup.filePath()
                                    .toString()
                    });
        }

        backupCountLabel.setText(
                displayedBackups.size()
                        + " bản sao lưu");
    }

    private void createBackup() {
        if (processing) {
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Tạo bản sao lưu toàn bộ database hiện tại?",
                "Xác nhận sao lưu",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        setProcessing(
                true,
                "Đang sao lưu toàn bộ database...");

        SwingWorker<Path, Void> worker = new SwingWorker<>() {

            @Override
            protected Path doInBackground() {
                return backupService
                        .createFullBackup();
            }

            @Override
            protected void done() {
                try {
                    Path createdFile = get();

                    statusLabel.setText(
                            "Sao lưu thành công: "
                                    + createdFile
                                            .getFileName());

                    JOptionPane.showMessageDialog(
                            BackupManagementPanel.this,
                            "Đã tạo bản sao lưu thành công.\n\n"
                                    + createdFile,
                            "Sao lưu thành công",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (InterruptedException exception) {
                    Thread.currentThread()
                            .interrupt();

                    showError(
                            "Quá trình sao lưu đã bị gián đoạn.");

                } catch (ExecutionException exception) {
                    showWorkerError(
                            exception);

                } finally {
                    setProcessing(
                            false,
                            statusLabel.getText());

                    loadBackupFiles();
                }
            }
        };

        worker.execute();
    }

    private void restoreSelectedBackup() {
        BackupFileInfo selectedBackup = getSelectedBackup();

        if (selectedBackup == null) {
            showWarning(
                    "Vui lòng chọn bản sao lưu cần khôi phục.");
            return;
        }

        confirmAndRestore(
                selectedBackup.filePath());
    }

    private void chooseExternalBackup() {
        if (processing) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setDialogTitle(
                "Chọn file SQL để khôi phục");

        fileChooser.setFileSelectionMode(
                JFileChooser.FILES_ONLY);

        fileChooser.setAcceptAllFileFilterUsed(
                false);

        fileChooser.setFileFilter(
                new FileNameExtensionFilter(
                        "SQL Backup (*.sql)",
                        "sql"));

        int result = fileChooser.showOpenDialog(
                this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path selectedFile = fileChooser
                .getSelectedFile()
                .toPath();

        try {
            backupService
                    .validateExternalBackupFile(
                            selectedFile);

            confirmAndRestore(
                    selectedFile);

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            showError(
                    exception.getMessage());
        }
    }

    private void confirmAndRestore(
            Path backupFile) {
        if (processing) {
            return;
        }

        int firstConfirmation = JOptionPane.showConfirmDialog(
                this,
                """
                        Khôi phục dữ liệu có thể thay thế toàn bộ dữ liệu hiện tại.

                        Hệ thống sẽ tự động tạo một bản sao lưu trước khi khôi phục.

                        File được chọn:
                        """
                        + backupFile
                        + "\n\nBạn có muốn tiếp tục?",
                "Cảnh báo khôi phục dữ liệu",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (firstConfirmation != JOptionPane.YES_OPTION) {
            return;
        }

        String confirmationInput = JOptionPane.showInputDialog(
                this,
                """
                        Đây là thao tác quan trọng.

                        Nhập KHOI PHUC để xác nhận:
                        """,
                "Xác nhận lần cuối",
                JOptionPane.WARNING_MESSAGE);

        if (confirmationInput == null) {
            return;
        }

        if (!"KHOI PHUC".equalsIgnoreCase(
                confirmationInput.trim())) {
            showWarning(
                    "Nội dung xác nhận không đúng. "
                            + "Quá trình khôi phục đã được hủy.");

            return;
        }

        restoreBackup(
                backupFile);
    }

    private void restoreBackup(
            Path backupFile) {
        setProcessing(
                true,
                "Đang tạo bản sao lưu an toàn và khôi phục dữ liệu...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() {
                backupService.restoreBackup(
                        backupFile);

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    statusLabel.setText(
                            "Khôi phục dữ liệu thành công.");

                    JOptionPane.showMessageDialog(
                            BackupManagementPanel.this,
                            """
                                    Khôi phục dữ liệu thành công.

                                    Hệ thống đã tạo một bản sao lưu trước khi khôi phục.

                                    Bạn nên đăng xuất và khởi động lại chương trình
                                    để toàn bộ dữ liệu được tải lại chính xác.
                                    """,
                            "Khôi phục thành công",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (InterruptedException exception) {
                    Thread.currentThread()
                            .interrupt();

                    showError(
                            "Quá trình khôi phục đã bị gián đoạn.");

                } catch (ExecutionException exception) {
                    showWorkerError(
                            exception);

                } finally {
                    setProcessing(
                            false,
                            statusLabel.getText());

                    loadBackupFiles();
                }
            }
        };

        worker.execute();
    }

    private void deleteSelectedBackup() {
        BackupFileInfo selectedBackup = getSelectedBackup();

        if (selectedBackup == null) {
            showWarning(
                    "Vui lòng chọn bản sao lưu cần xóa.");
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa file:\n\n"
                        + selectedBackup.fileName()
                        + "\n\nThao tác này không thể hoàn tác.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        setProcessing(
                true,
                "Đang xóa file sao lưu...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() {
                backupService.deleteBackup(
                        selectedBackup.filePath());

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    statusLabel.setText(
                            "Đã xóa file "
                                    + selectedBackup
                                            .fileName());

                    JOptionPane.showMessageDialog(
                            BackupManagementPanel.this,
                            "Xóa file sao lưu thành công.",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (InterruptedException exception) {
                    Thread.currentThread()
                            .interrupt();

                    showError(
                            "Quá trình xóa file đã bị gián đoạn.");

                } catch (ExecutionException exception) {
                    showWorkerError(
                            exception);

                } finally {
                    setProcessing(
                            false,
                            statusLabel.getText());

                    loadBackupFiles();
                }
            }
        };

        worker.execute();
    }

    private void openBackupDirectory() {
        try {
            backupService.openBackupDirectory();

            statusLabel.setText(
                    "Đã mở thư mục sao lưu.");

        } catch (
                IllegalArgumentException
                | IllegalStateException exception) {
            showError(
                    exception.getMessage());
        }
    }

    private BackupFileInfo getSelectedBackup() {
        int selectedViewRow = backupTable.getSelectedRow();

        if (selectedViewRow < 0) {
            return null;
        }

        int selectedModelRow = backupTable
                .convertRowIndexToModel(
                        selectedViewRow);

        if (selectedModelRow < 0
                || selectedModelRow >= displayedBackups.size()) {

            return null;
        }

        return displayedBackups.get(
                selectedModelRow);
    }

    private void setProcessing(
            boolean processing,
            String message) {
        this.processing = processing;

        createBackupButton.setEnabled(
                !processing);

        restoreSelectedButton.setEnabled(
                !processing);

        restoreExternalButton.setEnabled(
                !processing);

        deleteButton.setEnabled(
                !processing);

        openFolderButton.setEnabled(
                !processing);

        refreshButton.setEnabled(
                !processing);

        backupTable.setEnabled(
                !processing);

        progressBar.setVisible(
                processing);

        setCursor(
                processing
                        ? Cursor.getPredefinedCursor(
                                Cursor.WAIT_CURSOR)
                        : Cursor.getDefaultCursor());

        if (message != null
                && !message.isBlank()) {

            statusLabel.setText(message);
        }
    }

    private String formatFileSize(
            long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        double kilobytes = bytes / 1024.0;

        if (kilobytes < 1024) {
            return String.format(
                    "%.2f KB",
                    kilobytes);
        }

        double megabytes = kilobytes / 1024.0;

        if (megabytes < 1024) {
            return String.format(
                    "%.2f MB",
                    megabytes);
        }

        double gigabytes = megabytes / 1024.0;

        return String.format(
                "%.2f GB",
                gigabytes);
    }

    private void showWorkerError(
            ExecutionException exception) {
        Throwable cause = exception.getCause() == null
                ? exception
                : exception.getCause();

        showError(
                cause.getMessage() == null
                        ? "Đã xảy ra lỗi không xác định."
                        : cause.getMessage());
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
        statusLabel.setText(
                "Thao tác thất bại.");

        JOptionPane.showMessageDialog(
                this,
                message,
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
    }
}