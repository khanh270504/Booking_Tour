package com.example.bookingtour.dtos.response.tour;

import lombok.Data;

@Data
public class TourImageResponse {
    private Integer id;
    private Integer tourId;
    private String imageUrl;
}