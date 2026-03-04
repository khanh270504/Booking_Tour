package com.example.bookingtour.dtos.response.support;

import lombok.Data;
import java.time.Instant;

@Data
public class ReviewResponse {
    private Integer id;
    private String customerName;
    private String tourName;
    private Integer rating;
    private String comment;
    private Instant createdAt;
}