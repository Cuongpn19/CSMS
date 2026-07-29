package com.csms.utils;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtils {

    private PasswordUtils() {
    }

    public static String hashPassword(
            String rawPassword) {
        if (rawPassword == null
                || rawPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "Mật khẩu không được để trống.");
        }

        return BCrypt.hashpw(
                rawPassword,
                BCrypt.gensalt(12));
    }

    public static boolean verifyPassword(
            String rawPassword,
            String passwordHash) {
        if (rawPassword == null
                || passwordHash == null) {
            return false;
        }

        return BCrypt.checkpw(
                rawPassword,
                passwordHash);
    }
}