package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.booking.AdminReplyRequest;
import com.example.bookingtour.dtos.request.booking.ReviewCreateRequest;
import com.example.bookingtour.dtos.response.booking.ReviewResponse;
import com.example.bookingtour.dtos.response.booking.TourRatingResponse;
import org.springframework.data.domain.Page;

public interface IReviewService {

    ReviewResponse createReview(ReviewCreateRequest request, String userInternalId);

    Page<ReviewResponse> getReviewsByTour(Integer tourId, Integer rating, int page, int size);
    Page<ReviewResponse> getAllReviewsForAdmin(int page, int size);
    TourRatingResponse getTourRatingStats(Integer tourId);

    ReviewResponse replyToReview(Integer reviewId, AdminReplyRequest request);

    void hideReview(Integer reviewId);
}