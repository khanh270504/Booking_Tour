package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.profile.CustomerUpdateProfileRequest;
import com.example.bookingtour.dtos.response.profile.CustomerListResponse;
import com.example.bookingtour.dtos.response.profile.CustomerProfileResponse;
import org.springframework.data.domain.Page;

public interface ICustomerProfileService {

    // Khách hàng tự xem hồ sơ của mình
    CustomerProfileResponse getMyProfile(Integer userId);

    // Khách hàng tự cập nhật hồ sơ của mình
    CustomerProfileResponse updateMyProfile(Integer userId, CustomerUpdateProfileRequest request);

    Page<CustomerListResponse> getAllCustomers(int page, int size, String keyword);
    CustomerProfileResponse getCustomerDetail(Integer customerId);
    CustomerProfileResponse updateAdminCustomer(Integer customerId, CustomerUpdateProfileRequest request);
}