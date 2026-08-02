package com.csms.service;

import com.csms.dao.BranchDAO;
import com.csms.dto.BranchEmployee;
import com.csms.dto.BranchFormData;
import com.csms.entity.Branch;
import com.csms.entity.BranchStatus;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class BranchService {

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(0|\\+84)[0-9]{9,10}$");

    private final BranchDAO branchDAO;

    public BranchService() {
        branchDAO = new BranchDAO();
    }

    public List<Branch> search(
            String keyword,
            BranchStatus status) {
        return branchDAO.search(
                keyword,
                status);
    }

    public List<Branch> findAllActive() {
        return branchDAO.findAllActive();
    }

    public Optional<Branch> findById(
            int branchId) {
        return branchDAO.findById(branchId);
    }

    public Branch create(
            BranchFormData formData) {
        validate(
                formData,
                0);

        Branch branch = new Branch();

        mapFormData(
                branch,
                formData);

        int branchId = branchDAO.insert(branch);

        branch.setId(branchId);

        return branch;
    }

    public void update(
            int branchId,
            BranchFormData formData) {
        Branch branch = branchDAO.findById(branchId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy chi nhánh."));

        validate(
                formData,
                branchId);

        mapFormData(
                branch,
                formData);

        branchDAO.update(branch);
    }

    public void toggleStatus(
            int branchId) {
        Branch branch = branchDAO.findById(branchId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy chi nhánh."));

        BranchStatus newStatus = branch.getStatus() == BranchStatus.ACTIVE
                ? BranchStatus.INACTIVE
                : BranchStatus.ACTIVE;

        branchDAO.updateStatus(
                branchId,
                newStatus);
    }

    public void delete(
            int branchId) {
        Branch branch = branchDAO.findById(branchId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy chi nhánh."));

        int employeeCount = branchDAO.countEmployees(branchId);

        if (employeeCount > 0) {
            throw new IllegalStateException(
                    "Không thể xóa chi nhánh \""
                            + branch.getName()
                            + "\" vì đang có "
                            + employeeCount
                            + " nhân viên. "
                            + "Vui lòng chuyển nhân viên sang "
                            + "chi nhánh khác trước.");
        }

        branchDAO.delete(branchId);
    }

    public List<BranchEmployee> findEmployees(
            int branchId) {
        branchDAO.findById(branchId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy chi nhánh."));

        return branchDAO.findEmployees(
                branchId);
    }

    private void validate(
            BranchFormData formData,
            int excludedBranchId) {
        if (formData == null) {
            throw new IllegalArgumentException(
                    "Thông tin chi nhánh không hợp lệ.");
        }

        String name = formData.name() == null
                ? ""
                : formData.name().trim();

        if (name.length() < 2) {
            throw new IllegalArgumentException(
                    "Tên chi nhánh phải có ít nhất 2 ký tự.");
        }

        if (name.length() > 150) {
            throw new IllegalArgumentException(
                    "Tên chi nhánh không được vượt quá 150 ký tự.");
        }

        if (branchDAO.nameExists(
                name,
                excludedBranchId)) {
            throw new IllegalArgumentException(
                    "Tên chi nhánh đã tồn tại.");
        }

        String address = formData.address() == null
                ? ""
                : formData.address().trim();

        if (address.length() < 5) {
            throw new IllegalArgumentException(
                    "Địa chỉ phải có ít nhất 5 ký tự.");
        }

        if (address.length() > 255) {
            throw new IllegalArgumentException(
                    "Địa chỉ không được vượt quá 255 ký tự.");
        }

        String phone = formData.phone() == null
                ? ""
                : formData.phone().trim();

        if (!phone.isBlank()
                && !PHONE_PATTERN
                        .matcher(phone)
                        .matches()) {

            throw new IllegalArgumentException(
                    "Số điện thoại không hợp lệ.");
        }

        LocalTime openingTime = formData.openingTime();

        LocalTime closingTime = formData.closingTime();

        if (openingTime == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn giờ mở cửa.");
        }

        if (closingTime == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn giờ đóng cửa.");
        }

        if (!closingTime.isAfter(openingTime)) {
            throw new IllegalArgumentException(
                    "Giờ đóng cửa phải sau giờ mở cửa.");
        }

        if (formData.status() == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn trạng thái.");
        }
    }

    private void mapFormData(
            Branch branch,
            BranchFormData formData) {
        branch.setName(
                formData.name().trim());

        branch.setAddress(
                formData.address().trim());

        branch.setPhone(
                normalizeOptional(
                        formData.phone()));

        branch.setOpeningTime(
                formData.openingTime());

        branch.setClosingTime(
                formData.closingTime());

        branch.setStatus(
                formData.status());
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