package com.example.bookingtour.dtos.request.operation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TourCostRequest {
    @NotNull(message = "Thiếu ID lịch trình")
    private Integer scheduleId;

    @NotNull(message = "Thiếu ID nhà cung cấp")
    private Integer providerId;

    @NotBlank(message = "Tên chi phí không được để trống")
    private String expenseName;

    @NotNull
    @DecimalMin(value = "0.0", message = "Số tiền không được âm")
    private BigDecimal amount;
}