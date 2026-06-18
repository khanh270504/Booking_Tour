package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.INotificationService;
import com.example.bookingtour.dtos.response.ApiResponse; // Nhớ import đúng file ApiResponse của ông giáo vào đây
import com.example.bookingtour.dtos.response.notification.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final INotificationService notificationService;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getMyNotifications() {
        List<NotificationResponse> notis = notificationService.getMyNotifications();

        return ApiResponse.<List<NotificationResponse>>builder()
                .code(200)
                .message("Tải danh sách thông báo thành công")
                .result(notis)
                .build();
    }

    @PutMapping("/read-all")
    public ApiResponse<String> markAllAsRead() {
        notificationService.markAllAsRead();

        return ApiResponse.<String>builder()
                .code(200)
                .message("Đã đánh dấu đọc tất cả thông báo")
                .result("Success")
                .build();
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);

        return ApiResponse.<String>builder()
                .code(200)
                .message("Cập nhật trạng thái đọc thông báo thành công")
                .result("Success")
                .build();
    }
}