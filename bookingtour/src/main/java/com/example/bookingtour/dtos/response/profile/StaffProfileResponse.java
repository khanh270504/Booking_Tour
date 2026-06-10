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
    private Integer staffId;
    private String employeeCode;
    private String fullName;

    private String email;
    private String phone;

    private String departmentId;
    private String departmentName;
    private String position;

    private String roleName;
    private LocalDate hireDate;
    private String status;

    public static StaffProfileResponse fromStaffProfile(StaffProfile entity) {
        if (entity == null) return null;

        String userEmail = null;
        String userRole = null;
        String userStatus = "UNKNOWN";

        if (entity.getUser() != null) {
            userEmail = entity.getUser().getEmail();
            userStatus = entity.getUser().getStatus().name();

            if (entity.getUser().getRole() != null) {
                userRole = entity.getUser().getRole().getRoleName();
            }
        }

        String deptId = null;
        String deptName = "N/A";

        if (entity.getDepartment() != null) {
            // Ép sang String bằng String.valueOf nếu ID ở DB của sếp là Long/Integer
            deptId = String.valueOf(entity.getDepartment().getDepartmentId());
            deptName = entity.getDepartment().getName();
        }

        return StaffProfileResponse.builder()
                .staffId(entity.getId())
                .employeeCode(entity.getEmployeeCode())
                .fullName(entity.getFullName())
                .email(userEmail)
                .phone(entity.getPhone())
                .departmentId(deptId)
                .departmentName(deptName)
                .position(entity.getPosition())
                .roleName(userRole)
                .hireDate(entity.getHireDate())
                .status(userStatus)
                .build();
    }
}