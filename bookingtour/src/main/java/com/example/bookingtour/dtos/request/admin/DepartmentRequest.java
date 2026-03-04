package com.example.bookingtour.dtos.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class DepartmentRequest {
    private String departmentId;

    @NotBlank(message = "Tên phòng ban không được để trống")
    private String name;

    private String description;

}
