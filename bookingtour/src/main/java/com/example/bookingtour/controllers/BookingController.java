package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.IBookingService;
import com.example.bookingtour.dtos.request.booking.BookingCancelRequest;
import com.example.bookingtour.dtos.request.booking.BookingCreateRequest;
import com.example.bookingtour.dtos.response.ApiResponse;
import com.example.bookingtour.dtos.response.booking.BookingResponse;
import com.example.bookingtour.dtos.response.booking.PassengerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final IBookingService bookingService;

    private Integer getCurrentUserIdSafely() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) auth.getPrincipal();
            Long userId = jwt.getClaim("userId");
            return userId != null ? userId.intValue() : null;
        }
        return null;
    }



    @PostMapping("/bookings")
    public ApiResponse<BookingResponse> createBooking(@RequestBody BookingCreateRequest request) {
        Integer currentUserId = getCurrentUserIdSafely();

        log.info("API: Nhận yêu cầu tạo Booking cho email {}. UserId: {}",
                request.getContactInfo() != null ? request.getContactInfo().getEmail() : "Ẩn danh",
                currentUserId);

        return ApiResponse.<BookingResponse>builder()
                .code(201) // Mã HTTP Created
                .message("Tạo đơn đặt tour thành công")
                .result(bookingService.createBooking(request, currentUserId))
                .build();
    }

    @GetMapping("/bookings/{bookingId}")
    public ApiResponse<BookingResponse> getBookingById(@PathVariable Integer bookingId) {
        log.info("API: Lấy thông tin đơn hàng ID: {}", bookingId);

        return ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("Lấy thông tin đơn hàng thành công")
                .result(bookingService.getBookingById(bookingId))
                .build();
    }

    @GetMapping("/bookings/me")
    public ApiResponse<List<BookingResponse>> getMyBookings() {
        Integer currentUserId = getCurrentUserIdSafely();

        if (currentUserId == null) {
            return ApiResponse.<List<BookingResponse>>builder()
                    .code(401)
                    .message("Vui lòng đăng nhập để xem danh sách đơn hàng")
                    .build();
        }

        log.info("API: Lấy danh sách đơn hàng của User ID: {}", currentUserId);

        return ApiResponse.<List<BookingResponse>>builder()
                .code(200)
                .result(bookingService.getBookingsByUser(currentUserId))
                .build();
    }

    @PostMapping("/bookings/cancel")
    public ApiResponse<BookingResponse> cancelBooking(@RequestBody BookingCancelRequest request) {
        Integer currentUserId = getCurrentUserIdSafely();

        if (currentUserId == null) {
            return ApiResponse.<BookingResponse>builder()
                    .code(401)
                    .message("Vui lòng đăng nhập để thực hiện thao tác này")
                    .build();
        }

        log.info("API: User {} yêu cầu hủy đơn hàng ID: {}", currentUserId, request.getBookingId());

        return ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("Hủy đơn hàng thành công")
                .result(bookingService.cancelBooking(request, currentUserId))
                .build();
    }

    @GetMapping("/bookings/lookup")
    public ApiResponse<BookingResponse> lookupBooking(
            @RequestParam String bookingCode,
            @RequestParam String email) {

        return ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("Tra cứu đơn hàng thành công")
                .result(bookingService.lookupBooking(bookingCode, email))
                .build();
    }


    @GetMapping("/admin/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<BookingResponse>> getAllBookingsForAdmin() {
        return ApiResponse.<List<BookingResponse>>builder()
                .code(200)
                .message("Lấy danh sách đơn hàng thành công")
                .result(bookingService.getAllBookingsForAdmin())
                .build();
    }

    @PatchMapping("/admin/bookings/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BookingResponse> updateBookingStatus(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, String> payload) {

        String status = payload.get("status");
        String reason = payload.getOrDefault("reason", "Admin cập nhật trạng thái đơn hàng");

        return ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("Cập nhật trạng thái đơn hàng thành công")
                .result(bookingService.updateBookingStatus(id, status, reason))
                .build();
    }
    @GetMapping("/bookings/schedule/{scheduleId}/passengers")
    public ApiResponse<List<PassengerResponse>> getPassengersBySchedule(@PathVariable Integer scheduleId) {
        log.info("API: Hệ thống yêu cầu xuất danh sách đoàn cho Schedule ID: {}", scheduleId);

        List<PassengerResponse> passengers = bookingService.getPassengersByScheduleId(scheduleId);

        return ApiResponse.<List<PassengerResponse>>builder()
                .code(200)
                .message("Lấy danh sách hành khách theo lịch trình thành công")
                .result(passengers)
                .build();
    }
}