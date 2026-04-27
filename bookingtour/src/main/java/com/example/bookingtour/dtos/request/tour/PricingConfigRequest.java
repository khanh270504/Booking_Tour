package com.example.bookingtour.dtos.request.tour;

import com.example.bookingtour.enums.PassengerType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PricingConfigRequest {

    @NotNull(message = "Giá phải thuộc về một lịch trình cụ thể (Schedule ID)")
    private Integer scheduleId;

    @NotNull(message = "Loại hành khách (Người lớn/Trẻ em...) không được để trống")
    private PassengerType passengerType;

    @NotNull(message = "Giá tiền không được để trống")
    @DecimalMin(value = "0.0", message = "Giá không được âm")
    private BigDecimal price;

    @NotBlank(message = "Đơn vị tiền tệ không được để trống")
    private String currency = "VND";

}