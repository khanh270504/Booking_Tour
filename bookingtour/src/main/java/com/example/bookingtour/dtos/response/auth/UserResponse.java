package com.example.bookingtour.dtos.response.auth;

import com.example.bookingtour.entities.User;
import com.example.bookingtour.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Integer userId;
    private String userCode;
    private String email;
    private String roleName;
    private UserStatus status;

    private Instant createdAt;

    public static UserResponse fromUser(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .userId(user.getId())
                .userCode(user.getUserCode())
                .email(user.getEmail())
                .roleName(user.getRole() != null ? user.getRole().getRoleName() : null)
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}