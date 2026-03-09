package com.example.bookingtour.dtos.request.tour;

import com.example.bookingtour.enums.PassengerType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PricingConfigRequest {
    @NotNull(message = "Thiếu ID Tour")
    private Integer tourId;

    @NotNull(message = "Loại hành khách không được để trống")
    private PassengerType passengerType;

    @NotNull
    @DecimalMin(value = "0.0", message = "Giá không được âm")
    private BigDecimal price;

    private String currency;

    @NotNull(message = "Ngày áp dụng không được để trống")
    private LocalDate effectiveDate;
}