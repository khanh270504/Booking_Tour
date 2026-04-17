package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IDepartmentService;
import com.example.bookingtour.dtos.request.admin.DepartmentRequest;
import com.example.bookingtour.dtos.response.admin.DepartmentResponse;
import com.example.bookingtour.entities.Department;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.DepartmentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DepartmentServiceImpl implements IDepartmentService {

    DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {

        String depId = request.getDepartmentId().trim().toUpperCase();

        if (departmentRepository.existsById(depId)) {
            throw new AppException(ErrorCode.DEPARTMENT_EXISTED);
        }

        Department department = Department.builder()
                .departmentId(depId)
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();

        return DepartmentResponse.fromDepartment(departmentRepository.save(department));
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::fromDepartment)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(String departmentId, DepartmentRequest request) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_EXISTED));

        department.setName(request.getName().trim());
        department.setDescription(request.getDescription());

        return DepartmentResponse.fromDepartment(departmentRepository.save(department));
    }
}