package com.example.bookingtour.dtos.response.booking;

import com.example.bookingtour.entities.Review;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;


@Data
@Builder
public class ReviewResponse {
    private Integer id;
    private String customerName;
    private Integer rating;
    private String comment;
    private Instant createdAt;
    private String adminReply;
    private Instant repliedAt;

    public static ReviewResponse fromReview(Review review, String customerName) {
        return ReviewResponse.builder()
                .id(review.getId())
                .customerName(customerName)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .adminReply(review.getAdminReply())
                .repliedAt(review.getRepliedAt())
                .build();
    }
}