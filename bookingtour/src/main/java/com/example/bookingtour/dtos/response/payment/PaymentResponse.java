package com.example.bookingtour.dtos.response.payment;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PaymentResponse {
    private Integer id;
    private Integer bookingId;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionCode;
    private String status;
    private Instant createdAt;
}