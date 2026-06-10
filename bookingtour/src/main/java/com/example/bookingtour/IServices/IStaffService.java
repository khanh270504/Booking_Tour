package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.admin.StaffCreateRequest;
import com.example.bookingtour.dtos.request.admin.StaffUpdateRequest;
import com.example.bookingtour.dtos.response.auth.UserResponse;
import com.example.bookingtour.dtos.response.profile.StaffProfileResponse;

import java.util.List;

public interface IStaffService {


    StaffProfileResponse createStaff(StaffCreateRequest request);


    StaffProfileResponse updateStaff(Integer userId, StaffUpdateRequest request);


    void toggleStaffStatus(Integer staffId);


    StaffProfileResponse getStaffByCode(String employeeCode);


    List<StaffProfileResponse> getAllStaffs();
}