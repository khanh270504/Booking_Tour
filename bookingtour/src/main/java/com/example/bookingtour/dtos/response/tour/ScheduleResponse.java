package com.example.bookingtour.dtos.response.tour;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ScheduleResponse {
    private Integer id;


    private Integer tourId;


    private String tourName;

    private LocalDate departureDate;

    private LocalDate returnDate;

    private Integer availableSlots;

    private String status;
}