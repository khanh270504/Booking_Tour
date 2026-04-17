package com.example.bookingtour.services;

import com.example.bookingtour.dtos.request.profile.CustomerUpdateProfileRequest;
import com.example.bookingtour.dtos.response.profile.CustomerProfileResponse;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.CustomerProfileRepository;
import com.example.bookingtour.IServices.ICustomerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerProfileServiceImpl implements ICustomerProfileService {

    private final CustomerProfileRepository customerRepository;

    @Override
    public CustomerProfileResponse getMyProfile(Integer userId) {
        var profile = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return CustomerProfileResponse.fromCustomerProfile(profile);
    }

    @Override
    @Transactional
    public CustomerProfileResponse updateMyProfile(Integer userId, CustomerUpdateProfileRequest request) {
        var profile = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Tối ưu Dirty Checking: Chỉ cập nhật những trường Khách hàng được phép sửa
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            profile.setFullName(request.getFullName());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            profile.setPhone(request.getPhone());
        }
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            profile.setAddress(request.getAddress());
        }
        if (request.getNationality() != null) {
            profile.setNationality(request.getNationality());
        }
        if (request.getIdentityType() != null) {
            profile.setIdentityType(request.getIdentityType());
        }
        if (request.getIdentityNumber() != null) {
            profile.setIdentityNumber(request.getIdentityNumber());
        }
        return CustomerProfileResponse.fromCustomerProfile(profile);
    }
}