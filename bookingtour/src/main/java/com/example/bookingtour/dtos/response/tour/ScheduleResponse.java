package com.example.bookingtour.dtos.response.tour;

import com.example.bookingtour.entities.TourSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleResponse {
    private Integer id;
    private Integer tourId;
    private String tourName;
    private String scheduleCode;
    private LocalDateTime departureDate;
    private LocalDateTime returnDate;
    private String departureLocation;
    private Integer maxSlots;
    private Integer availableSlots;
    private String status;

    private List<PricingConfigResponse> pricings;
    private List<SurchargeResponse> surcharges;

    public static ScheduleResponse fromSchedule(TourSchedule schedule) {
        if (schedule == null) return null;

        return ScheduleResponse.builder()
                .id(schedule.getId())
                .tourId(schedule.getTour() != null ? schedule.getTour().getId() : null)
                .tourName(schedule.getTour() != null ? schedule.getTour().getName() : null)
                .scheduleCode(schedule.getScheduleCode())
                .departureDate(schedule.getDepartureDate())
                .returnDate(schedule.getReturnDate())
                .departureLocation(schedule.getDepartureLocation())
                .maxSlots(schedule.getMaxSlots())
                .availableSlots(schedule.getAvailableSlots())
                .status(schedule.getStatus() != null ? schedule.getStatus().name() : null)
                .build();
    }
}