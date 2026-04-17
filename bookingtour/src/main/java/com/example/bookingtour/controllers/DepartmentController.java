package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.IDepartmentService;
import com.example.bookingtour.dtos.request.admin.DepartmentRequest;
import com.example.bookingtour.dtos.response.admin.DepartmentResponse;
import com.example.bookingtour.dtos.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/departments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DepartmentController {

    IDepartmentService departmentService;

    @PostMapping
    public ApiResponse<DepartmentResponse> createDepartment(@RequestBody @Valid DepartmentRequest request) {
        return ApiResponse.<DepartmentResponse>builder()
                .result(departmentService.createDepartment(request))
                .message("Tạo phòng ban mới thành công!")
                .build();
    }

    @GetMapping
    public ApiResponse<List<DepartmentResponse>> getAllDepartments() {
        return ApiResponse.<List<DepartmentResponse>>builder()
                .result(departmentService.getAllDepartments())
                .build();
    }

    @PutMapping("/{departmentId}")
    public ApiResponse<DepartmentResponse> updateDepartment(
            @PathVariable String departmentId,
            @RequestBody @Valid DepartmentRequest request) {
        return ApiResponse.<DepartmentResponse>builder()
                .result(departmentService.updateDepartment(departmentId, request))
                .message("Cập nhật phòng ban thành công!")
                .build();
    }
}