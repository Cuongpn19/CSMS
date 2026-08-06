package com.csms.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.csms.config.BackupConfig;

public final class AutoBackupScheduler {

    private static final LocalTime BACKUP_TIME = BackupConfig.AUTO_BACKUP_TIME;

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(
                runnable,
                "csms-auto-backup");

        thread.setDaemon(true);

        return thread;
    });

    private static boolean started;

    private AutoBackupScheduler() {
    }

    public static synchronized void start() {
        if (started) {
            return;
        }

        started = true;

        long initialDelaySeconds = calculateInitialDelaySeconds();

        long oneDaySeconds = TimeUnit.DAYS.toSeconds(1);

        SCHEDULER.scheduleAtFixedRate(
                AutoBackupScheduler::runBackup,
                initialDelaySeconds,
                oneDaySeconds,
                TimeUnit.SECONDS);

        System.out.println(
                "Đã bật sao lưu tự động lúc "
                        + BACKUP_TIME
                        + " mỗi ngày.");
    }

    private static void runBackup() {
        try {
            BackupService backupService = new BackupService();

            var backupFile = backupService.createFullBackup();

            System.out.println(
                    "Sao lưu tự động thành công: "
                            + backupFile);

        } catch (Exception exception) {
            System.err.println(
                    "Sao lưu tự động thất bại: "
                            + exception.getMessage());

            exception.printStackTrace();
        }
    }

    private static long calculateInitialDelaySeconds() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime nextRun = now.toLocalDate()
                .atTime(BACKUP_TIME);

        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1);
        }

        return Duration.between(
                now,
                nextRun).getSeconds();
    }

    public static synchronized void stop() {
        if (!started) {
            return;
        }

        SCHEDULER.shutdown();

        started = false;
    }
}