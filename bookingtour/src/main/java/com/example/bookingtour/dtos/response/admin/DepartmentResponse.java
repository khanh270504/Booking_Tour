package com.example.bookingtour.dtos.response.admin;

import com.example.bookingtour.entities.Department;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentResponse {
    private String departmentId;
    private String name;
    private String description;

    public static DepartmentResponse fromDepartment(Department department) {
        if (department == null) return null;

        return DepartmentResponse.builder()

                .departmentId(department.getDepartmentId())
                .name(department.getName())
                .description(department.getDescription())
                .build();
    }
}