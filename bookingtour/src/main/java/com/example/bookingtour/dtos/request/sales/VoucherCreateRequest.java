package com.example.bookingtour.dtos.request.sales;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class VoucherCreateRequest {

    @NotBlank(message = "Tên voucher không được để trống")
    @Size(max = 255)
    private String title;

    private String description;

    @NotBlank(message = "Mã giảm giá không được để trống")
    @Size(max = 50)
    private String code;

    @NotBlank(message = "Loại giảm giá không được để trống")
    private String discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal maxDiscountAmount;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal minOrderValue;

    @NotNull(message = "Số lượt sử dụng không được để trống")
    @Min(value = 1)
    private Integer maxUsage;

    @Min(value = 1)
    private Integer maxUsagePerUser;

    private Instant startDate;

    @NotNull(message = "Ngày hết hạn không được để trống")
    @Future(message = "Ngày hết hạn phải ở tương lai")
    private Instant expiryDate;

    private Boolean isActive = true;

    private Integer tourId;

    private Integer userId;
}