package com.example.bookingtour.dtos.response.booking;

import com.example.bookingtour.entities.BookingStatusHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingStatusHistoryResponse {
    private String fromStatus;
    private String toStatus;
    private String reason;
    private String changedBy;
    private Instant createdAt;
    public static BookingStatusHistoryResponse fromHistory(BookingStatusHistory history) {
        if (history == null) {
            return null;
        }

        return BookingStatusHistoryResponse.builder()
                .fromStatus(history.getFromStatus() != null ? history.getFromStatus().name() : null)
                .toStatus(history.getToStatus() != null ? history.getToStatus().name() : null)
                .reason(history.getReason())
                .changedBy(history.getChangedBy())
                .createdAt(history.getCreatedAt())
                .build();
    }
}