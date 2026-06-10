package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.ITourCostService;
import com.example.bookingtour.dtos.request.operation.TourCostRequest;
import com.example.bookingtour.dtos.response.PageResponse;
import com.example.bookingtour.dtos.response.operation.TourCostResponse;
import com.example.bookingtour.dtos.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tour-costs")
@RequiredArgsConstructor
public class TourCostController {

    private final ITourCostService tourCostService;
//    @GetMapping
//    public ApiResponse<List<TourCostResponse>> getAllTourCosts() {
//        return ApiResponse.<List<TourCostResponse>>builder()
//                .code(200)
//                .message("Lấy toàn bộ danh sách phiếu chi thành công")
//                .result(tourCostService.getAllTourCosts())
//                .build();
//    }

    @PostMapping
    public ApiResponse<TourCostResponse> createTourCost(@RequestBody @Valid TourCostRequest request) {
        return ApiResponse.<TourCostResponse>builder()
                .code(201)
                .message("Thêm khoản chi phí thực tế thành công")
                .result(tourCostService.createTourCost(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<TourCostResponse> updateTourCost(
            @PathVariable Integer id,
            @RequestBody @Valid TourCostRequest request) {
        return ApiResponse.<TourCostResponse>builder()
                .code(200)
                .message("Cập nhật chi phí thành công")
                .result(tourCostService.updateTourCost(id, request))
                .build();
    }

    @GetMapping("/schedule/{scheduleId}")
    public ApiResponse<List<TourCostResponse>> getTourCostsByScheduleId(@PathVariable Integer scheduleId) {
        return ApiResponse.<List<TourCostResponse>>builder()
                .code(200)
                .message("Lấy danh sách chi phí của lịch trình thành công")
                .result(tourCostService.getTourCostsByScheduleId(scheduleId))
                .build();
    }

    @GetMapping("/provider/{providerId}")
    public ApiResponse<List<TourCostResponse>> getTourCostsByProviderId(@PathVariable Integer providerId) {
        return ApiResponse.<List<TourCostResponse>>builder()
                .code(200)
                .message("Lấy danh sách chi phí theo nhà cung cấp thành công")
                .result(tourCostService.getTourCostsByProviderId(providerId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<TourCostResponse> getTourCostById(@PathVariable Integer id) {
        return ApiResponse.<TourCostResponse>builder()
                .code(200)
                .message("Lấy chi tiết khoản chi thành công")
                .result(tourCostService.getTourCostById(id))
                .build();
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<TourCostResponse> updateCostStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        String note = payload.get("note");

        return ApiResponse.<TourCostResponse>builder()
                .code(200)
                .message("Cập nhật trạng thái thanh toán thành công")
                .result(tourCostService.updateCostStatus(id, status, note))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTourCost(@PathVariable Integer id) {
        tourCostService.deleteTourCost(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Đã xóa khoản chi phí thành công")
                .build();
    }
    @GetMapping
    public ApiResponse<PageResponse<TourCostResponse>> getTourCosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        return ApiResponse.<PageResponse<TourCostResponse>>builder()
                .code(200)
                .message("Lấy danh sách phiếu chi thành công")
                .result(tourCostService.getTourCosts(page, size, keyword))
                .build();
    }
    @GetMapping("/stats")
    public ApiResponse<Map<String, Double>> getCostStatistics() {
        return ApiResponse.<Map<String, Double>>builder()
                .code(200)
                .message("Lấy thống kê chi phí thành công")
                .result(tourCostService.getCostStatistics())
                .build();
    }
}