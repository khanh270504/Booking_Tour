package com.example.bookingtour.dtos.response.profile;

import com.example.bookingtour.entities.StaffProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffProfileResponse {
    private String employeeCode;
    private String fullName;
    private String phone;
    private String departmentName;
    private String position;
    private LocalDate hireDate;
    private String status;
    public static StaffProfileResponse fromStaffProfile(StaffProfile entity) {
        if (entity == null) return null;

        return StaffProfileResponse.builder()
                .employeeCode(entity.getEmployeeCode())
                .fullName(entity.getFullName())
                .phone(entity.getPhone())
                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getName() : "N/A")
                .position(entity.getPosition())
                .hireDate(entity.getHireDate())
                .status(entity.getUser() != null ? entity.getUser().getStatus().name() : "UNKNOWN")
                .build();
    }
}