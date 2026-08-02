package com.csms.dto;

import java.nio.file.Path;
import java.time.LocalDateTime;

public record BackupFileInfo(
        String fileName,
        Path filePath,
        long fileSize,
        LocalDateTime createdAt) {
}