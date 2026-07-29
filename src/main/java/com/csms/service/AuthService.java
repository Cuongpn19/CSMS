package com.csms.service;

import com.csms.dao.UserDAO;
import com.csms.dto.LoginResult;
import com.csms.entity.User;
import com.csms.entity.UserStatus;
import com.csms.utils.PasswordUtils;

import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public LoginResult login(
            String username,
            String password) {
        String normalizedUsername = username == null ? "" : username.trim();

        if (normalizedUsername.isBlank()) {
            return LoginResult.failure(
                    "Vui lòng nhập tên đăng nhập.");
        }

        if (password == null || password.isBlank()) {
            return LoginResult.failure(
                    "Vui lòng nhập mật khẩu.");
        }

        Optional<User> optionalUser = userDAO.findByUsername(normalizedUsername);

        if (optionalUser.isEmpty()) {
            return LoginResult.failure(
                    "Tên đăng nhập hoặc mật khẩu không đúng.");
        }

        User user = optionalUser.get();

        if (user.getStatus() != UserStatus.ACTIVE) {
            return LoginResult.failure(
                    "Tài khoản hiện đang bị khóa.");
        }

        boolean passwordMatches = PasswordUtils.verifyPassword(
                password,
                user.getPasswordHash());

        if (!passwordMatches) {
            return LoginResult.failure(
                    "Tên đăng nhập hoặc mật khẩu không đúng.");
        }

        return LoginResult.success(user);
    }
}