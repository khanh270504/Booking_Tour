package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.profile.CustomerUpdateProfileRequest;
import com.example.bookingtour.dtos.response.profile.CustomerProfileResponse;

public interface ICustomerProfileService {

    // Khách hàng tự xem hồ sơ của mình
    CustomerProfileResponse getMyProfile(Integer userId);

    // Khách hàng tự cập nhật hồ sơ của mình
    CustomerProfileResponse updateMyProfile(Integer userId, CustomerUpdateProfileRequest request);

}