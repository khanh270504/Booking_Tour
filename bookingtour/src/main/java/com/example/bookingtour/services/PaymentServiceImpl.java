package com.example.bookingtour.services;

import com.example.bookingtour.dtos.request.payment.ManualPaymentRequest;
import com.example.bookingtour.entities.Booking;
import com.example.bookingtour.entities.Payment;
import com.example.bookingtour.enums.BookingStatus; // Mở comment cái này
import com.example.bookingtour.enums.PaymentMethod;
import com.example.bookingtour.enums.PaymentStatus;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.IServices.IPaymentService;
import com.example.bookingtour.repositories.BookingRepository;
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
@Slf4j // Thêm cái này để log cho sướng
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public Payment processManualPayment(ManualPaymentRequest request) {
        // 1. Tìm đơn hàng
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // 2. Chặn nếu đơn đã hủy
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }

        // 3. Tính toán số tiền tích lũy
        List<Payment> existingPayments = paymentRepository.findByBookingId(booking.getId());
        BigDecimal totalPaidSoFar = existingPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Kiểm tra xem đã trả đủ từ trước chưa
        if (totalPaidSoFar.compareTo(booking.getTotalFinalPrice()) >= 0) {
            throw new AppException(ErrorCode.DUPLICATE_PAYMENT); // Hoặc tạo ErrorCode: ALREADY_PAID_FULL
        }

        // Kiểm tra xem lần này nạp vào có bị thừa tiền không (tùy ông có muốn chặn không)
        BigDecimal newTotal = totalPaidSoFar.add(request.getAmount());
        if (newTotal.compareTo(booking.getTotalFinalPrice()) > 0) {
            log.warn("Khách thanh toán thừa! Đơn: {}, Thừa: {}", booking.getId(), newTotal.subtract(booking.getTotalFinalPrice()));
            // Ông có thể throw lỗi ở đây nếu muốn bắt chuyển chính xác
        }

        // 5. Khởi tạo Payment bản ghi mới
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(request.getAmount());

        try {
            payment.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.PAYMENT_METHOD_INVALID);
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionCode(request.getTransactionCode());
        payment.setIdempotencyKey(UUID.randomUUID().toString() + "-B" + booking.getId());

        // 6. Lưu giao dịch thanh toán
        Payment savedPayment = paymentRepository.save(payment);

        // 7. Cập nhật trạng thái Booking dựa trên tổng tiền mới
        if (newTotal.compareTo(booking.getTotalFinalPrice()) >= 0) {
            booking.setStatus(BookingStatus.PAID);
            log.info("=> Đơn hàng {} đã thanh toán ĐỦ.", booking.getId());
        } else {
            booking.setStatus(BookingStatus.PARTIALLY_PAID); // Nhớ thêm enum này nhé
            log.info("=> Đơn hàng {} đã thanh toán MỘT PHẦN (Cọc).", booking.getId());
        }

        bookingRepository.save(booking);

        return savedPayment;
    }

    @Override
    public List<Payment> getPaymentHistoryByBookingId(Integer bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
        }
        return paymentRepository.findByBookingId(bookingId);
    }

    @Override
    public Payment getPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    @Override
    @Transactional
    public Payment cancelPayment(Integer paymentId) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() == PaymentStatus.FAILED) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_CANCELLED);
        }

        payment.setStatus(PaymentStatus.FAILED);

        // Sau khi hủy 1 bản ghi payment, phải tính lại tổng tiền còn lại để hạ cấp trạng thái Booking
        Booking booking = payment.getBooking();
        List<Payment> activePayments = paymentRepository.findByBookingId(booking.getId());
        BigDecimal remainingPaid = activePayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS && !p.getId().equals(paymentId))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (remainingPaid.compareTo(BigDecimal.ZERO) == 0) {
            booking.setStatus(BookingStatus.PENDING);
        } else if (remainingPaid.compareTo(booking.getTotalFinalPrice()) < 0) {
            booking.setStatus(BookingStatus.PARTIALLY_PAID);
        }

        bookingRepository.save(booking);
        return paymentRepository.save(payment);
    }
}