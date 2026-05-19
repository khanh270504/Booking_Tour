package com.example.bookingtour.services;

import com.example.bookingtour.dtos.request.payment.ManualPaymentRequest;
import com.example.bookingtour.dtos.response.payment.PaymentResponse;
import com.example.bookingtour.entities.Booking;
import com.example.bookingtour.entities.BookingStatusHistory;
import com.example.bookingtour.entities.Payment;
import com.example.bookingtour.enums.BookingStatus;
import com.example.bookingtour.enums.PaymentMethod;
import com.example.bookingtour.enums.PaymentStatus;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.IServices.IPaymentService;
import com.example.bookingtour.repositories.BookingRepository;
import com.example.bookingtour.repositories.BookingStatusHistoryRepository;
import com.example.bookingtour.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingStatusHistoryRepository statusHistoryRepository;
    @Override
    @Transactional
    public PaymentResponse processManualPayment(ManualPaymentRequest request) {
        // 1. Kiểm tra đơn hàng
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }

        // 2. Tính toán tiền đã trả trước đó để chặn nếu đã đủ
        BigDecimal totalPaidSoFar = getTotalPaid(booking);

        if (totalPaidSoFar.compareTo(booking.getTotalFinalPrice()) >= 0) {
            throw new AppException(ErrorCode.DUPLICATE_PAYMENT);
        }

        // 3. Xử lý thanh toán mới
        BigDecimal newTotal = totalPaidSoFar.add(request.getAmount());

        if (newTotal.compareTo(booking.getTotalFinalPrice()) > 0) {
            log.warn("Thanh toán thừa: booking={}, thừa={}",
                    booking.getId(),
                    newTotal.subtract(booking.getTotalFinalPrice()));
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(request.getAmount());
        payment.setTransactionCode(request.getTransactionCode());
        payment.setIdempotencyKey(generateIdempotencyKey(booking.getId()));

        try {
            payment.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.PAYMENT_METHOD_INVALID);
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        Payment savedPayment = paymentRepository.save(payment);

        // 4. Cập nhật trạng thái Booking dựa trên tổng tích lũy
        updateBookingStatus(booking, newTotal);

        // 5. Tính số tiền còn lại và trả về Response DTO
        BigDecimal remaining = booking.getTotalFinalPrice().subtract(newTotal);

        return PaymentResponse.fromPayment(savedPayment, remaining.max(BigDecimal.ZERO));
    }

    @Override
    public List<PaymentResponse> getPaymentHistoryByBookingId(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        List<Payment> payments = paymentRepository.findByBookingId(bookingId);

        // Tính tổng đã trả (Live) để đảm bảo remainingAmount chính xác
        BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = booking.getTotalFinalPrice().subtract(totalPaid).max(BigDecimal.ZERO);

        // Map danh sách Entity sang danh sách DTO
        return payments.stream()
                .map(p -> PaymentResponse.fromPayment(p, remaining))
                .toList();
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() == PaymentStatus.FAILED) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_CANCELLED);
        }

        payment.setStatus(PaymentStatus.FAILED);
        Booking booking = payment.getBooking();

        // Tính lại tiền sau khi loại bỏ khoản thanh toán bị hủy
        BigDecimal remainingPaid = getTotalPaidExcluding(booking, paymentId);
        updateBookingStatus(booking, remainingPaid);

        paymentRepository.save(payment);

        BigDecimal remainingAmount = booking.getTotalFinalPrice().subtract(remainingPaid).max(BigDecimal.ZERO);
        return PaymentResponse.fromPayment(payment, remainingAmount);
    }

    @Override
    public Payment getPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
    }


    private BigDecimal getTotalPaid(Booking booking) {
        return paymentRepository.findByBookingId(booking.getId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getTotalPaidExcluding(Booking booking, Integer excludePaymentId) {
        return paymentRepository.findByBookingId(booking.getId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS && !p.getId().equals(excludePaymentId))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void updateBookingStatus(Booking booking, BigDecimal totalPaid) {
        BookingStatus oldStatus = booking.getStatus();
        BookingStatus newStatus;

        if (totalPaid.compareTo(booking.getTotalFinalPrice()) >= 0) {
            newStatus = BookingStatus.CONFIRMED;
        } else {
            newStatus = BookingStatus.PENDING;
        }

        if (oldStatus != newStatus) {
            booking.setStatus(newStatus);
            bookingRepository.save(booking);

            // 🎯 GHI SỔ VÀO HISTORY CHO CÁI TIMELINE NÓ ĐỌC
            BookingStatusHistory history = BookingStatusHistory.builder()
                    .booking(booking)
                    .fromStatus(oldStatus)
                    .toStatus(newStatus)
                    .reason("Hệ thống tự động cập nhật: Khách đã thanh toán đủ tiền.")
                    .changedBy("System - Payment")
                    .build();

            statusHistoryRepository.save(history);

            log.info("Booking ID {} đã chuyển trạng thái từ {} sang {}", booking.getId(), oldStatus, newStatus);
        }
    }

    private String generateIdempotencyKey(Integer bookingId) {
        return UUID.randomUUID().toString() + "-B" + bookingId;
    }
}