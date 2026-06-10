package com.example.bookingtour.services;

import com.example.bookingtour.dtos.request.payment.ManualPaymentRequest;
import com.example.bookingtour.dtos.response.payment.PaymentResponse;
import com.example.bookingtour.entities.Booking;
import com.example.bookingtour.entities.BookingStatusHistory;
import com.example.bookingtour.entities.CustomerProfile;
import com.example.bookingtour.entities.Payment;
import com.example.bookingtour.enums.BookingStatus;
import com.example.bookingtour.enums.PaymentMethod;
import com.example.bookingtour.enums.PaymentStatus;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.IServices.IPaymentService;
import com.example.bookingtour.repositories.BookingRepository;
import com.example.bookingtour.repositories.BookingStatusHistoryRepository;
import com.example.bookingtour.repositories.CustomerProfileRepository;
import com.example.bookingtour.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements IPaymentService {
    private final CustomerProfileRepository customerProfileRepository;
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

        updateBookingStatus(booking, newTotal);

        CustomerProfile customer = booking.getCustomer();
        if (customer != null) {
            int pointsToAdd = request.getAmount().divide(new BigDecimal("10000")).intValue();
            if (pointsToAdd > 0) {
                int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
                customer.setLoyaltyPoints(currentPoints + pointsToAdd);
                customerProfileRepository.save(customer);
                log.info("Đã cộng {} điểm cho khách hàng ID {}", pointsToAdd, customer.getId());
            }
        }

        BigDecimal remaining = booking.getTotalFinalPrice().subtract(newTotal);
        return PaymentResponse.fromPayment(savedPayment, remaining.max(BigDecimal.ZERO));
    }

    // 🎯 HÀM MỚI: XỬ LÝ KẾT QUẢ TRẢ VỀ TỪ VNPAY CỰC KỲ BÀI BẢN
    @Override
    @Transactional
    public PaymentResponse processVNPayCallback(Map<String, String> queryParams) {
        String responseCode = queryParams.get("vnp_ResponseCode");
        String txnRef = queryParams.get("vnp_TxnRef");
        String transactionNo = queryParams.get("vnp_TransactionNo");

//        if (txnRef == null || transactionNo == null) {
//            throw new AppException(ErrorCode.INVALID_PAYMENT_DATA);
//        }

        Integer bookingId = Integer.parseInt(txnRef.split("_")[0]);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (paymentRepository.existsByTransactionCode(transactionNo)) {
            log.warn("Giao dịch VNPay {} đã được hệ thống xử lý trước đó.", transactionNo);
            BigDecimal totalPaid = getTotalPaid(booking);
            BigDecimal remaining = booking.getTotalFinalPrice().subtract(totalPaid).max(BigDecimal.ZERO);
            return PaymentResponse.fromPayment(paymentRepository.findByTransactionCode(transactionNo), remaining);
        }

        // 3. Quy đổi số tiền từ VNPay về giá trị thực thực tế (VNPay nhân 100 gửi về)
        BigDecimal amount = new BigDecimal(queryParams.get("vnp_Amount")).divide(new BigDecimal("100"));

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setTransactionCode(transactionNo);
        payment.setPaymentMethod(PaymentMethod.VNPAY); // Chỗ này ông sửa lại theo đúng tên Enum VNPAY của ông nhé
        payment.setIdempotencyKey(txnRef);

        // 4. Kiểm tra mã phản hồi thành công ("00" là thành công)
        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            Payment savedPayment = paymentRepository.save(payment);

            // Tính toán tổng tiền thực tế sau khi cộng thêm khoản online mới này
            BigDecimal newTotalPaid = getTotalPaid(booking);
            updateBookingStatus(booking, newTotalPaid);

            // Tự động cộng điểm loyalty cho khách hàng
            CustomerProfile customer = booking.getCustomer();
            if (customer != null) {
                int pointsToAdd = amount.divide(new BigDecimal("10000")).intValue();
                if (pointsToAdd > 0) {
                    int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
                    customer.setLoyaltyPoints(currentPoints + pointsToAdd);
                    customerProfileRepository.save(customer);
                    log.info("VNPay: Đã tự động cộng {} điểm cho khách hàng ID {}", pointsToAdd, customer.getId());
                }
            }

            BigDecimal remaining = booking.getTotalFinalPrice().subtract(newTotalPaid).max(BigDecimal.ZERO);
            return PaymentResponse.fromPayment(savedPayment, remaining);
        } else {
            // Trường hợp khách huỷ thanh toán hoặc lỗi thẻ ngân hàng
            payment.setStatus(PaymentStatus.FAILED);
            Payment savedPayment = paymentRepository.save(payment);

            BigDecimal totalPaid = getTotalPaid(booking);
            BigDecimal remaining = booking.getTotalFinalPrice().subtract(totalPaid).max(BigDecimal.ZERO);
            log.warn("Giao dịch VNPay thất bại hoặc bị hủy cho Booking ID {}. Mã lỗi: {}", bookingId, responseCode);
            return PaymentResponse.fromPayment(savedPayment, remaining);
        }
    }

    @Override
    public List<PaymentResponse> getPaymentHistoryByBookingId(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        List<Payment> payments = paymentRepository.findByBookingId(bookingId);

        BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = booking.getTotalFinalPrice().subtract(totalPaid).max(BigDecimal.ZERO);

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

        BigDecimal remainingPaid = getTotalPaidExcluding(booking, paymentId);
        updateBookingStatus(booking, remainingPaid);

        paymentRepository.save(payment);

        BigDecimal remainingAmount = booking.getTotalFinalPrice().subtract(remainingPaid).max(BigDecimal.ZERO);
        return PaymentResponse.fromPayment(payment, remainingAmount);
    }

    @Override
    public PaymentResponse getPaymentById(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        BigDecimal totalPaid = getTotalPaid(payment.getBooking());
        BigDecimal remaining = payment.getBooking().getTotalFinalPrice().subtract(totalPaid).max(BigDecimal.ZERO);

        return PaymentResponse.fromPayment(payment, remaining);
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

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(payment -> {
                    BigDecimal totalPaid = getTotalPaid(payment.getBooking());
                    BigDecimal remaining = payment.getBooking().getTotalFinalPrice().subtract(totalPaid).max(BigDecimal.ZERO);
                    return PaymentResponse.fromPayment(payment, remaining);
                })
                .collect(Collectors.toList());
    }
}