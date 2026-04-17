package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.IStaffService;
import com.example.bookingtour.dtos.request.admin.StaffCreateRequest;
import com.example.bookingtour.dtos.request.admin.StaffUpdateRequest;
import com.example.bookingtour.dtos.response.auth.UserResponse;
import com.example.bookingtour.dtos.response.profile.StaffProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/staffs")
@RequiredArgsConstructor

@PreAuthorize("hasRole('ADMIN')")
public class StaffController {

    private final IStaffService staffService;

    @PostMapping
    public ResponseEntity<UserResponse> createStaff(@Valid @RequestBody StaffCreateRequest request) {
        return ResponseEntity.ok(staffService.createStaff(request));
    }

    @GetMapping
    public ResponseEntity<List<StaffProfileResponse>> getAllStaffs() {
        return ResponseEntity.ok(staffService.getAllStaffs());
    }

    @GetMapping("/{code}")
    public ResponseEntity<StaffProfileResponse> getStaffByCode(@PathVariable("code") String employeeCode) {
        return ResponseEntity.ok(staffService.getStaffByCode(employeeCode));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateStaff(
            @PathVariable("id") Integer userId,
            @Valid @RequestBody StaffUpdateRequest request) {
        return ResponseEntity.ok(staffService.updateStaff(userId, request));
    }

    @PatchMapping("/{code}/toggle-status")
    public ResponseEntity<String> toggleStaffStatus(@PathVariable("code") String employeeCode) {
        staffService.toggleStaffStatus(employeeCode);
        return ResponseEntity.ok("Đã thay đổi trạng thái tài khoản nhân viên thành công");
    }
}