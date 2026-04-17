package com.example.bookingtour.controllers;

import com.example.bookingtour.dtos.request.auth.IntrospectRequest;
import com.example.bookingtour.dtos.request.auth.LoginRequest;
import com.example.bookingtour.dtos.request.auth.RegisterRequest;
import com.example.bookingtour.dtos.response.ApiResponse;
import com.example.bookingtour.dtos.response.auth.AuthenticationResponse;
import com.example.bookingtour.dtos.response.auth.IntrospectResponse;
import com.example.bookingtour.services.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(
            @RequestBody @Valid LoginRequest dto,
            HttpServletResponse response) {

        var authResponse = authService.authenticate(dto);

        Cookie refreshCookie = new Cookie("refresh_token", authResponse.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(3600 * 24);
        refreshCookie.setAttribute("SameSite", "Strict");

        response.addCookie(refreshCookie);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(AuthenticationResponse.builder()
                        .token(authResponse.getToken())
                        .authenticated(true)
                        .build())
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response) throws ParseException, JOSEException {

        String refreshToken = Arrays.stream(request.getCookies() != null ? request.getCookies() : new Cookie[0])
                .filter(c -> "refresh_token".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        var authResponse = authService.refreshToken(refreshToken);

        Cookie newCookie = new Cookie("refresh_token", authResponse.getRefreshToken());
        newCookie.setHttpOnly(true);
        newCookie.setSecure(false);
        newCookie.setPath("/");
        newCookie.setMaxAge(3600 * 24);
        newCookie.setAttribute("SameSite", "Strict");
        response.addCookie(newCookie);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(AuthenticationResponse.builder()
                        .token(authResponse.getToken())
                        .authenticated(true)
                        .build())
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }

        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return ApiResponse.<Void>builder()
                .message("Đăng xuất thành công và vô hiệu hóa Token!")
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(
            @RequestBody IntrospectRequest dto) throws ParseException, JOSEException {

        var result = authService.introspect(dto);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/register")
    public ApiResponse<AuthenticationResponse> register(@RequestBody @Valid RegisterRequest dto) {
        var authResponse = authService.register(dto);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authResponse)
                .message("Đăng ký thành công! Chào mừng bạn đến với Booking Tour.")
                .build();
    }
}