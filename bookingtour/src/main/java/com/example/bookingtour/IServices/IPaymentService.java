package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.payment.ManualPaymentRequest;
import com.example.bookingtour.entities.Payment;

import java.util.List;

public interface IPaymentService {
    // 1. Tạo thanh toán thủ công
    Payment processManualPayment(ManualPaymentRequest request);

    // 2. Lấy toàn bộ lịch sử thanh toán của 1 đơn hàng (Dùng cho FE hiển thị lịch sử)
    List<Payment> getPaymentHistoryByBookingId(Integer bookingId);

    // 3. Lấy chi tiết 1 giao dịch (Dùng khi click vào xem chi tiết hóa đơn)
    Payment getPaymentById(Integer paymentId);

    // 4. (Mở rộng) Hủy hoặc hoàn tiền giao dịch
    Payment cancelPayment(Integer paymentId);
}