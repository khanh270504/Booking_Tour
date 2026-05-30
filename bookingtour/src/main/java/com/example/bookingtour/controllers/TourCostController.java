package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.ITourCostService;
import com.example.bookingtour.dtos.request.operation.TourCostRequest;
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

    // 🎯 API này siêu quan trọng: Để hiển thị danh sách chi phí trong chi tiết 1 Schedule
    @GetMapping("/schedule/{scheduleId}")
    public ApiResponse<List<TourCostResponse>> getTourCostsByScheduleId(@PathVariable Integer scheduleId) {
        return ApiResponse.<List<TourCostResponse>>builder()
                .code(200)
                .message("Lấy danh sách chi phí của lịch trình thành công")
                .result(tourCostService.getTourCostsByScheduleId(scheduleId))
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
        // payload JSON: {"status": "PAID", "note": "Kế toán đã chuyển khoản"}
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
}