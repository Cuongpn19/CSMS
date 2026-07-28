package com.csms.utils;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtils {

    private PasswordUtils() {
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(
                plainPassword,
                BCrypt.gensalt(12));
    }

    public static boolean verifyPassword(
            String plainPassword,
            String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(
                    plainPassword,
                    hashedPassword);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}