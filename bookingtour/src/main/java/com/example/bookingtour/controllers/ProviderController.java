package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.IProviderService;
import com.example.bookingtour.dtos.request.operation.ProviderRequest;
import com.example.bookingtour.dtos.response.operation.ProviderResponse;
import com.example.bookingtour.dtos.response.ApiResponse;
import com.example.bookingtour.enums.ProviderStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final IProviderService providerService;

    @PostMapping
    public ApiResponse<ProviderResponse> createProvider(@RequestBody @Valid ProviderRequest request) {
        return ApiResponse.<ProviderResponse>builder()
                .code(201)
                .message("Tạo nhà cung cấp thành công")
                .result(providerService.createProvider(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ProviderResponse> updateProvider(
            @PathVariable Integer id,
            @RequestBody @Valid ProviderRequest request) {
        return ApiResponse.<ProviderResponse>builder()
                .code(200)
                .message("Cập nhật thông tin nhà cung cấp thành công")
                .result(providerService.updateProvider(id, request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProviderResponse> getProviderById(@PathVariable Integer id) {
        return ApiResponse.<ProviderResponse>builder()
                .code(200)
                .message("Lấy thông tin nhà cung cấp thành công")
                .result(providerService.getProviderById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<ProviderResponse>> getAllProviders() {
        return ApiResponse.<List<ProviderResponse>>builder()
                .code(200)
                .message("Lấy danh sách nhà cung cấp thành công")
                .result(providerService.getAllProviders())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProvider(@PathVariable Integer id) {
        providerService.deleteProvider(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Đã xóa/ngừng hợp tác nhà cung cấp thành công")
                .build();
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<ProviderResponse> changeStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, ProviderStatus> payload) {
        // Lấy status từ body JSON {"status": "ACTIVE"}
        return ApiResponse.<ProviderResponse>builder()
                .code(200)
                .message("Cập nhật trạng thái thành công")
                .result(providerService.changeStatus(id, payload.get("status")))
                .build();
    }
}