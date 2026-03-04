package com.example.bookingtour.dtos.request.tour;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class DestinationRequest {
    @NotBlank(message = "Tên địa điểm không được để trống")
    private String name;

    private String description;
}