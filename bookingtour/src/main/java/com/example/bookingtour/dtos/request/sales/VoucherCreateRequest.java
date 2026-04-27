package com.example.bookingtour.dtos.request.sales;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class VoucherCreateRequest {

    @NotBlank(message = "Mã giảm giá không được để trống")
    @Size(max = 50, message = "Mã giảm giá không được vượt quá 50 ký tự")
    private String code;

    @NotBlank(message = "Loại giảm giá không được để trống")
    private String discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", inclusive = true, message = "Giảm tối đa không được âm")
    private BigDecimal maxDiscountAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Đơn tối thiểu không được âm")
    private BigDecimal minOrderValue;

    @NotNull(message = "Số lượt sử dụng không được để trống")
    @Min(value = 1, message = "Lượt sử dụng tối thiểu là 1")
    private Integer maxUsage;

    @NotNull(message = "Ngày hết hạn không được để trống")
    @Future(message = "Ngày hết hạn phải ở tương lai")
    private Instant expiryDate;
}