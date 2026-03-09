package com.example.bookingtour.dtos.request.tour;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScheduleCreateRequest {

    @NotNull(message = "Thiếu ID Tour")
    private Integer tourId;

    @NotNull(message = "Ngày giờ khởi hành không được để trống")
    @Future(message = "Ngày giờ khởi hành phải ở tương lai")
    private LocalDateTime departureDate; // Đổi sang LocalDateTime

    @NotNull(message = "Ngày giờ về không được để trống")
    @Future(message = "Ngày giờ về phải ở tương lai")
    private LocalDateTime returnDate;

    @NotNull(message = "Phải nhập tổng số chỗ ")
    @Min(value = 1, message = "Tổng số chỗ phải lớn hơn 0")
    private Integer availableSlots;

    // --- CÁC TRƯỜNG TÙY CHỌN (Nên cân nhắc thêm) ---
    // private Integer guideId; // ID của hướng dẫn viên (nếu đã chốt)
    // private Integer minParticipants; // Số người tối thiểu để tour khởi hành
}