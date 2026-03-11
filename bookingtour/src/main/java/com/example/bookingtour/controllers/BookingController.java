package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.IBookingService;
import com.example.bookingtour.dtos.request.booking.BookingCancelRequest;
import com.example.bookingtour.dtos.request.booking.BookingCreateRequest;
import com.example.bookingtour.dtos.request.payment.ManualPaymentRequest;
import com.example.bookingtour.dtos.response.booking.BookingResponse;
import com.example.bookingtour.dtos.response.payment.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final IBookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody BookingCreateRequest request,
            @RequestParam(required = false) String userId) {

        log.info("API: Nhận yêu cầu tạo Booking cho email {}", request.getEmail());
        BookingResponse response = bookingService.createBooking(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Integer bookingId) {
        log.info("API: Lấy thông tin đơn hàng ID: {}", bookingId);
        BookingResponse response = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(response);
    }

       @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(@PathVariable String userId) {
        log.info("API: Lấy danh sách đơn hàng của User ID: {}", userId);
        List<BookingResponse> responses = bookingService.getBookingsByUser(userId);
        return ResponseEntity.ok(responses);
    }

      @PostMapping("/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@RequestBody BookingCancelRequest request) {
        log.info("API: Nhận yêu cầu hủy đơn hàng ID: {}", request.getBookingId());
        BookingResponse response = bookingService.cancelBooking(request);
        return ResponseEntity.ok(response);
    }

      @PostMapping("/manual-payment")
    public ResponseEntity<PaymentResponse> processManualPayment(@RequestBody ManualPaymentRequest request) {
        log.info("API: Xác nhận thanh toán thủ công cho đơn hàng ID: {}", request.getBookingId());
        PaymentResponse response = bookingService.processManualPayment(request);
        return ResponseEntity.ok(response);
    }
}