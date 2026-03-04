package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.auth.LoginRequest;
import com.example.bookingtour.dtos.request.auth.RegisterRequest;
import com.example.bookingtour.dtos.response.auth.UserResponse;

public interface IAuthService {

    UserResponse login(LoginRequest request);

    UserResponse register(RegisterRequest request);
}