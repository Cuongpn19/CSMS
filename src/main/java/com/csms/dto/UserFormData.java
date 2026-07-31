package com.csms.dto;

import com.csms.entity.RoleName;
import com.csms.entity.UserStatus;

public record UserFormData(
        String username,
        String password,
        String confirmPassword,
        String fullName,
        String email,
        String phone,
        RoleName roleName,
        UserStatus status,
        Integer branchId) {
}