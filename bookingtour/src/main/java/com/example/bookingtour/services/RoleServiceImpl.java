package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IRoleService;
import com.example.bookingtour.dtos.request.admin.RoleRequest;
import com.example.bookingtour.dtos.response.admin.RoleResponse;
import com.example.bookingtour.entities.Role;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.RoleRepository;
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
public class RoleServiceImpl implements IRoleService {

    RoleRepository roleRepository;

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {

        String roleName = request.getRoleName().trim().toUpperCase();

        if (roleRepository.existsById(roleName)) {
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }

        Role role = Role.builder()
                .roleName(roleName)
                .description(request.getDescription())
                .build();

        return RoleResponse.fromRole(roleRepository.save(role));
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::fromRole)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleResponse updateRole(String roleName, RoleRequest request) {
        Role role = roleRepository.findById(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        role.setDescription(request.getDescription());

        return RoleResponse.fromRole(roleRepository.save(role));
    }
}