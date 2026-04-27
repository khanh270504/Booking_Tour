package com.example.bookingtour.dtos.request.sales;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VoucherApplyRequest {

    @NotBlank(message = "Vui lòng nhập mã giảm giá")
    private String code;

    @NotNull(message = "Tổng tiền giỏ hàng không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tổng tiền phải lớn hơn 0")
    private BigDecimal orderTotal;

    private Integer tourId;

    private Integer customerId;
}