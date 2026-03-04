package com.example.bookingtour.dtos.request.booking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class BookingCreateRequest {

    @NotNull(message = "Vui lòng chọn lịch khởi hành")
    private Integer scheduleId;

    private String voucherCode; // Có thể null

    // Dùng List để hứng thông tin chi tiết từng người đi
    @NotEmpty(message = "Danh sách hành khách không được để trống")
    private List<PassengerRequest> passengers;


}