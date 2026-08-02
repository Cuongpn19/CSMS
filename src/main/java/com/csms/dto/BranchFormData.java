package com.csms.dto;

import com.csms.entity.BranchStatus;

import java.time.LocalTime;

public record BranchFormData(
        String name,
        String address,
        String phone,
        LocalTime openingTime,
        LocalTime closingTime,
        BranchStatus status) {
}