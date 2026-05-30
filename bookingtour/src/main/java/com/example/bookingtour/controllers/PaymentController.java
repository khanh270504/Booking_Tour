package com.example.bookingtour.controllers;

import com.example.bookingtour.dtos.request.payment.ManualPaymentRequest;
import com.example.bookingtour.dtos.response.ApiResponse;
import com.example.bookingtour.dtos.response.payment.PaymentResponse;
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
    public ApiResponse<PaymentResponse> createManualPayment(@Valid @RequestBody ManualPaymentRequest request) {
        PaymentResponse response = paymentService.processManualPayment(request);
        return ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("Xác nhận thanh toán thủ công thành công")
                .result(response)
                .build();
    }

    @GetMapping("/booking/{bookingId}")
    public ApiResponse<List<PaymentResponse>> getPaymentHistory(@PathVariable Integer bookingId) {
        List<PaymentResponse> history = paymentService.getPaymentHistoryByBookingId(bookingId);

        return ApiResponse.<List<PaymentResponse>>builder()
                .code(200)
                .message("Lấy lịch sử thanh toán thành công")
                .result(history)
                .build();
    }
    @GetMapping
    public ApiResponse<List<PaymentResponse>> getAllPayments() {
        List<PaymentResponse> allPayments = paymentService.getAllPayments();
        return ApiResponse.<List<PaymentResponse>>builder()
                .code(200)
                .message("Lấy danh sách tất cả giao dịch thành công")
                .result(allPayments)
                .build();
    }

    @PatchMapping("/{paymentId}/cancel")
    public ApiResponse<PaymentResponse> cancelPayment(@PathVariable Integer paymentId) {
        PaymentResponse response = paymentService.cancelPayment(paymentId);
        return ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("Hủy giao dịch thành công")
                .result(response)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> getPaymentDetail(@PathVariable Integer id) {

        return ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("Lấy chi tiết giao dịch thành công")
                .result(paymentService.getPaymentById(id))
                .build();
    }
}