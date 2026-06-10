package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.ICustomerProfileService;
import com.example.bookingtour.dtos.request.profile.CustomerUpdateProfileRequest;
import com.example.bookingtour.dtos.response.ApiResponse;
import com.example.bookingtour.dtos.response.profile.CustomerListResponse;
import com.example.bookingtour.dtos.response.profile.CustomerProfileResponse;
import com.example.bookingtour.IServices.ICustomerProfileService;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final ICustomerProfileService customerService;

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            Long userId = jwt.getClaim("userId");
            if (userId == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);
            return userId.intValue();
        }

        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    @GetMapping("/me")
    public ApiResponse<CustomerProfileResponse> getMyProfile() {
        Integer userId = getCurrentUserId();
        return ApiResponse.<CustomerProfileResponse>builder()
                .result(customerService.getMyProfile(userId))
                .build();
    }

    @PutMapping("/me")
    public ApiResponse<CustomerProfileResponse> updateMyProfile(
            @Valid @RequestBody CustomerUpdateProfileRequest request) {
        Integer userId = getCurrentUserId();
        return ApiResponse.<CustomerProfileResponse>builder()
                .result(customerService.updateMyProfile(userId, request))
                .build();
    }


    @GetMapping("/admin/list")
    //@PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<CustomerListResponse>> getAllCustomers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword) {

        return ApiResponse.<Page<CustomerListResponse>>builder()
                .result(customerService.getAllCustomers(page, size, keyword))
                .build();
    }

    @GetMapping("/admin/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CustomerProfileResponse> getCustomerDetail(@PathVariable Integer id) {
        return ApiResponse.<CustomerProfileResponse>builder()
                .result(customerService.getCustomerDetail(id))
                .build();
    }
    @PutMapping("/admin/{id}")
    public ApiResponse<CustomerProfileResponse> updateAdminCustomer(
            @PathVariable Integer customerId,
            @RequestBody CustomerUpdateProfileRequest request) {
        return ApiResponse.<CustomerProfileResponse>builder()
                .result(customerService.updateAdminCustomer(customerId, request))
                .build();
    }
}