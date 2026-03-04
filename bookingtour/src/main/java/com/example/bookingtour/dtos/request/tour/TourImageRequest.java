package com.example.bookingtour.dtos.request.tour;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TourImageRequest {
    @NotNull
    private Integer tourId;

    @NotBlank(message = "URL ảnh không được để trống")
    private String imageUrl;
}