package com.example.bookingtour.controllers;

import com.example.bookingtour.dtos.request.payment.ManualPaymentRequest;
import com.example.bookingtour.dtos.response.ApiResponse;
import com.example.bookingtour.dtos.response.payment.PaymentResponse;
import com.example.bookingtour.IServices.IPaymentService;
import com.example.bookingtour.services.VNPayService; // Import thêm cái này
import jakarta.servlet.http.HttpServletRequest; // Import thêm cái này
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;
    private final VNPayService vnPayService; // Inject thêm VNPayService vào đây

    @GetMapping("/vnpay/create-url")
    public ApiResponse<Map<String, String>> createVNPayPayment(
            @RequestParam Long bookingId,
            @RequestParam long amount,
            HttpServletRequest request) {

        String paymentUrl = vnPayService.createPaymentUrl(bookingId, amount, request);
        return ApiResponse.<Map<String, String>>builder()
                .code(200)
                .message("Tạo đường dẫn thanh toán VNPay thành công")
                .result(Map.of("paymentUrl", paymentUrl))
                .build();
    }

    @GetMapping("/vnpay/callback")
    public ResponseEntity<Void> vnpayCallback(@RequestParam Map<String, String> queryParams) {
        PaymentResponse response = paymentService.processVNPayCallback(queryParams);

        String txnRef = queryParams.get("vnp_TxnRef");
        String bookingId = txnRef.split("_")[0];
        String responseCode = queryParams.get("vnp_ResponseCode");
        String status = "00".equals(responseCode) ? "success" : "failed";

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://localhost:5173/payment/" + bookingId + "?status=" + status));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
    @PutMapping("/{id}/amount")
    public ApiResponse<PaymentResponse> updatePaymentAmount(
            @PathVariable Integer id,
            @RequestParam BigDecimal correctAmount) {

        PaymentResponse response = paymentService.updatePaymentAmount(id, correctAmount);
        return ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("Điều chỉnh số tiền giao dịch thành công")
                .result(response)
                .build();
    }

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


    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> getPaymentDetail(@PathVariable Integer id) {
        return ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("Lấy chi tiết giao dịch thành công")
                .result(paymentService.getPaymentById(id))
                .build();
    }


}