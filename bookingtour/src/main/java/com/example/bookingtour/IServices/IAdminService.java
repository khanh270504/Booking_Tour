package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.admin.StaffCreateRequest;
import com.example.bookingtour.dtos.response.profile.StaffProfileResponse;

public interface IAdminService {

    StaffProfileResponse createStaff(StaffCreateRequest request);

    void toggleStaffStatus(String employeeCode);
}