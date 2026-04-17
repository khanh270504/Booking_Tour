package com.example.bookingtour.dtos.response.admin;

import com.example.bookingtour.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleResponse {
    private String roleName;
    private String description;

    public static RoleResponse fromRole(Role role) {
        return RoleResponse.builder()
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .build();
    }
}