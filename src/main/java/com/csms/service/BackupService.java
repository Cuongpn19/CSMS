package com.csms.service;

import com.csms.config.BackupConfig;
import com.csms.dto.BackupFileInfo;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class BackupService {

    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyyMMdd_HHmmss");

    private static final int SQL_PREVIEW_MAX_BYTES = 16_384;

    public BackupService() {
        createBackupDirectory();
    }

    /**
     * Tạo một bản sao lưu toàn bộ database.
     */
    public Path createFullBackup() {
        String fileName = "csms_backup_"
                + LocalDateTime.now()
                        .format(FILE_NAME_FORMATTER)
                + ".sql";

        return createBackup(fileName);
    }

    /**
     * Tạo backup tự động trước khi restore.
     */
    public Path createBeforeRestoreBackup() {
        String fileName = "csms_before_restore_"
                + LocalDateTime.now()
                        .format(FILE_NAME_FORMATTER)
                + ".sql";

        return createBackup(fileName);
    }

    /**
     * Thực hiện khôi phục database từ file SQL.
     */
    public void restoreBackup(
            Path backupFile) {
        validateExecutable(
                BackupConfig.MYSQL_PATH,
                "mysql.exe");

        validateBackupFile(backupFile);

        /*
         * Tự sao lưu dữ liệu hiện tại trước khi khôi phục.
         */
        createBeforeRestoreBackup();

        List<String> command = buildRestoreCommand();

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.redirectInput(
                backupFile.toFile());

        processBuilder.redirectError(
                ProcessBuilder.Redirect.PIPE);

        processBuilder.redirectOutput(
                ProcessBuilder.Redirect.PIPE);

        try {
            Process process = processBuilder.start();

            /*
             * Đọc stderr trước khi kiểm tra exit code.
             */
            String errorOutput = readProcessStream(
                    process.getErrorStream());

            String standardOutput = readProcessStream(
                    process.getInputStream());

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IllegalStateException(
                        buildProcessErrorMessage(
                                "Khôi phục dữ liệu thất bại",
                                exitCode,
                                errorOutput,
                                standardOutput));
            }

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Không thể chạy công cụ khôi phục: "
                            + exception.getMessage(),
                    exception);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Quá trình khôi phục đã bị gián đoạn.",
                    exception);
        }
    }

    /**
     * Lấy danh sách các file backup trong thư mục hệ thống.
     */
    public List<BackupFileInfo> findBackupFiles() {
        createBackupDirectory();

        try (
                Stream<Path> stream = Files.list(
                        BackupConfig.BACKUP_DIRECTORY)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(this::isSqlFile)
                    .map(this::mapBackupFileInfo)
                    .sorted(
                            Comparator.comparing(
                                    BackupFileInfo::createdAt).reversed())
                    .toList();

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Không thể tải danh sách file sao lưu: "
                            + exception.getMessage(),
                    exception);
        }
    }

    /**
     * Xóa một file backup trong thư mục backups.
     */
    public void deleteBackup(
            Path backupFile) {
        validateBackupFile(backupFile);
        validateFileInsideBackupDirectory(
                backupFile);

        try {
            Files.delete(backupFile);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Không thể xóa file sao lưu: "
                            + exception.getMessage(),
                    exception);
        }
    }

    /**
     * Mở thư mục backup bằng File Explorer.
     */
    public void openBackupDirectory() {
        createBackupDirectory();

        if (!Desktop.isDesktopSupported()) {
            throw new IllegalStateException(
                    "Hệ điều hành không hỗ trợ mở thư mục.");
        }

        try {
            Desktop.getDesktop().open(
                    BackupConfig.BACKUP_DIRECTORY
                            .toAbsolutePath()
                            .normalize()
                            .toFile());

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Không thể mở thư mục backup: "
                            + exception.getMessage(),
                    exception);
        }
    }

    /**
     * Cho phép kiểm tra file SQL được chọn từ bên ngoài.
     */
    public void validateExternalBackupFile(
            Path backupFile) {
        validateBackupFile(backupFile);
    }

    /**
     * Tạo file backup bằng mysqldump.
     */
    private Path createBackup(
            String fileName) {
        validateExecutable(
                BackupConfig.MYSQL_DUMP_PATH,
                "mysqldump.exe");

        createBackupDirectory();

        Path backupFile = BackupConfig.BACKUP_DIRECTORY
                .resolve(fileName)
                .toAbsolutePath()
                .normalize();

        List<String> command = buildBackupCommand();

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.redirectOutput(
                backupFile.toFile());

        processBuilder.redirectError(
                ProcessBuilder.Redirect.PIPE);

        try {
            Process process = processBuilder.start();

            String errorOutput = readProcessStream(
                    process.getErrorStream());

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                Files.deleteIfExists(
                        backupFile);

                throw new IllegalStateException(
                        buildProcessErrorMessage(
                                "Sao lưu dữ liệu thất bại",
                                exitCode,
                                errorOutput,
                                null));
            }

            validateCreatedBackupFile(
                    backupFile);

            return backupFile;

        } catch (IOException exception) {
            deleteQuietly(backupFile);

            throw new IllegalStateException(
                    "Không thể chạy công cụ sao lưu: "
                            + exception.getMessage(),
                    exception);

        } catch (InterruptedException exception) {
            deleteQuietly(backupFile);

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Quá trình sao lưu đã bị gián đoạn.",
                    exception);
        }
    }

    /**
     * Lệnh mysqldump.
     */
    private List<String> buildBackupCommand() {
        List<String> command = new ArrayList<>();

        command.add(
                BackupConfig.MYSQL_DUMP_PATH
                        .toString());

        command.add(
                "--host="
                        + BackupConfig.DATABASE_HOST);

        command.add(
                "--port="
                        + BackupConfig.DATABASE_PORT);

        command.add(
                "--user="
                        + BackupConfig.DATABASE_USER);

        if (BackupConfig.DATABASE_PASSWORD != null
                && !BackupConfig.DATABASE_PASSWORD
                        .isBlank()) {

            command.add(
                    "--password="
                            + BackupConfig.DATABASE_PASSWORD);
        }

        command.add(
                "--default-character-set=utf8mb4");

        command.add(
                "--single-transaction");

        command.add(
                "--routines");

        command.add(
                "--triggers");

        command.add(
                "--events");

        command.add(
                "--add-drop-table");

        command.add(
                "--skip-lock-tables");

        command.add(
                BackupConfig.DATABASE_NAME);

        return command;
    }

    /**
     * Lệnh mysql dùng để restore.
     */
    private List<String> buildRestoreCommand() {
        List<String> command = new ArrayList<>();

        command.add(
                BackupConfig.MYSQL_PATH
                        .toString());

        command.add(
                "--host="
                        + BackupConfig.DATABASE_HOST);

        command.add(
                "--port="
                        + BackupConfig.DATABASE_PORT);

        command.add(
                "--user="
                        + BackupConfig.DATABASE_USER);

        if (BackupConfig.DATABASE_PASSWORD != null
                && !BackupConfig.DATABASE_PASSWORD
                        .isBlank()) {

            command.add(
                    "--password="
                            + BackupConfig.DATABASE_PASSWORD);
        }

        command.add(
                "--default-character-set=utf8mb4");

        command.add(
                BackupConfig.DATABASE_NAME);

        return command;
    }

    private void validateExecutable(
            Path executable,
            String executableName) {
        if (executable == null) {
            throw new IllegalStateException(
                    "Chưa cấu hình đường dẫn "
                            + executableName
                            + ".");
        }

        if (!Files.exists(executable)) {
            throw new IllegalStateException(
                    "Không tìm thấy "
                            + executableName
                            + " tại:\n"
                            + executable);
        }

        if (!Files.isRegularFile(executable)) {
            throw new IllegalStateException(
                    "Đường dẫn "
                            + executableName
                            + " không phải là file hợp lệ:\n"
                            + executable);
        }
    }

    private void validateBackupFile(
            Path backupFile) {
        if (backupFile == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn file sao lưu.");
        }

        Path normalizedPath = backupFile
                .toAbsolutePath()
                .normalize();

        if (!Files.exists(normalizedPath)) {
            throw new IllegalArgumentException(
                    "File sao lưu không tồn tại.");
        }

        if (!Files.isRegularFile(
                normalizedPath)) {
            throw new IllegalArgumentException(
                    "Đường dẫn đã chọn không phải là file.");
        }

        if (!isSqlFile(normalizedPath)) {
            throw new IllegalArgumentException(
                    "File sao lưu phải có định dạng .sql.");
        }

        try {
            if (Files.size(normalizedPath) == 0) {
                throw new IllegalArgumentException(
                        "File sao lưu không có dữ liệu.");
            }

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Không thể kiểm tra dung lượng file sao lưu.",
                    exception);
        }

        validateSqlContent(
                normalizedPath);
    }

    /**
     * Kiểm tra sơ bộ nội dung file SQL.
     */
    private void validateSqlContent(
            Path backupFile) {
        try {
            byte[] fileBytes = Files.readAllBytes(
                    backupFile);

            int previewLength = Math.min(
                    fileBytes.length,
                    SQL_PREVIEW_MAX_BYTES);

            String preview = new String(
                    fileBytes,
                    0,
                    previewLength,
                    StandardCharsets.UTF_8)
                    .toLowerCase(
                            Locale.ROOT);

            boolean looksLikeSql = preview.contains(
                    "mysql dump")
                    || preview.contains(
                            "mariadb dump")
                    || preview.contains(
                            "create table")
                    || preview.contains(
                            "insert into")
                    || preview.contains(
                            "drop table")
                    || preview.contains(
                            "set ");

            if (!looksLikeSql) {
                throw new IllegalArgumentException(
                        "Nội dung file không giống một file sao lưu SQL hợp lệ.");
            }

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Không thể đọc file sao lưu: "
                            + exception.getMessage(),
                    exception);
        }
    }

    private void validateCreatedBackupFile(
            Path backupFile) throws IOException {

        if (!Files.exists(backupFile)) {
            throw new IllegalStateException(
                    "Không tìm thấy file sao lưu sau khi tạo.");
        }

        if (!Files.isRegularFile(
                backupFile)) {
            throw new IllegalStateException(
                    "Đường dẫn backup không phải là file.");
        }

        if (Files.size(backupFile) == 0) {
            Files.deleteIfExists(
                    backupFile);

            throw new IllegalStateException(
                    "File sao lưu được tạo nhưng không có dữ liệu.");
        }
    }

    private void validateFileInsideBackupDirectory(
            Path backupFile) {
        Path backupDirectory = BackupConfig.BACKUP_DIRECTORY
                .toAbsolutePath()
                .normalize();

        Path normalizedFile = backupFile
                .toAbsolutePath()
                .normalize();

        if (!normalizedFile.startsWith(
                backupDirectory)) {
            throw new SecurityException(
                    "Không được phép xóa file ngoài thư mục backup.");
        }
    }

    private BackupFileInfo mapBackupFileInfo(
            Path file) {
        try {
            LocalDateTime createdAt = Files.getLastModifiedTime(
                    file)
                    .toInstant()
                    .atZone(
                            ZoneId.systemDefault())
                    .toLocalDateTime();

            return new BackupFileInfo(
                    file
                            .getFileName()
                            .toString(),

                    file
                            .toAbsolutePath()
                            .normalize(),

                    Files.size(file),

                    createdAt);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Không thể đọc thông tin file "
                            + file.getFileName()
                            + ": "
                            + exception.getMessage(),
                    exception);
        }
    }

    private boolean isSqlFile(
            Path path) {
        if (path == null
                || path.getFileName() == null) {

            return false;
        }

        return path
                .getFileName()
                .toString()
                .toLowerCase(
                        Locale.ROOT)
                .endsWith(".sql");
    }

    private void createBackupDirectory() {
        try {
            Files.createDirectories(
                    BackupConfig.BACKUP_DIRECTORY);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Không thể tạo thư mục backup tại: "
                            + BackupConfig.BACKUP_DIRECTORY
                                    .toAbsolutePath(),
                    exception);
        }
    }

    private String readProcessStream(
            java.io.InputStream inputStream) throws IOException {

        if (inputStream == null) {
            return "";
        }

        return new String(
                inputStream.readAllBytes(),
                StandardCharsets.UTF_8).trim();
    }

    private String buildProcessErrorMessage(
            String title,
            int exitCode,
            String errorOutput,
            String standardOutput) {
        StringBuilder message = new StringBuilder();

        message.append(title)
                .append(". Mã lỗi: ")
                .append(exitCode)
                .append(".");

        if (errorOutput != null
                && !errorOutput.isBlank()) {

            message.append("\n")
                    .append(errorOutput.trim());
        }

        if (standardOutput != null
                && !standardOutput.isBlank()) {

            message.append("\n")
                    .append(standardOutput.trim());
        }

        return message.toString();
    }

    private void deleteQuietly(
            Path path) {
        if (path == null) {
            return;
        }

        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}