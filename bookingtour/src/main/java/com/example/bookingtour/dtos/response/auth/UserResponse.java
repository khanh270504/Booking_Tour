package com.example.bookingtour.dtos.response.auth;

import lombok.Data;
import java.time.Instant;

@Data
public class UserResponse {
    private String userId;
    private String email;
    private String roleName;
    private String status;
    private Instant createdAt;
}