package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.payment.ManualPaymentRequest;
import com.example.bookingtour.dtos.response.payment.PaymentResponse;

import java.util.List;
import java.util.Map;

public interface IPaymentService {

    PaymentResponse processManualPayment(ManualPaymentRequest request);

    PaymentResponse updatePaymentAmount(Integer paymentId, java.math.BigDecimal correctAmount);

    PaymentResponse processVNPayCallback(Map<String, String> queryParams);

    void cancelAndRefundBooking(Integer bookingId, String reason);

    List<PaymentResponse> getPaymentHistoryByBookingId(Integer bookingId);

    PaymentResponse getPaymentById(Integer paymentId);

    List<PaymentResponse> getAllPayments();
}