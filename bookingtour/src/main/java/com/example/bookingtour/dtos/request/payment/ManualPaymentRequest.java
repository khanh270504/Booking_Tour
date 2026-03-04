package com.example.bookingtour.dtos.request.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ManualPaymentRequest {
    @NotNull(message = "Thiếu ID đơn hàng")
    private Integer bookingId;

    @NotNull
    @DecimalMin(value = "0.0", message = "Số tiền không được âm")
    private BigDecimal amount;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod; // CASH, BANK_TRANSFER

    private String transactionCode;
    private String note;
}