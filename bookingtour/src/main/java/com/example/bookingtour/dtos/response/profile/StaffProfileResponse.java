package com.example.bookingtour.dtos.response.profile;

import lombok.Data;
import java.time.LocalDate;

@Data
public class StaffProfileResponse {
    private String employeeCode;
    private String fullName;
    private String phone;
    private String departmentName; // Gom từ bảng departments
    private LocalDate hireDate;
}