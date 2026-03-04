package com.example.bookingtour.dtos.request.tour;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ScheduleCreateRequest {
    @NotNull(message = "Thiếu ID Tour")
    private Integer tourId;

    @NotNull(message = "Ngày khởi hành không được để trống")
    @Future(message = "Ngày khởi hành phải ở tương lai")
    private LocalDate departureDate;

    private LocalDate returnDate;

    @NotNull(message = "Phải nhập số chỗ mở bán")
    private Integer availableSlots;
}