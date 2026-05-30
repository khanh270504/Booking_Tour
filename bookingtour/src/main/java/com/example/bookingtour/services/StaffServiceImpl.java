package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IStaffService;
import com.example.bookingtour.dtos.request.admin.StaffCreateRequest;
import com.example.bookingtour.dtos.request.admin.StaffUpdateRequest;
import com.example.bookingtour.dtos.response.profile.StaffProfileResponse; // Dùng duy nhất em này
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

import java.util.List;
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
    public StaffProfileResponse createStaff(StaffCreateRequest request) { // 🎯 Đã đổi kiểu trả về
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        var department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_EXISTED));

        var role = roleRepository.findById(request.getRoleName())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        var newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status(UserStatus.ACTIVE)
                .userCode(request.getEmployeeCode())
                .build();

        newUser = userRepository.save(newUser);

        var staffProfile = StaffProfile.builder()
                .user(newUser)
                .employeeCode(request.getEmployeeCode())
                .fullName(request.getFullName())
                .position(request.getPosition())
                .department(department)
                .build();

        // Lưu thông tin profile và hứng thực thể đã lưu xuống
        var savedProfile = staffProfileRepository.save(staffProfile);
        return StaffProfileResponse.fromStaffProfile(savedProfile);
    }

    @Override
    @Transactional
    public StaffProfileResponse updateStaff(Integer userId, StaffUpdateRequest request) { // 🎯 Đã đổi kiểu trả về
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var profile = staffProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            user.setStatus(UserStatus.valueOf(request.getStatus()));
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

        // Vì nằm trong @Transactional, trạng thái thay đổi của User và Profile
        // sẽ tự động đồng bộ. Ta trả về luôn Profile chứa thông tin mới nhất.
        return StaffProfileResponse.fromStaffProfile(profile);
    }

    @Override
    @Transactional
    public void toggleStaffStatus(String employeeCode) {
        var profile = staffProfileRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var user = profile.getUser();
        user.setStatus(user.getStatus() == UserStatus.ACTIVE ? UserStatus.BLOCKED : UserStatus.ACTIVE);
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