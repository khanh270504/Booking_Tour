package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.admin.RoleRequest;
import com.example.bookingtour.dtos.response.admin.RoleResponse;

import java.util.List;

public interface IRoleService {
    RoleResponse createRole(RoleRequest request);
    List<RoleResponse> getAllRoles();
    RoleResponse updateRole(String roleName, RoleRequest request);

}