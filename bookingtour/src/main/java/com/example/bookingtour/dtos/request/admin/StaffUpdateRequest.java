package com.example.bookingtour.dtos.request.admin;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Builder
public class StaffUpdateRequest {
    private String fullName;
    private String phone;
    private String departmentId;
    private LocalDate hireDate;
    private String status;
}