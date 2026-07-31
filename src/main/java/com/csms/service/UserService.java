package com.csms.service;

import com.csms.dao.UserDAO;
import com.csms.dto.UserFormData;
import com.csms.entity.RoleName;
import com.csms.entity.User;
import com.csms.entity.UserStatus;
import com.csms.utils.PasswordUtils;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._]{4,30}$");

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(0|\\+84)[0-9]{9,10}$");

    private final UserDAO userDAO;

    public UserService() {
        userDAO = new UserDAO();
    }

    public List<User> findAll() {
        return userDAO.findAll();
    }

    public List<User> search(
            String keyword,
            RoleName roleName,
            UserStatus status) {
        return userDAO.search(
                keyword,
                roleName,
                status);
    }

    public Optional<User> findById(
            int userId) {
        return userDAO.findById(userId);
    }

    public User createUser(
            UserFormData formData) {
        validateForm(
                formData,
                true,
                0);

        User user = new User();

        user.setUsername(
                normalizeUsername(
                        formData.username()));

        user.setPasswordHash(
                PasswordUtils.hashPassword(
                        formData.password()));

        user.setFullName(
                formData.fullName().trim());

        user.setEmail(
                normalizeOptional(
                        formData.email()));

        user.setPhone(
                normalizeOptional(
                        formData.phone()));

        user.setRoleName(
                formData.roleName());

        user.setStatus(
                formData.status() == null
                        ? UserStatus.ACTIVE
                        : formData.status());

        user.setBranchId(
                formData.branchId());

        int userId = userDAO.insert(user);

        user.setId(userId);

        return user;
    }

    public void updateUser(
            int userId,
            UserFormData formData,
            int currentLoggedInUserId) {
        User existingUser = userDAO.findById(userId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy tài khoản."));

        validateForm(
                formData,
                false,
                userId);

        /*
         * Không cho Admin đang đăng nhập tự khóa
         * hoặc tự ngừng tài khoản của chính mình.
         */
        if (userId == currentLoggedInUserId
                && formData.status() != UserStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Bạn không thể tự khóa hoặc ngừng tài khoản đang đăng nhập.");
        }

        existingUser.setUsername(
                normalizeUsername(
                        formData.username()));

        existingUser.setFullName(
                formData.fullName().trim());

        existingUser.setEmail(
                normalizeOptional(
                        formData.email()));

        existingUser.setPhone(
                normalizeOptional(
                        formData.phone()));

        existingUser.setRoleName(
                formData.roleName());

        existingUser.setStatus(
                formData.status());

        existingUser.setBranchId(
                formData.branchId());

        userDAO.update(existingUser);
    }

    // public void toggleLock(
    // int userId,
    // int currentLoggedInUserId) {
    // User user = userDAO.findById(userId)
    // .orElseThrow(
    // () -> new IllegalArgumentException(
    // "Không tìm thấy tài khoản."));

    // if (userId == currentLoggedInUserId) {
    // throw new IllegalArgumentException(
    // "Bạn không thể tự khóa tài khoản đang đăng nhập.");
    // }

    // UserStatus newStatus = user.getStatus() == UserStatus.INACTIVE
    // ? UserStatus.ACTIVE
    // : UserStatus.INACTIVE;

    // userDAO.updateStatus(
    // userId,
    // newStatus);
    // }

    public void toggleStatus(
            int userId,
            int currentLoggedInUserId) {
        User user = userDAO.findById(userId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy tài khoản."));

        if (userId == currentLoggedInUserId) {
            throw new IllegalArgumentException(
                    "Bạn không thể tự vô hiệu hóa tài khoản đang đăng nhập.");
        }

        UserStatus newStatus = user.getStatus() == UserStatus.ACTIVE
                ? UserStatus.INACTIVE
                : UserStatus.ACTIVE;

        userDAO.updateStatus(
                userId,
                newStatus);
    }

    public void resetPassword(
            int userId,
            String newPassword,
            String confirmPassword) {
        userDAO.findById(userId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy tài khoản."));

        validatePassword(
                newPassword,
                confirmPassword);

        String passwordHash = PasswordUtils.hashPassword(
                newPassword);

        userDAO.resetPassword(
                userId,
                passwordHash);
    }

    private void validateForm(
            UserFormData formData,
            boolean creating,
            int excludedUserId) {
        if (formData == null) {
            throw new IllegalArgumentException(
                    "Thông tin tài khoản không hợp lệ.");
        }

        String username = normalizeUsername(
                formData.username());

        if (!USERNAME_PATTERN
                .matcher(username)
                .matches()) {

            throw new IllegalArgumentException(
                    "Tên đăng nhập phải dài từ 4 đến 30 ký tự "
                            + "và chỉ chứa chữ, số, dấu chấm hoặc gạch dưới.");
        }

        if (userDAO.usernameExists(
                username,
                excludedUserId)) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập đã tồn tại.");
        }

        if (formData.fullName() == null
                || formData.fullName()
                        .trim()
                        .length() < 2) {

            throw new IllegalArgumentException(
                    "Họ tên phải có ít nhất 2 ký tự.");
        }

        String email = normalizeOptional(
                formData.email());

        if (email != null
                && !EMAIL_PATTERN
                        .matcher(email)
                        .matches()) {

            throw new IllegalArgumentException(
                    "Địa chỉ email không hợp lệ.");
        }

        if (email != null
                && userDAO.emailExists(
                        email,
                        excludedUserId)) {

            throw new IllegalArgumentException(
                    "Email đã được sử dụng.");
        }

        String phone = normalizeOptional(
                formData.phone());

        if (phone != null
                && !PHONE_PATTERN
                        .matcher(phone)
                        .matches()) {

            throw new IllegalArgumentException(
                    "Số điện thoại không hợp lệ.");
        }

        if (formData.roleName() == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn vai trò.");
        }

        if (formData.status() == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn trạng thái.");
        }

        if (formData.roleName() != RoleName.ADMIN
                && formData.branchId() == null) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn chi nhánh cho nhân viên.");
        }

        if (creating) {
            validatePassword(
                    formData.password(),
                    formData.confirmPassword());
        }
    }

    private void validatePassword(
            String password,
            String confirmPassword) {
        if (password == null
                || password.length() < 6) {

            throw new IllegalArgumentException(
                    "Mật khẩu phải có ít nhất 6 ký tự.");
        }

        if (password.length() > 72) {
            throw new IllegalArgumentException(
                    "Mật khẩu không được dài quá 72 ký tự.");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException(
                    "Mật khẩu xác nhận không khớp.");
        }
    }

    private String normalizeUsername(
            String username) {
        return username == null
                ? ""
                : username.trim().toLowerCase();
    }

    private String normalizeOptional(
            String value) {
        if (value == null
                || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}