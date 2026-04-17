package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.IRoleService;
import com.example.bookingtour.dtos.request.admin.RoleRequest;
import com.example.bookingtour.dtos.response.admin.RoleResponse;
import com.example.bookingtour.dtos.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {

    IRoleService roleService;

    @PostMapping
    public ApiResponse<RoleResponse> createRole(@RequestBody @Valid RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.createRole(request))
                .message("Tạo quyền mới thành công!")
                .build();
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> getAllRoles() {
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAllRoles())
                .build();
    }

    @PutMapping("/{roleName}")
    public ApiResponse<RoleResponse> updateRole(
            @PathVariable String roleName,
            @RequestBody @Valid RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.updateRole(roleName, request))
                .message("Cập nhật mô tả quyền thành công!")
                .build();
    }
}