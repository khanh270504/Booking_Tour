package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.admin.DepartmentRequest;
import com.example.bookingtour.dtos.response.admin.DepartmentResponse;

import java.util.List;

public interface IDepartmentService {
    DepartmentResponse createDepartment(DepartmentRequest request);
    List<DepartmentResponse> getAllDepartments();
    DepartmentResponse updateDepartment(String departmentId, DepartmentRequest request);
}