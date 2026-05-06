package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IReviewService;
import com.example.bookingtour.dtos.request.booking.AdminReplyRequest;
import com.example.bookingtour.dtos.request.booking.ReviewCreateRequest;
import com.example.bookingtour.dtos.response.booking.ReviewResponse;
import com.example.bookingtour.entities.Booking;
import com.example.bookingtour.entities.CustomerProfile;
import com.example.bookingtour.entities.Review;
import com.example.bookingtour.enums.BookingStatus;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.BookingRepository;
import com.example.bookingtour.repositories.ReviewRepository; // 🎯 Đã xóa CustomerProfileRepository vì không còn cần thiết
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

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

        Review review = new Review();
        review.setBooking(booking);
        review.setUser(profile.getUser());
        review.setTour(booking.getSchedule().getTour());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        reviewRepository.save(review);

        return ReviewResponse.fromReview(review);
    }

    @Override
    public List<ReviewResponse> getReviewsByTour(Integer tourId, Integer rating) {
        List<Review> reviews = reviewRepository.findByTourWithFilter(tourId, rating);

        return reviews.stream()
                .map(ReviewResponse::fromReview)
                .collect(Collectors.toList());
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
}