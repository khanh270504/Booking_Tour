package com.example.bookingtour.dtos.request.booking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingCancelRequest {
    @NotNull(message = "Thiếu ID đơn hàng")
    private Integer bookingId;

    @NotBlank(message = "Phải nhập lý do hủy")
    private String reason;
}