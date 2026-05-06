package com.example.bookingtour.dtos.response.payment;

import com.example.bookingtour.entities.Booking;
import com.example.bookingtour.entities.Payment;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PaymentResponse {
    private Integer id;
    private Integer bookingId;

    private String bookingCode;

    private String customerEmail;

    private BigDecimal amount;

    private String paymentMethod;

    private String transactionCode;

    private String status;

    private String idempotencyKey;

    private BigDecimal remainingAmount;

    private Instant createdAt;

    public static PaymentResponse fromPayment(Payment payment, BigDecimal remainingAmount) {
        Booking booking = payment.getBooking();

        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(booking.getId())
                .bookingCode(booking.getBookingCode())
                .customerEmail(booking.getContactEmail())

                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().name())
                .transactionCode(payment.getTransactionCode())
                .status(payment.getStatus().name())
                .idempotencyKey(payment.getIdempotencyKey())

                .remainingAmount(remainingAmount)

                .createdAt(payment.getCreatedAt())
                .build();
    }

}

