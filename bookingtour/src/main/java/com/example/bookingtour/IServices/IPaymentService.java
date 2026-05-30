package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.payment.ManualPaymentRequest;
import com.example.bookingtour.dtos.response.payment.PaymentResponse;
import com.example.bookingtour.entities.Payment;

import java.util.List;

public interface IPaymentService {
    // 1. Tạo thanh toán thủ công
    PaymentResponse processManualPayment(ManualPaymentRequest request);

    // 2. Lấy toàn bộ lịch sử thanh toán của 1 đơn hàng (Dùng cho FE hiển thị lịch sử)
    List<PaymentResponse> getPaymentHistoryByBookingId(Integer bookingId);

    // 3. Lấy chi tiết 1 giao dịch (Dùng khi click vào xem chi tiết hóa đơn)
    PaymentResponse getPaymentById(Integer paymentId);

    // 4. (Mở rộng) Hủy hoặc hoàn tiền giao dịch
    PaymentResponse cancelPayment(Integer paymentId);

    List<PaymentResponse> getAllPayments();
}