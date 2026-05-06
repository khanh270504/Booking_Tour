package com.example.bookingtour.dtos.request.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BookingCreateRequest {

    @NotNull(message = "Vui lòng chọn lịch khởi hành")
    @Min(value = 1, message = "scheduleId không hợp lệ")
    private Integer scheduleId;

    private String voucherCode;

    @Valid
    @NotNull(message = "Thông tin người liên hệ không được để trống")
    private ContactInfoRequest contactInfo;

    @NotEmpty(message = "Danh sách hành khách không được để trống")
    private List<PassengerRequest> passengers;

    private String note;
}