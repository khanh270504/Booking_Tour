package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IReviewService;
import com.example.bookingtour.dtos.request.booking.AdminReplyRequest;
import com.example.bookingtour.dtos.request.booking.ReviewCreateRequest;
import com.example.bookingtour.dtos.response.booking.ReviewResponse;
import com.example.bookingtour.dtos.response.booking.TourRatingResponse;
import com.example.bookingtour.entities.Booking;
import com.example.bookingtour.entities.CustomerProfile;
import com.example.bookingtour.entities.Review;
import com.example.bookingtour.enums.BookingStatus;
import com.example.bookingtour.enums.ReviewStatus;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.BookingRepository;
import com.example.bookingtour.repositories.ReviewRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewServiceImpl implements IReviewService {

    ReviewRepository reviewRepository;
    BookingRepository bookingRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request, String userInternalId) {
        Integer userId = Integer.parseInt(userInternalId);

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        CustomerProfile profile = booking.getCustomer();
        if (profile == null || profile.getUser() == null || !profile.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new AppException(ErrorCode.REVIEW_NOT_ALLOWED);
        }

        if (reviewRepository.existsByBooking(booking)) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.builder()
                .booking(booking)
                .user(profile.getUser())
                .tour(booking.getSchedule().getTour())
                .rating(request.getRating())
                .comment(request.getComment())
                .status(ReviewStatus.ACTIVE)
                .build();

        reviewRepository.save(review);

        return ReviewResponse.fromReview(review);
    }

    @Override
    public Page<ReviewResponse> getReviewsByTour(Integer tourId, Integer rating, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Page<Review> reviews = reviewRepository.findByTourAndStatusWithFilter(tourId, ReviewStatus.ACTIVE, rating, pageable);

        return reviews.map(ReviewResponse::fromReview);
    }

    @Override
    public TourRatingResponse getTourRatingStats(Integer tourId) {
        Double avgRating = reviewRepository.getAverageRatingByTourId(tourId);
        Integer reviewCount = reviewRepository.countActiveReviewsByTourId(tourId);

        double finalAvg = (avgRating != null) ? Math.round(avgRating * 10.0) / 10.0 : 0.0;
        int finalCount = (reviewCount != null) ? reviewCount : 0;

        return new TourRatingResponse(finalAvg, finalCount);
    }

    @Override
    @Transactional
    public ReviewResponse replyToReview(Integer reviewId, AdminReplyRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (review.getAdminReply() != null) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_REPLIED);
        }

        review.setAdminReply(request.getReply());
        review.setRepliedAt(Instant.now());

        reviewRepository.save(review);

        return ReviewResponse.fromReview(review);
    }

    @Override
    @Transactional
    public void hideReview(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        review.setStatus(ReviewStatus.HIDDEN);
        reviewRepository.save(review);
    }
    @Override
    public Page<ReviewResponse> getAllReviewsForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findAll(pageable);

        return reviews.map(ReviewResponse::fromReview);
    }

}