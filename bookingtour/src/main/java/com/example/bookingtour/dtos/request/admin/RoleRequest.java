package com.example.bookingtour.dtos.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleRequest {
    @NotBlank(message = "Tên quyền (Role Name) không được để trống")
    private String roleName;

    private String description;
}