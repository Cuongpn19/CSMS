package com.csms.utils;

import com.csms.entity.User;

public final class SessionManager {

    private static User currentUser;

    private SessionManager() {
    }

    public static void createSession(
            User user) {
        currentUser = user;
    }

    public static void login(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
    }

    public static void clearSession() {
        currentUser = null;
    }
}