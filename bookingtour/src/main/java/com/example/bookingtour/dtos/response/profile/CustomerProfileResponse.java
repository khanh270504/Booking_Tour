package com.example.bookingtour.dtos.response.profile;

import lombok.Data;

@Data
public class CustomerProfileResponse {
    private String fullName;
    private String phone;
    private String address;
    private String nationality;
    private String identityType;
    private String identityNumber;
    private Integer loyaltyPoints;
}