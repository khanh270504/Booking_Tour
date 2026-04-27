package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.IBookingService;
import com.example.bookingtour.dtos.request.booking.BookingCancelRequest;
import com.example.bookingtour.dtos.request.booking.BookingCreateRequest;
import com.example.bookingtour.dtos.response.booking.BookingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
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

    // 1. TẠO BOOKING
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody BookingCreateRequest request) {

        Integer currentUserId = getCurrentUserIdSafely();
        log.info("API: Nhận yêu cầu tạo Booking cho email {}. UserId: {}", request.getEmail(), currentUserId);

        BookingResponse response = bookingService.createBooking(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Integer bookingId) {
        log.info("API: Lấy thông tin đơn hàng ID: {}", bookingId);
        BookingResponse response = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/me")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        Integer currentUserId = getCurrentUserIdSafely();
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Chưa đăng nhập thì cút!
        }

        log.info("API: Lấy danh sách đơn hàng của User ID: {}", currentUserId);
        List<BookingResponse> responses = bookingService.getBookingsByUser(currentUserId);
        return ResponseEntity.ok(responses);
    }

    // 4. HỦY ĐƠN
    @PostMapping("/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@RequestBody BookingCancelRequest request) {
        // Trong thực tế, service phải check xem currentUserId có đúng là chủ của đơn này không mới cho hủy nhé!
        Integer currentUserId = getCurrentUserIdSafely();
        log.info("API: User {} yêu cầu hủy đơn hàng ID: {}", currentUserId, request.getBookingId());

        BookingResponse response = bookingService.cancelBooking(request);
        return ResponseEntity.ok(response);
    }
}