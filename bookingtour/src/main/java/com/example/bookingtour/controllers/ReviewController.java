package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.IReviewService;
import com.example.bookingtour.dtos.request.booking.AdminReplyRequest;
import com.example.bookingtour.dtos.request.booking.ReviewCreateRequest;
import com.example.bookingtour.dtos.response.booking.ReviewResponse;
import com.example.bookingtour.dtos.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews") // Endpoint thường để ở root luôn cho dễ gọi
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
    public ApiResponse<List<ReviewResponse>> getReviewsByTour(
            @PathVariable Integer tourId,
            @RequestParam(required = false) Integer rating) { // Bổ sung tham số rating ở đây

        return ApiResponse.<List<ReviewResponse>>builder()
                .result(reviewService.getReviewsByTour(tourId, rating))
                .message("Lấy danh sách đánh giá thành công")
                .build();
    }

    @PostMapping("/admin/{reviewId}/reply")
    public ApiResponse<ReviewResponse> replyReview(
            @PathVariable Integer reviewId,
            @RequestBody @Valid AdminReplyRequest request) {

        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.replyToReview(reviewId, request))
                .message("Trả lời đánh giá thành công!")
                .build();
    }
}