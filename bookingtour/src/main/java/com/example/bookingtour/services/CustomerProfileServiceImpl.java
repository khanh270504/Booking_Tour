package com.example.bookingtour.services;

import com.example.bookingtour.dtos.request.profile.CustomerUpdateProfileRequest;
import com.example.bookingtour.dtos.response.profile.CustomerListResponse;
import com.example.bookingtour.dtos.response.profile.CustomerProfileResponse;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.BookingRepository;
import com.example.bookingtour.repositories.CustomerProfileRepository;
import com.example.bookingtour.IServices.ICustomerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CustomerProfileServiceImpl implements ICustomerProfileService {

    private final CustomerProfileRepository customerRepository;
    private final BookingRepository bookingRepository;

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

    @Override
    public Page<CustomerListResponse> getAllCustomers(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

        return customerRepository.searchCustomers(keyword, pageable)
                .map(profile -> {
                    long totalBookings = bookingRepository.countByCustomer_Id(profile.getId());
                    BigDecimal totalSpent = bookingRepository.sumTotalPriceByCustomer_Id(profile.getId());

                    return CustomerListResponse.from(profile, totalBookings, totalSpent);
                });
    }
    @Override
    public CustomerProfileResponse getCustomerDetail(Integer customerId) {
        var profile = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return CustomerProfileResponse.fromCustomerProfile(profile);
    }
    @Override
    @Transactional
    public CustomerProfileResponse updateAdminCustomer(Integer customerId, CustomerUpdateProfileRequest request) {
        // Tìm theo ID khách hàng, không phải ID user
        var profile = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Cập nhật tất cả các trường từ request
        if (request.getFullName() != null) profile.setFullName(request.getFullName());
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());
        if (request.getNationality() != null) profile.setNationality(request.getNationality());
        if (request.getIdentityType() != null) profile.setIdentityType(request.getIdentityType());
        if (request.getIdentityNumber() != null) profile.setIdentityNumber(request.getIdentityNumber());

        // Dùng @Transactional nên Hibernate sẽ tự động lưu (dirty checking)
        return CustomerProfileResponse.fromCustomerProfile(profile);
    }
}