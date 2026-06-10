package com.example.bookingtour.dtos.response.profile;

import com.example.bookingtour.entities.CustomerProfile;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerProfileResponse {

    private String email;

    private String fullName;
    private String phone;
    private String address;
    private String nationality;
    private String identityType;
    private String identityNumber;
    private Integer loyaltyPoints;

    public static CustomerProfileResponse fromCustomerProfile(CustomerProfile profile) {
        String email = (profile.getEmail() != null) ? profile.getEmail() :
                (profile.getUser() != null) ? profile.getUser().getEmail() : null;

        return CustomerProfileResponse.builder()
                .email(email)
                .fullName(profile.getFullName())
                .phone(profile.getPhone())
                .address(profile.getAddress())
                .nationality(profile.getNationality())
                .identityType(profile.getIdentityType())
                .identityNumber(profile.getIdentityNumber())
                .loyaltyPoints(profile.getLoyaltyPoints() != null ? profile.getLoyaltyPoints() : 0)
                .build();
    }
}