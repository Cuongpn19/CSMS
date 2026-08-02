package com.csms.config;

import java.nio.file.Path;

public final class BackupConfig {

    private BackupConfig() {
    }

    public static final String DATABASE_HOST = "127.0.0.1";

    public static final String DATABASE_PORT = "3306";

    public static final String DATABASE_NAME = "csms";

    public static final String DATABASE_USER = "root";

    public static final String DATABASE_PASSWORD = System.getenv()
            .getOrDefault(
                    "CSMS_DB_PASSWORD",
                    "");

    public static final Path MYSQL_DUMP_PATH = Path.of(
            "C:",
            "xampp",
            "mysql",
            "bin",
            "mysqldump.exe");

    public static final Path MYSQL_PATH = Path.of(
            "C:",
            "xampp",
            "mysql",
            "bin",
            "mysql.exe");

    public static final Path BACKUP_DIRECTORY = Path.of("backups");
}