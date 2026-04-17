package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.admin.StaffCreateRequest;
import com.example.bookingtour.dtos.request.admin.StaffUpdateRequest;
import com.example.bookingtour.dtos.response.auth.UserResponse;
import com.example.bookingtour.dtos.response.profile.StaffProfileResponse;

import java.util.List;

public interface IStaffService {


    UserResponse createStaff(StaffCreateRequest request);


    UserResponse updateStaff(Integer userId, StaffUpdateRequest request);


    void toggleStaffStatus(String employeeCode);


    StaffProfileResponse getStaffByCode(String employeeCode);


    List<StaffProfileResponse> getAllStaffs();
}