package com.example.bookingtour.controllers;

import com.example.bookingtour.dtos.request.payment.ManualPaymentRequest;
import com.example.bookingtour.dtos.response.ApiResponse;
import com.example.bookingtour.entities.Payment;
import com.example.bookingtour.IServices.IPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping("/manual")
    public ApiResponse<Payment> createManualPayment(@Valid @RequestBody ManualPaymentRequest request) {
        // Cứ gọi thẳng, nếu có lỗi thì GlobalExceptionHandler tự bắt
        Payment savedPayment = paymentService.processManualPayment(request);

        return ApiResponse.<Payment>builder()
                .code(1000)
                .message("Xác nhận thanh toán thủ công thành công")
                .result(savedPayment)
                .build();
    }

    @GetMapping("/booking/{bookingId}")
    public ApiResponse<List<Payment>> getPaymentHistory(@PathVariable Integer bookingId) {
        List<Payment> history = paymentService.getPaymentHistoryByBookingId(bookingId);

        return ApiResponse.<List<Payment>>builder()
                .code(1000)
                .message("Lấy lịch sử thanh toán thành công")
                .result(history)
                .build();
    }
}