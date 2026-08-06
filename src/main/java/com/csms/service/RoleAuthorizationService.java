package com.csms.service;

import com.csms.entity.RoleName;
import com.csms.entity.User;
import com.csms.utils.SessionManager;

public class RoleAuthorizationService {

    public User requireAuthenticatedUser() {
        User currentUser = SessionManager
                .getCurrentUser();

        if (currentUser == null) {
            throw new SecurityException(
                    "Phiên đăng nhập không hợp lệ.");
        }

        return currentUser;
    }

    public User requireRole(
            RoleName requiredRole) {
        if (requiredRole == null) {
            throw new IllegalArgumentException(
                    "Role yêu cầu không hợp lệ.");
        }

        User currentUser = requireAuthenticatedUser();

        RoleName currentRole = currentUser.getRoleName();

        if (currentRole != requiredRole) {

            throw new SecurityException(
                    "Bạn không có quyền thực hiện thao tác này.");
        }

        return currentUser;
    }

    public User requireAnyRole(
            RoleName... allowedRoles) {
        User currentUser = requireAuthenticatedUser();

        if (allowedRoles == null
                || allowedRoles.length == 0) {

            throw new IllegalArgumentException(
                    "Danh sách role được phép đang trống.");
        }

        RoleName currentRole = currentUser.getRoleName();

        for (RoleName allowedRole : allowedRoles) {

            if (currentRole == allowedRole) {

                return currentUser;
            }
        }

        throw new SecurityException(
                "Bạn không có quyền thực hiện thao tác này.");
    }
}