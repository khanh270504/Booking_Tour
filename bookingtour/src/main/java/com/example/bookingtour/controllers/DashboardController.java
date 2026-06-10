package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.IDashboardService;
import com.example.bookingtour.dtos.response.ApiResponse;
import com.example.bookingtour.dtos.response.dashboard.DashboardOverviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DashboardOverviewResponse> getOverview(@RequestParam(defaultValue = "YEAR") String period) {
        return ApiResponse.<DashboardOverviewResponse>builder()
                .result(dashboardService.getOverviewData(period))
                .message("Lấy dữ liệu tổng quan thành công")
                .build();
    }
}