package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.IStaffService;
import com.example.bookingtour.dtos.request.admin.StaffCreateRequest;
import com.example.bookingtour.dtos.request.admin.StaffUpdateRequest;
import com.example.bookingtour.dtos.response.ApiResponse; // Sếp check lại đúng package ApiResponse của dự án nhé
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/staffs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StaffController {

    private final IStaffService staffService;

    // 1. Tạo nhân viên mới
    @PostMapping
    public ApiResponse<?> createStaff(@Valid @RequestBody StaffCreateRequest request) {
        return ApiResponse.builder()
                .code(200)
                .result(staffService.createStaff(request))
                .build();
    }

    // 2. Lấy toàn bộ danh sách nhân viên
    @GetMapping
    public ApiResponse<?> getAllStaffs() {
        return ApiResponse.builder()
                .code(200)
                .result(staffService.getAllStaffs())
                .build();
    }

    // 3. Lấy chi tiết 1 nhân viên theo Mã nhân viên (employeeCode)
    @GetMapping("/{code}")
    public ApiResponse<?> getStaffByCode(@PathVariable("code") String employeeCode) {
        return ApiResponse.builder()
                .code(200)
                .result(staffService.getStaffByCode(employeeCode))
                .build();
    }

    // 4. Cập nhật nhân viên theo userId
    @PutMapping("/{id}")
    public ApiResponse<?> updateStaff(
            @PathVariable("id") Integer userId,
            @Valid @RequestBody StaffUpdateRequest request) {
        return ApiResponse.builder()
                .code(200)
                .result(staffService.updateStaff(userId, request))
                .build();
    }

    @PatchMapping("/{code}/toggle-status")
    public ApiResponse<?> toggleStaffStatus(@PathVariable("code") Integer staffId) {
        staffService.toggleStaffStatus(staffId);
        return ApiResponse.builder()
                .code(200)
                .result("Đã thay đổi trạng thái tài khoản nhân viên thành công")
                .build();
    }
}