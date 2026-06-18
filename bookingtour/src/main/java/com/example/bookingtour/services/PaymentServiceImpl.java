package com.example.bookingtour.services;

import com.example.bookingtour.dtos.request.payment.ManualPaymentRequest;
import com.example.bookingtour.dtos.response.payment.PaymentResponse;
import com.example.bookingtour.entities.*;
import com.example.bookingtour.enums.*;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.IServices.IPaymentService;
import com.example.bookingtour.repositories.*;
import com.example.bookingtour.dtos.request.email.BookingEmailEvent;
import com.example.bookingtour.configurations.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
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
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,###");

    @Override
    @Transactional
    public PaymentResponse processManualPayment(ManualPaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }

        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().trim().isEmpty()) {
            var existingPayment = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingPayment.isPresent()) {
                log.warn("RE-SUBMIT detected with IdempotencyKey {}", request.getIdempotencyKey());
                List<Payment> allPayments = paymentRepository.findByBookingId(booking.getId());
                BigDecimal totalPaid = calculateTotalPaidFromList(allPayments);
                BigDecimal remaining = booking.getTotalFinalPrice().subtract(totalPaid).max(BigDecimal.ZERO);
                return PaymentResponse.fromPayment(existingPayment.get(), remaining);
            }
        }

        List<Payment> allPayments = paymentRepository.findByBookingId(booking.getId());
        BigDecimal totalPaidSoFar = calculateTotalPaidFromList(allPayments);

        if (totalPaidSoFar.compareTo(booking.getTotalFinalPrice()) >= 0) {
            throw new AppException(ErrorCode.DUPLICATE_PAYMENT);
        }

        BigDecimal newTotal = totalPaidSoFar.add(request.getAmount());

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(request.getAmount());
        String txCode = request.getTransactionCode();

        if (txCode == null || txCode.trim().isEmpty()) {
            txCode = "BANK-" + booking.getBookingCode() + "-" + System.currentTimeMillis();
        }

        payment.setTransactionCode(txCode);

        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().trim().isEmpty()) {
            payment.setIdempotencyKey(request.getIdempotencyKey());
        } else {
            payment.setIdempotencyKey(generateIdempotencyKey(booking.getId()));
        }

        try {
            payment.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.PAYMENT_METHOD_INVALID);
        }
        payment.setStatus(PaymentStatus.SUCCESS);
        Payment savedPayment = paymentRepository.save(payment);

        updateBookingStatus(booking, newTotal);

        int pointsToAdd = processLoyaltyPoints(booking.getCustomer(), request.getAmount());
        sendPaymentNotifications(booking, request.getAmount(), pointsToAdd, "Kế toán viên", "CRM System");

        BigDecimal remaining = booking.getTotalFinalPrice().subtract(newTotal);
        return PaymentResponse.fromPayment(savedPayment, remaining.max(BigDecimal.ZERO));
    }

    @Override
    @Transactional
    public PaymentResponse processVNPayCallback(Map<String, String> queryParams) {
        String responseCode = queryParams.get("vnp_ResponseCode");
        String txnRef = queryParams.get("vnp_TxnRef");
        String transactionNo = queryParams.get("vnp_TransactionNo");

        Integer bookingId = Integer.parseInt(txnRef.split("_")[0]);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (paymentRepository.existsByTransactionCode(transactionNo)) {
            log.warn("Giao dịch VNPay {} đã được hệ thống xử lý trước đó.", transactionNo);
            List<Payment> allPayments = paymentRepository.findByBookingId(booking.getId());
            BigDecimal totalPaid = calculateTotalPaidFromList(allPayments);
            BigDecimal remaining = booking.getTotalFinalPrice().subtract(totalPaid).max(BigDecimal.ZERO);
            return PaymentResponse.fromPayment(paymentRepository.findByTransactionCode(transactionNo), remaining);
        }

        BigDecimal amount = new BigDecimal(queryParams.get("vnp_Amount")).divide(new BigDecimal("100"));

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setTransactionCode(transactionNo);
        payment.setPaymentMethod(PaymentMethod.VNPAY);
        payment.setIdempotencyKey(txnRef);

        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            Payment savedPayment = paymentRepository.save(payment);

            List<Payment> allPayments = paymentRepository.findByBookingId(booking.getId());
            BigDecimal newTotalPaid = calculateTotalPaidFromList(allPayments);

            updateBookingStatus(booking, newTotalPaid);

            int pointsToAdd = processLoyaltyPoints(booking.getCustomer(), amount);
            sendPaymentNotifications(booking, amount, pointsToAdd, "VNPay Gateway", "VNPay Gateway");

            BigDecimal remaining = booking.getTotalFinalPrice().subtract(newTotalPaid).max(BigDecimal.ZERO);
            return PaymentResponse.fromPayment(savedPayment, remaining);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            Payment savedPayment = paymentRepository.save(payment);

            List<Payment> allPayments = paymentRepository.findByBookingId(booking.getId());
            BigDecimal totalPaid = calculateTotalPaidFromList(allPayments);
            BigDecimal remaining = booking.getTotalFinalPrice().subtract(totalPaid).max(BigDecimal.ZERO);

            log.warn("Giao dịch VNPay thất bại cho Booking ID {}. Mã lỗi: {}", bookingId, responseCode);
            return PaymentResponse.fromPayment(savedPayment, remaining);
        }
    }

    @Override
    @Transactional
    public void cancelAndRefundBooking(Integer bookingId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }

        if (booking.getStatus() == BookingStatus.PENDING) {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }
        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        List<Payment> allPayments = paymentRepository.findByBookingId(booking.getId());
        BigDecimal totalRefunded = BigDecimal.ZERO;

        for (Payment payment : allPayments) {
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                totalRefunded = totalRefunded.add(payment.getAmount());
                payment.setStatus(PaymentStatus.REFUND);
                paymentRepository.save(payment);
            }
        }

        String historyReason = String.format("Khách hủy đặt tour thành công (Đơn chuyển sang CANCELLED). Hệ thống tự động kích hoạt REFUND toàn bộ hóa đơn cũ với tổng tiền: %s VNĐ. Lý do: %s",
                MONEY_FORMAT.format(totalRefunded), reason);

        BookingStatusHistory history = BookingStatusHistory.builder()
                .booking(booking)
                .fromStatus(oldStatus)
                .toStatus(BookingStatus.CANCELLED)
                .reason(historyReason)
                .changedBy("Điều hành viên")
                .build();
        statusHistoryRepository.save(history);

        if (totalRefunded.compareTo(BigDecimal.ZERO) > 0 && booking.getCustomer() != null) {
            CustomerProfile customer = booking.getCustomer();
            int pointsToDeduct = totalRefunded.divide(new BigDecimal("10000")).intValue();
            if (customer.getLoyaltyPoints() != null) {
                customer.setLoyaltyPoints(Math.max(0, customer.getLoyaltyPoints() - pointsToDeduct));
                customerProfileRepository.save(customer);
            }
        }

        sendCancelNotifications(booking, totalRefunded, "BOOKING-CANCEL-AUTOMATIC-REFUND");
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentAmount(Integer paymentId, BigDecimal correctAmount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() == PaymentStatus.FAILED || payment.getStatus() == PaymentStatus.REFUND) {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }

        log.info("Kế toán tiến hành duyệt và điều chỉnh hóa đơn ID {}: Đổi số tiền sang {}đ", paymentId, correctAmount);

        payment.setAmount(correctAmount);
        payment.setStatus(PaymentStatus.SUCCESS);
        Payment updatedPayment = paymentRepository.save(payment);

        Booking booking = updatedPayment.getBooking();
        List<Payment> allPayments = paymentRepository.findByBookingId(booking.getId());
        BigDecimal newTotalPaid = calculateTotalPaidFromList(allPayments);

        updateBookingStatus(booking, newTotalPaid);

        int pointsToAdd = processLoyaltyPoints(booking.getCustomer(), correctAmount);
        sendPaymentNotifications(booking, correctAmount, pointsToAdd, "Kế toán viên", "Financial Adjustment System");

        BigDecimal remaining = booking.getTotalFinalPrice().subtract(newTotalPaid).max(BigDecimal.ZERO);
        return PaymentResponse.fromPayment(updatedPayment, remaining);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentHistoryByBookingId(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        List<Payment> payments = paymentRepository.findByBookingId(bookingId);
        BigDecimal totalPaid = calculateTotalPaidFromList(payments);
        BigDecimal remaining = booking.getTotalFinalPrice().subtract(totalPaid).max(BigDecimal.ZERO);

        return payments.stream()
                .map(p -> PaymentResponse.fromPayment(p, remaining))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        List<Payment> allPayments = paymentRepository.findByBookingId(payment.getBooking().getId());
        BigDecimal totalPaid = calculateTotalPaidFromList(allPayments);
        BigDecimal remaining = payment.getBooking().getTotalFinalPrice().subtract(totalPaid).max(BigDecimal.ZERO);

        return PaymentResponse.fromPayment(payment, remaining);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(payment -> {
                    List<Payment> siblingPayments = paymentRepository.findByBookingId(payment.getBooking().getId());
                    BigDecimal totalPaid = calculateTotalPaidFromList(siblingPayments);
                    BigDecimal remaining = payment.getBooking().getTotalFinalPrice().subtract(totalPaid).max(BigDecimal.ZERO);
                    return PaymentResponse.fromPayment(payment, remaining);
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calculateTotalPaidFromList(List<Payment> payments) {
        return payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void updateBookingStatus(Booking booking, BigDecimal totalPaid) {
        BookingStatus oldStatus = booking.getStatus();
        BookingStatus newStatus;
        if (totalPaid.compareTo(BigDecimal.ZERO) <= 0) {
            newStatus = BookingStatus.PENDING;
        } else if (totalPaid.compareTo(booking.getTotalFinalPrice()) >= 0) {
            newStatus = BookingStatus.CONFIRMED;
        } else {
            newStatus = BookingStatus.PARTIALLY_PAID;
        }

        if (oldStatus != newStatus) {
            booking.setStatus(newStatus);
            bookingRepository.save(booking);

            String textReason = String.format("Dòng tiền biến động tự động dịch chuyển trạng thái từ %s sang %s. (Tổng đã thu: %s VNĐ)",
                    oldStatus, newStatus, MONEY_FORMAT.format(totalPaid));

            BookingStatusHistory history = BookingStatusHistory.builder()
                    .booking(booking)
                    .fromStatus(oldStatus)
                    .toStatus(newStatus)
                    .reason(textReason)
                    .changedBy("System - Financial Ledger")
                    .build();

            statusHistoryRepository.save(history);
            log.info("Booking Code {} nhảy trạng thái lý tưởng: {} -> {}", booking.getBookingCode(), oldStatus, newStatus);
        }
    }

    private int processLoyaltyPoints(CustomerProfile customer, BigDecimal amount) {
        if (customer == null || amount.compareTo(BigDecimal.ZERO) <= 0) return 0;

        int pointsToAdd = amount.divide(new BigDecimal("10000")).intValue();
        if (pointsToAdd > 0) {
            int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
            customer.setLoyaltyPoints(currentPoints + pointsToAdd);
            customerProfileRepository.save(customer);
            log.info("Đã cộng thêm +{} điểm thành viên cho khách hàng ID {}", pointsToAdd, customer.getId());
        }
        return pointsToAdd;
    }

    private void sendPaymentNotifications(Booking booking, BigDecimal amount, int pointsToAdd, String userCreator, String sysCreator) {
        try {
            String formattedAmount = MONEY_FORMAT.format(amount);
            String msg = String.format("Hệ thống đã ghi nhận khoản thanh toán %s VNĐ cho đơn đặt tour %s.\nTrạng thái đơn hiện tại: %s.",
                    formattedAmount, booking.getBookingCode(), booking.getStatus());
            if (pointsToAdd > 0) {
                msg += String.format(" Bạn được cộng thêm +%d điểm tích lũy!", pointsToAdd);
            }

            String emailSubject = "[TravelVN] Xác nhận giao dịch tài chính thành công - Đơn hàng " + booking.getBookingCode();
            BookingEmailEvent emailEvent = BookingEmailEvent.builder()
                    .type("PAYMENT_SUCCESS")
                    .toEmail(booking.getContactEmail())
                    .subject(emailSubject)
                    .content(msg)
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, emailEvent);

            if (booking.getCustomer() != null && booking.getCustomer().getUser() != null) {
                notificationRepository.save(Notification.builder()
                        .title("Thanh toán thành công!")
                        .message(msg)
                        .type(NotificationType.SUCCESS)
                        .user(booking.getCustomer().getUser())
                        .createdBy(userCreator)
                        .build());
            }

            List<User> receivers = userRepository.findAdminAndKetoan();
            if (receivers != null && !receivers.isEmpty()) {
                String adminMsg = String.format("Đã cập nhật giao dịch trị giá %s VNĐ cho đơn %s của khách %s.\nTrạng thái đơn: %s.",
                        formattedAmount, booking.getBookingCode(), booking.getContactName(), booking.getStatus());
                for (User receiver : receivers) {
                    notificationRepository.save(Notification.builder()
                            .title("Dòng tiền thanh toán mới")
                            .message(adminMsg)
                            .type(NotificationType.SUCCESS)
                            .user(receiver)
                            .createdBy(sysCreator)
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("Hệ thống thông báo gặp lỗi: {}", e.getMessage());
        }
    }

    private void sendCancelNotifications(Booking booking, BigDecimal amount, String txnCode) {
        try {
            String formattedAmount = MONEY_FORMAT.format(amount);
            String cancelMsg = String.format("Đơn hàng %s đã được hủy thành công. Hệ thống tiến hành hoàn trả %s VNĐ lại cho quý khách.",
                    booking.getBookingCode(), formattedAmount);

            String emailSubject = "[TravelVN] Thông báo hủy tour và hoàn tiền đơn hàng " + booking.getBookingCode();
            BookingEmailEvent emailEvent = BookingEmailEvent.builder()
                    .type("PAYMENT_SUCCESS")
                    .toEmail(booking.getContactEmail())
                    .subject(emailSubject)
                    .content(cancelMsg)
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, emailEvent);

            if (booking.getCustomer() != null && booking.getCustomer().getUser() != null) {
                notificationRepository.save(Notification.builder()
                        .title("Hủy tour & Hoàn tiền thành công")
                        .message(cancelMsg)
                        .type(NotificationType.WARNING)
                        .user(booking.getCustomer().getUser())
                        .createdBy("Hệ thống tài chính")
                        .build());
            }

            List<User> receivers = userRepository.findAdminAndKetoan();
            if (receivers != null && !receivers.isEmpty()) {
                String adminCancelMsg = String.format("Đơn hàng %s của khách %s vừa được hủy vĩnh viễn. Hệ thống đã tự động đẩy trạng thái toàn bộ hóa đơn thành công về REFUND. Tổng hoàn: %s VNĐ.",
                        booking.getBookingCode(), booking.getContactName(), formattedAmount);
                for (User receiver : receivers) {
                    notificationRepository.save(Notification.builder()
                            .title("Thông báo: Hủy đơn và Hoàn tiền thành công")
                            .message(adminCancelMsg)
                            .type(NotificationType.ERROR)
                            .user(receiver)
                            .createdBy("CRM System")
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("Lỗi bắn thông báo kiểm toán hủy: {}", e.getMessage());
        }
    }

    private String generateIdempotencyKey(Integer bookingId) {
        return UUID.randomUUID().toString() + "-B" + bookingId;
    }
}