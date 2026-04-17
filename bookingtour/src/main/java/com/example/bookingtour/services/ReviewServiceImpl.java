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

        // 1. Kiểm tra Booking có tồn tại không
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // 2. Chốt chặn 1: Check quyền sở hữu đơn hàng
        if (!booking.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        // 3. Chốt chặn 2: Đã đi Tour xong chưa?
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new AppException(ErrorCode.REVIEW_NOT_ALLOWED);
        }

        // 4. Chốt chặn 3: Đã Rate rồi thì không cho Rate nữa
        if (reviewRepository.existsByBooking(booking)) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // 5. Khởi tạo và Lưu DB
        Review review = new Review();
        review.setBooking(booking);
        review.setUser(booking.getUser()); // Lưu thẳng User vào (như Entity yêu cầu)
        review.setTour(booking.getSchedule().getTour()); // Rút Tour ra từ Schedule
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        // Thời gian (createdAt) sẽ tự động được Spring gán nhờ @CreationTimestamp
        reviewRepository.save(review);

        // Lấy tên khách từ CustomerProfile để trả về FE cho đẹp
        String customerName = profileRepository.findByUser_Id(userId)
                .map(CustomerProfile::getFullName)
                .orElse("Khách hàng ẩn danh");

        return ReviewResponse.fromReview(review, customerName);
    }

    @Override
    public List<ReviewResponse> getReviewsByTour(Integer tourId, Integer rating) {
        // Sử dụng hàm có Hỗ trợ Lọc và Sắp xếp Mới nhất lên đầu
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
        // 1. Tìm cái Review gốc
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        // 2. Chốt chặn: Chỉ cho phép trả lời 1 lần
        if (review.getAdminReply() != null) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_REPLIED); // Báo lỗi: Đã có câu trả lời
        }

        // 3. Cập nhật dữ liệu
        review.setAdminReply(request.getReply());
        review.setRepliedAt(Instant.now()); // Lấy giờ chuẩn UTC hiện tại

        reviewRepository.save(review);

        // Lấy lại tên khách để trả về Response
        String customerName = profileRepository.findByUser_Id(review.getUser().getId())
                .map(CustomerProfile::getFullName)
                .orElse("Khách hàng ẩn danh");

        return ReviewResponse.fromReview(review, customerName);
    }
}