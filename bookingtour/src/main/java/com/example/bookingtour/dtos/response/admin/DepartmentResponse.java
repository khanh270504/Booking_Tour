package com.example.bookingtour.dtos.response.admin;

import lombok.Data;

@Data
public class DepartmentResponse {
    private String departmentId;
    private String name;
    private String description;
}