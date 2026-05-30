package com.example.bookingtour.dtos.request.operation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourCostRequest {

    @NotNull(message = "Thiếu thông tin Lịch trình (Schedule ID)")
    private Integer scheduleId;

    @NotNull(message = "Thiếu thông tin Nhà cung cấp (Provider ID)")
    private Integer providerId;

    @NotBlank(message = "Tên khoản chi không được để trống")
    private String expenseName;

    @NotNull(message = "Số tiền không được để trống")
    @Min(value = 0, message = "Số tiền phải lớn hơn hoặc bằng 0")
    private BigDecimal amount;

    @NotBlank(message = "Trạng thái thanh toán không được để trống")
    private String status;

    private String note;

    private Instant paidAt;
}