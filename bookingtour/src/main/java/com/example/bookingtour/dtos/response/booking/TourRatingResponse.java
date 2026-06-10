package com.example.bookingtour.dtos.response.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourRatingResponse {
    private Double averageRating;
    private Integer totalReviews;
}