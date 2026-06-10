package com.example.bookingtour.dtos.response.booking;

import com.example.bookingtour.entities.Review;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ReviewResponse {

    private Integer id;

    private Integer bookingId;
    private Integer tourId;

    private String customerName;

    private Integer rating;
    private String comment;

    private String status;

    private Instant createdAt;

    private String adminReply;
    private Instant repliedAt;

    public static ReviewResponse fromReview(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBooking() != null
                        ? review.getBooking().getId()
                                : null
                )
                .tourId(review.getTour() != null
                                ? review.getTour().getId()
                                : null
                )
                .customerName(review.getBooking() != null
                                ? review.getBooking().getContactName()
                                : null
                )
                .rating(review.getRating())
                .comment(review.getComment())

                .status(review.getStatus() != null
                                ? review.getStatus().name()
                                : null
                )
                .createdAt(review.getCreatedAt())

                .adminReply(review.getAdminReply())
                .repliedAt(review.getRepliedAt())
                .build();
    }
}