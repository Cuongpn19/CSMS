package com.csms.dto;

import com.csms.entity.RoleName;
import com.csms.entity.UserStatus;

public record BranchEmployee(
        int id,
        String username,
        String fullName,
        RoleName roleName,
        UserStatus status) {
}