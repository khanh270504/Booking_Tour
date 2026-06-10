package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.IReviewService;
import com.example.bookingtour.dtos.request.booking.AdminReplyRequest;
import com.example.bookingtour.dtos.request.booking.ReviewCreateRequest;
import com.example.bookingtour.dtos.response.booking.ReviewResponse;
import com.example.bookingtour.dtos.response.booking.TourRatingResponse;
import com.example.bookingtour.dtos.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {

    IReviewService reviewService;


    @PostMapping
    public ApiResponse<ReviewResponse> createReview(@RequestBody @Valid ReviewCreateRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userInternalId = jwt.getClaimAsString("userId");

        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.createReview(request, userInternalId))
                .message("Cảm ơn bạn đã đánh giá chuyến đi!")
                .build();
    }

    @GetMapping("/tour/{tourId}")
    public ApiResponse<Page<ReviewResponse>> getReviewsByTour(
            @PathVariable Integer tourId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ApiResponse.<Page<ReviewResponse>>builder()
                .result(reviewService.getReviewsByTour(tourId, rating, page, size))
                .message("Lấy danh sách đánh giá thành công")
                .build();
    }

    @GetMapping("/tour/{tourId}/rating")
    public ApiResponse<TourRatingResponse> getTourRatingStats(@PathVariable Integer tourId) {
        return ApiResponse.<TourRatingResponse>builder()
                .result(reviewService.getTourRatingStats(tourId))
                .message("Lấy thống kê đánh giá thành công")
                .build();
    }

    @PostMapping("/admin/{reviewId}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReviewResponse> replyReview(
            @PathVariable Integer reviewId,
            @RequestBody @Valid AdminReplyRequest request) {

        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.replyToReview(reviewId, request))
                .message("Trả lời đánh giá thành công!")
                .build();
    }

    @PutMapping("/admin/{reviewId}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> hideReview(@PathVariable Integer reviewId) {
        reviewService.hideReview(reviewId);

        return ApiResponse.<Void>builder()
                .message("Đã ẩn đánh giá vi phạm thành công!")
                .build();
    }
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<ReviewResponse>> getAllReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.<Page<ReviewResponse>>builder()
                .result(reviewService.getAllReviewsForAdmin(page, size))
                .message("Lấy tất cả danh sách đánh giá cho Admin thành công")
                .build();
    }
}