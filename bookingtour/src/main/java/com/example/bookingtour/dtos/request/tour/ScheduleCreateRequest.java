package com.example.bookingtour.dtos.request.tour;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ScheduleCreateRequest {

    @NotNull(message = "Thiếu ID Tour")
    private Integer tourId;

    @NotBlank(message = "Nơi khởi hành không được để trống")
    private String departureLocation;

    @NotNull(message = "Ngày giờ khởi hành không được để trống")
    @Future(message = "Ngày giờ khởi hành phải ở tương lai")
    private LocalDate departureDate;

    @NotNull(message = "Ngày giờ về không được để trống")
    @Future(message = "Ngày giờ về phải ở tương lai")
    private LocalDate returnDate;

    @NotNull(message = "Phải nhập tổng số chỗ tối đa")
    @Min(value = 1, message = "Tổng số chỗ phải lớn hơn 0")
    private Integer maxSlots;
    @Valid
    private List<PricingConfigRequest> pricings;

    @Valid
    private List<SurchargeRequest> surcharges;
}