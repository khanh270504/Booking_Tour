package com.example.bookingtour.dtos.response.profile;

import com.example.bookingtour.entities.CustomerProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerListResponse {
    private Integer id;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private Integer totalBookings;
    private Double totalSpent;
    private Integer loyaltyPoints;

    public static CustomerListResponse from(CustomerProfile profile, long totalBookings, BigDecimal totalSpent) {
        String email = profile.getEmail() != null ? profile.getEmail() :
                (profile.getUser() != null ? profile.getUser().getEmail() : "Chưa cập nhật");

        return CustomerListResponse.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .phone(profile.getPhone())
                .email(email)
                .address(profile.getAddress() != null ? profile.getAddress() : "Chưa cập nhật")
                .totalBookings((int) totalBookings)
                .totalSpent(totalSpent != null ? totalSpent.doubleValue() : 0.0)
                .loyaltyPoints(profile.getLoyaltyPoints() != null ? profile.getLoyaltyPoints() : 0)
                .build();
    }
}
