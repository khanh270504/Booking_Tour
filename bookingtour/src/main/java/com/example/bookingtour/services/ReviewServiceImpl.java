package com.example.bookingtour.services; // Giữ nguyên package theo file của ông

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
import com.example.bookingtour.repositories.CustomerProfileRepository;
import com.example.bookingtour.repositories.ReviewRepository;
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
    CustomerProfileRepository profileRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request, String userInternalId) {
        Integer userId = Integer.parseInt(userInternalId);
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        if (!booking.getUser().getId().equals(userId)) {
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
        review.setUser(booking.getUser());
        review.setTour(booking.getSchedule().getTour());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        reviewRepository.save(review);

        String customerName = profileRepository.findByUser_Id(userId)
                .map(CustomerProfile::getFullName)
                .orElse("Khách hàng ẩn danh");

        return ReviewResponse.fromReview(review, customerName);
    }

    @Override
    public List<ReviewResponse> getReviewsByTour(Integer tourId, Integer rating) {
        List<Review> reviews = reviewRepository.findByTourWithFilter(tourId, rating);

        return reviews.stream().map(review -> {
            String customerName = profileRepository.findByUser_Id(review.getUser().getId())
                    .map(CustomerProfile::getFullName)
                    .orElse("Khách hàng ẩn danh");
            return ReviewResponse.fromReview(review, customerName);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewResponse replyToReview(Integer reviewId, AdminReplyRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (review.getAdminReply() != null) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_REPLIED); // Báo lỗi: Đã có câu trả lời
        }

        review.setAdminReply(request.getReply());
        review.setRepliedAt(Instant.now());

        reviewRepository.save(review);

        String customerName = profileRepository.findByUser_Id(review.getUser().getId())
                .map(CustomerProfile::getFullName)
                .orElse("Khách hàng ẩn danh");

        return ReviewResponse.fromReview(review, customerName);
    }
}