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

    @NotNull(message = "Số tiền đơn hàng không được trống")
    private BigDecimal orderTotal;

    private Integer tourId;

}