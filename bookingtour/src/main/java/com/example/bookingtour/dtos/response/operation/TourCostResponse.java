package com.example.bookingtour.dtos.response.operation;

import com.example.bookingtour.entities.TourCost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourCostResponse {
    private Integer id;

    // Thông tin Schedule
    private Integer scheduleId;
    private String tourName;
    private String scheduleName;

    // Thông tin Provider
    private Integer providerId;
    private String providerName;
    private String providerCode;

    // Thông tin Chi tiết khoản chi
    private String expenseName;
    private BigDecimal amount;
    private String status;
    private String note;
    private String paidAt;
    private String createdAt;

    public static TourCostResponse fromEntity(TourCost entity) {
        if (entity == null) return null;

        return TourCostResponse.builder()
                .id(entity.getId())
                .scheduleId(entity.getSchedule() != null ? entity.getSchedule().getId() : null)
                .tourName((entity.getSchedule() != null && entity.getSchedule().getTour() != null)
                        ? entity.getSchedule().getTour().getName() : "Không xác định")

                .providerId(entity.getProvider() != null ? entity.getProvider().getId() : null)
                .providerName(entity.getProvider() != null ? entity.getProvider().getName() : null)
                .providerCode(entity.getProvider() != null ? entity.getProvider().getProviderCode() : null)

                .expenseName(entity.getExpenseName())
                .amount(entity.getAmount())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .note(entity.getNote())
                .paidAt(entity.getPaidAt() != null ? entity.getPaidAt().toString() : null)
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null)
                .build();
    }
}