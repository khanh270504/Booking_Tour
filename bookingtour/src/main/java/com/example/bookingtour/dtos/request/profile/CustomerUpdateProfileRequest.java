package com.example.bookingtour.dtos.request.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerUpdateProfileRequest {
    @NotBlank
    private String fullName;

    @Pattern(regexp = "^\\d{10}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    private String address;

    private String nationality;

    private String identityType; // CCCD, PASSPORT

    private String identityNumber;
}