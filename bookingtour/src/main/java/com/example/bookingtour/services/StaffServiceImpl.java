package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IStaffService;
import com.example.bookingtour.dtos.request.admin.StaffCreateRequest;
import com.example.bookingtour.dtos.request.admin.StaffUpdateRequest;
import com.example.bookingtour.dtos.response.profile.StaffProfileResponse;
import com.example.bookingtour.entities.StaffProfile;
import com.example.bookingtour.entities.User;
import com.example.bookingtour.enums.UserStatus;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StaffServiceImpl implements IStaffService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    DepartmentRepository departmentRepository;
    StaffProfileRepository staffProfileRepository;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public StaffProfileResponse createStaff(StaffCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        var department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_EXISTED));

        var role = roleRepository.findById(request.getRoleName())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        String generatedEmployeeCode = "NV-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        var newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status(UserStatus.ACTIVE)
                .userCode(generatedEmployeeCode)
                .build();

        newUser = userRepository.save(newUser);

        var staffProfile = StaffProfile.builder()
                .user(newUser)
                .employeeCode(generatedEmployeeCode)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .position(request.getPosition())
                .department(department)
                .hireDate(LocalDate.now())
                .build();

        var savedProfile = staffProfileRepository.save(staffProfile);
        return StaffProfileResponse.fromStaffProfile(savedProfile);
    }

    @Override
    @Transactional
    public StaffProfileResponse updateStaff(Integer userId, StaffUpdateRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var profile = staffProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request.getRoleName() != null && !request.getRoleName().isBlank()) {
            var role = roleRepository.findById(request.getRoleName())
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
            user.setRole(role);
        }

        if (request.getDepartmentId() != null) {
            var department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_EXISTED));
            profile.setDepartment(department);
        }

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            profile.setFullName(request.getFullName());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            profile.setPhone(request.getPhone());
        }
        if (request.getPosition() != null && !request.getPosition().isBlank()) {
            profile.setPosition(request.getPosition());
        }
        if (request.getHireDate() != null) {
            profile.setHireDate(request.getHireDate());
        }

        return StaffProfileResponse.fromStaffProfile(profile);
    }

    @Override
    @Transactional
    public void toggleStaffStatus(Integer staffId) {
        var profile = staffProfileRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var user = profile.getUser();
        user.setStatus(user.getStatus() == UserStatus.ACTIVE ? UserStatus.BLOCKED : UserStatus.ACTIVE);
        userRepository.save(user); // Nhớ lưu lại nhé
    }
    @Override
    public StaffProfileResponse getStaffByCode(String employeeCode) {
        var profile = staffProfileRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return StaffProfileResponse.fromStaffProfile(profile);
    }

    @Override
    public List<StaffProfileResponse> getAllStaffs() {
        return staffProfileRepository.findAll().stream()
                .map(StaffProfileResponse::fromStaffProfile)
                .collect(Collectors.toList());
    }
}