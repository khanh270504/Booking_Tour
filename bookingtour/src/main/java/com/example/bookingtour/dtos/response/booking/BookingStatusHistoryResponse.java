package com.example.bookingtour.dtos.response.booking;

import lombok.Data;
import java.time.Instant;

@Data
public class BookingStatusHistoryResponse {
    private String fromStatus;
    private String toStatus;
    private String reason;
    private String changedBy; // Tên người thực hiện đổi trạng thái
    private Instant createdAt;
}