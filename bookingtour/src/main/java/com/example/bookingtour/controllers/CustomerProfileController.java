package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.ICustomerProfileService;
import com.example.bookingtour.dtos.request.profile.CustomerUpdateProfileRequest;
import com.example.bookingtour.dtos.response.profile.CustomerProfileResponse;
import com.example.bookingtour.IServices.ICustomerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final ICustomerProfileService customerService;

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("Vui lòng đăng nhập");
        }
        return Integer.parseInt(authentication.getName());
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerProfileResponse> getMyProfile() {
        Integer userId = getCurrentUserId();
        return ResponseEntity.ok(customerService.getMyProfile(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<CustomerProfileResponse> updateMyProfile(
            @Valid @RequestBody CustomerUpdateProfileRequest request) {
        Integer userId = getCurrentUserId();
        return ResponseEntity.ok(customerService.updateMyProfile(userId, request));
    }
}