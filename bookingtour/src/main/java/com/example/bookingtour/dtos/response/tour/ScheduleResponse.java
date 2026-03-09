package com.example.bookingtour.dtos.response.tour;

import com.example.bookingtour.entities.TourSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleResponse {
    private Integer id;

    private Integer tourId;

    private String tourName;

    private LocalDateTime departureDate;

    private LocalDateTime returnDate;

    private Integer availableSlots;

    private String status;

    public static ScheduleResponse fromSchedule(TourSchedule schedule) {
        if (schedule == null) return null;

        return ScheduleResponse.builder()
                .id(schedule.getId())

                .tourId(schedule.getTour() != null ? schedule.getTour().getId() : null)
                .tourName(schedule.getTour() != null ? schedule.getTour().getName() : null)

                .departureDate(schedule.getDepartureDate())
                .returnDate(schedule.getReturnDate())
                .availableSlots(schedule.getAvailableSlots())

                .status(schedule.getStatus() != null ? schedule.getStatus().name() : null)
                .build();
    }
}