package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.ITourService;
import com.example.bookingtour.dtos.request.tour.*;
import com.example.bookingtour.dtos.response.ApiResponse;
import com.example.bookingtour.dtos.response.tour.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TourController {

    private final ITourService tourService;

    // ================= CLIENT APIs (CHO KHÁCH) =================

    @GetMapping("/tours")
    public ApiResponse<List<TourResponse>> getAllToursForClient() {
        return ApiResponse.<List<TourResponse>>builder()
                .result(tourService.getAllToursForClient())
                .build();
    }

    @GetMapping("/tours/{id}")
    public ApiResponse<TourResponse> getTourDetailForClient(@PathVariable Integer id) {
        return ApiResponse.<TourResponse>builder()
                .result(tourService.getTourByIdForClient(id))
                .build();
    }

    // ================= ADMIN APIs (CHO QUẢN TRỊ) =================

    @PostMapping("/admin/tours")
    public ApiResponse<TourResponse> createTour(@RequestBody @Valid TourCreateRequest request) {
        return ApiResponse.<TourResponse>builder()
                .result(tourService.createTour(request))
                .build();
    }

    @GetMapping("/admin/tours")
    public ApiResponse<List<TourResponse>> getAllToursForAdmin() {
        return ApiResponse.<List<TourResponse>>builder()
                .result(tourService.getAllTours())
                .build();
    }

    @DeleteMapping("/admin/tours/{id}")
    public ApiResponse<String> deleteTour(@PathVariable Integer id) {
        tourService.deleteTour(id);
        return ApiResponse.<String>builder()
                .result("Tour đã được chuyển sang trạng thái INACTIVE thành công")
                .build();
    }

    // Quản lý Lịch trình (Schedules)
    @PostMapping("/admin/tours/schedules")
    public ApiResponse<ScheduleResponse> createSchedule(@RequestBody @Valid ScheduleCreateRequest request) {
        return ApiResponse.<ScheduleResponse>builder()
                .result(tourService.createSchedule(request))
                .build();
    }

    // Quản lý Giá (Pricing)
    @PostMapping("/admin/tours/pricing")
    public ApiResponse<PricingConfigResponse> createPricing(@RequestBody @Valid PricingConfigRequest request) {
        return ApiResponse.<PricingConfigResponse>builder()
                .result(tourService.createPricing(request))
                .build();
    }

    // Quản lý Phụ phí (Surcharges)
    @PostMapping("/admin/tours/surcharges")
    public ApiResponse<SurchargeResponse> createSurcharge(@RequestBody @Valid SurchargeRequest request) {
        return ApiResponse.<SurchargeResponse>builder()
                .result(tourService.createSurcharge(request))
                .build();
    }
}