package com.example.bookingtour.dtos.request.sales;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class VoucherCreateRequest {
    @NotBlank(message = "Mã giảm giá không được để trống")
    private String code;

    @NotBlank(message = "Loại giảm giá không được để trống")
    private String discountType; // PERCENT, FIXED_AMOUNT

    @NotNull
    @Min(value = 0, message = "Giá trị giảm không được âm")
    private BigDecimal discountValue;

    @Min(value = 1, message = "Lượt sử dụng tối thiểu là 1")
    private Integer maxUsage;

    @NotNull(message = "Ngày hết hạn không được để trống")
    @Future(message = "Ngày hết hạn phải ở tương lai")
    private Instant expiryDate;
}