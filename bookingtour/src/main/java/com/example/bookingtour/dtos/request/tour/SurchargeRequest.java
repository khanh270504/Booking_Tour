package com.example.bookingtour.dtos.request.tour;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SurchargeRequest {
    @NotNull(message = "Thiếu ID Tour")
    private Integer tourId;

    @NotBlank(message = "Tên phụ thu không được để trống")
    private String surchargeName;

    @NotNull
    @DecimalMin(value = "0.0", message = "Số tiền không được âm")
    private BigDecimal amount;

    @NotNull
    private Boolean isMandatory; // Bắt buộc hay Tùy chọn
}