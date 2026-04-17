package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.booking.AdminReplyRequest;
import com.example.bookingtour.dtos.request.booking.ReviewCreateRequest;
import com.example.bookingtour.dtos.response.booking.ReviewResponse;

import java.util.List;

public interface IReviewService {

    ReviewResponse createReview(ReviewCreateRequest request, String userInternalId);

    List<ReviewResponse> getReviewsByTour(Integer tourId, Integer rating);

    ReviewResponse replyToReview(Integer reviewId, AdminReplyRequest request);
}