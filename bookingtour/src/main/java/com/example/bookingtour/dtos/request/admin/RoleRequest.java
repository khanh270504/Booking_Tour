package com.example.bookingtour.dtos.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleRequest {
    // Khóa chính của bảng Role là role_name (VD: "ADMIN", "STAFF_SALE")
    @NotBlank(message = "Tên quyền (Role Name) không được để trống")
    private String roleName;

    private String description;
}