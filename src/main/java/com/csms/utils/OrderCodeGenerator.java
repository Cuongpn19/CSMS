package com.csms.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public final class OrderCodeGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(
            "yyyyMMddHHmmss");

    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    private OrderCodeGenerator() {
    }

    public static String generate() {
        int suffix = SEQUENCE.updateAndGet(
                value -> value >= 999
                        ? 1
                        : value + 1);

        return "ORD-"
                + LocalDateTime.now().format(FORMATTER)
                + "-"
                + String.format("%03d", suffix);
    }
}