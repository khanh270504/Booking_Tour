package com.example.bookingtour.dtos.request.tour;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class TourCreateRequest {

    @NotNull(message = "Phải chọn điểm đến")
    private Integer destinationId;

    @NotBlank(message = "Tên tour không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Lịch trình không được để trống")
    private List<Map<String, Object>> itinerary;

    private String thumbnail;

    private Integer minParticipants;

    private String duration;

    private String tourCode;
}