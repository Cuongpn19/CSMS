package com.csms;

import com.csms.dto.BackupFileInfo;
import com.csms.service.BackupService;

import java.nio.file.Path;
import java.util.List;

public class BackupTest {

    public static void main(String[] args) {
        BackupService backupService = new BackupService();

        Path createdFile = backupService.createFullBackup();

        System.out.println(
                "Đã tạo backup: "
                        + createdFile);

        List<BackupFileInfo> files = backupService.findBackupFiles();

        for (BackupFileInfo file : files) {
            System.out.println(
                    file.fileName()
                            + " | "
                            + file.fileSize()
                            + " bytes");
        }
    }
}