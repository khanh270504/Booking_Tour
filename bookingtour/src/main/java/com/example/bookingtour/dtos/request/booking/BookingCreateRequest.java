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

    // --- Class con (Inner Class) để định nghĩa cấu trúc 1 hành khách ---
    @Data
    public static class PassengerRequest {

        @NotBlank(message = "Tên hành khách không được để trống")
        private String fullName;

        // "ADULT" hoặc "CHILD"
        @NotBlank(message = "Loại hành khách không được để trống")
        private String passengerType;

        private String gender;

        
        @NotBlank
        private String birthDate;
    }
}