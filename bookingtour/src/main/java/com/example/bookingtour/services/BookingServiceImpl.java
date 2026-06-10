package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IBookingService;
import com.example.bookingtour.dtos.internal.PricingResultDto;
import com.example.bookingtour.dtos.request.booking.BookingCancelRequest;
import com.example.bookingtour.dtos.request.booking.BookingCreateRequest;
import com.example.bookingtour.dtos.response.booking.BookingResponse;
import com.example.bookingtour.dtos.response.booking.BookingStatusHistoryResponse;
import com.example.bookingtour.dtos.response.booking.PassengerResponse;
import com.example.bookingtour.dtos.response.payment.PaymentResponse;
import com.example.bookingtour.entities.*;
import com.example.bookingtour.enums.BookingStatus;
import com.example.bookingtour.enums.PassengerType;
import com.example.bookingtour.enums.PaymentStatus;
import com.example.bookingtour.enums.ScheduleStatus;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.*;
import com.example.bookingtour.services.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements IBookingService {
    private final PaymentRepository paymentRepository;

    private final UserRepository userRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final BookingPassengerRepository bookingPassengerRepository;
    private final BookingRepository bookingRepository;
    private final CustomerProfileRepository profileRepository;
    private final PricingServiceImpl pricingService;
    private final BookingStatusHistoryRepository statusHistoryRepository;
    private final VoucherRepository voucherRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request, Integer userInternalId) {
        log.info("--- Processing Booking for: {} ---", request.getContactInfo().getEmail());

        int quantity = request.getPassengers().size();
        TourSchedule schedule = tourScheduleRepository.findByIdForUpdate(request.getScheduleId())
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (schedule.getAvailableSlots() < quantity) {
            throw new AppException(ErrorCode.TOUR_FULL);
        }

        User actor = (userInternalId != null) ? userRepository.findById(userInternalId).orElse(null) : null;

        String contactEmail = request.getContactInfo().getEmail();
        CustomerProfile customerProfile = profileRepository.findByEmail(contactEmail).orElse(null);

        if (customerProfile == null) {
            customerProfile = CustomerProfile.builder()
                    .email(contactEmail)
                    .fullName(request.getContactInfo().getFullName()) // Lưu tên chính thống lần đầu
                    .phone(request.getContactInfo().getPhone())
                    .build();
            profileRepository.save(customerProfile);
            log.info("CRM: Đã ghi nhận khách hàng mới: {}", contactEmail);
        } else {
            if (customerProfile.getPhone() == null || customerProfile.getPhone().isEmpty()) {
                customerProfile.setPhone(request.getContactInfo().getPhone());
                customerProfile.setEmail(request.getContactInfo().getEmail());
                profileRepository.save(customerProfile);
            }
            log.info("CRM: Khách cũ {} quay lại đặt tour", contactEmail);
        }


        Voucher appliedVoucher = null;
        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            appliedVoucher = voucherRepository.findByCode(request.getVoucherCode())
                    .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

            if (appliedVoucher.getUsageCount() >= appliedVoucher.getMaxUsage()) {
                throw new AppException(ErrorCode.VOUCHER_OUT_OF_STOCK);
            }

            long userUsageCount = bookingRepository.countByVoucherIdAndContactEmail(
                    appliedVoucher.getId(), contactEmail
            );
            if (userUsageCount >= appliedVoucher.getMaxUsagePerUser()) {
                log.warn("VOUCHER BLOCK: Khách {} cố dùng mã {} vượt giới hạn {} lần/người",
                        contactEmail, appliedVoucher.getCode(), appliedVoucher.getMaxUsagePerUser());
                throw new AppException(ErrorCode.VOUCHER_OUT_OF_STOCK);
            }
        }


        PricingResultDto pricing = pricingService.calculatePrice(
                schedule.getId(),
                request.getPassengers(),
                request.getVoucherCode(),
                schedule.getTour().getId()
        );

        schedule.setAvailableSlots(schedule.getAvailableSlots() - quantity);
        if (schedule.getAvailableSlots() == 0) {
            schedule.setStatus(ScheduleStatus.FULL);
        }
        tourScheduleRepository.save(schedule);
        log.info("DEBUG TRƯỚC KHI LƯU: Voucher object là: {}", appliedVoucher);

        Booking booking = Booking.builder()
                .bookingCode(generateCode("BK"))
                .createdBy(actor)
                .customer(customerProfile)
                .schedule(schedule)
                .status(BookingStatus.PENDING)
                .tourNameSnapshot(schedule.getTour() != null ? schedule.getTour().getName() : "N/A")
                .departureDateSnapshot(schedule.getDepartureDate())
                .departureLocationSnapshot(schedule.getTour().getDestination().getName())

                .voucher(appliedVoucher) // Gắn Voucher vào Đơn

                .contactName(request.getContactInfo().getFullName())
                .contactPhone(request.getContactInfo().getPhone())
                .contactEmail(contactEmail)
                .note(request.getNote())
                .totalOriginalPrice(pricing.getTotalOriginalPrice())
                .totalSurcharge(pricing.getTotalSurcharge())
                .totalDiscount(pricing.getTotalDiscount())
                .totalFinalPrice(pricing.getTotalFinalPrice())
                .build();

        bookingRepository.save(booking);
        log.info("DEBUG SAU KHI LƯU: Booking ID {} có Voucher ID là: {}",
                booking.getId(),
                booking.getVoucher() != null ? booking.getVoucher().getId() : "NULL");
        String changedByActor = (actor != null) ? actor.getEmail() : "Guest (" + contactEmail + ")";
        saveStatusHistory(
                booking,
                null,
                BookingStatus.PENDING,
                "Hệ thống ghi nhận đơn hàng mới từ khách hàng.",
                changedByActor
        );


        if (appliedVoucher != null) {
            appliedVoucher.setUsageCount(appliedVoucher.getUsageCount() + 1);
            voucherRepository.save(appliedVoucher); // LUÔN LUÔN LƯU BẤT CHẤP maxUsagePerUser LÀ BAO NHIÊU

            log.info("VOUCHER SUCESS: Đã áp dụng mã {}. Tiến trình: {}/{}",
                    appliedVoucher.getCode(),
                    appliedVoucher.getUsageCount(),
                    appliedVoucher.getMaxUsage());
        }


        List<BookingPassenger> savedPassengers = request.getPassengers().stream().map(pReq -> {
            PassengerType type = pReq.getPassengerType();

            return bookingPassengerRepository.save(BookingPassenger.builder()
                    .booking(booking)
                    .fullName(pReq.getFullName())
                    .birthDate(pReq.getBirthDate())
                    .gender(pReq.getGender().toUpperCase())
                    .passengerType(type)
                    .build());
        }).collect(Collectors.toList());

        log.info("=> [SUCCESS] Booking {} created by {}", booking.getBookingCode(),
                actor != null ? actor.getEmail() : "Guest");
        emailService.sendBookingEmail(
                request.getContactInfo().getEmail(),
                booking.getBookingCode(),
                schedule.getTour().getName(),
                request.getContactInfo().getFullName(),
                request.getContactInfo().getPhone()


        );
        return BookingResponse.fromBooking(booking, savedPassengers);
    }

    @Override
    public BookingResponse getBookingById(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        List<BookingPassenger> passengers = bookingPassengerRepository.findByBookingId(bookingId);
        List<BookingStatusHistory> histories = statusHistoryRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
        List<Payment> payments = paymentRepository.findByBookingId(bookingId);

        BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = booking.getTotalFinalPrice().subtract(totalPaid).max(BigDecimal.ZERO);
        BookingResponse response = BookingResponse.fromBooking(booking, passengers);
        response.setPayments(payments.stream()
                .map(p -> PaymentResponse.fromPayment(p, remaining))
                .toList());

        response.setStatusHistories(histories.stream()
                .map(BookingStatusHistoryResponse::fromHistory)
                .toList());

        return response;
    }

    @Override
    public List<BookingResponse> getBookingsByUser(Integer userInternalId) {
        // 1. Lấy thông tin User hiện tại để lấy Email
        User currentUser = userRepository.findById(userInternalId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String email = currentUser.getEmail();

        return bookingRepository.findByContactEmailOrderByCreatedAtDesc(email).stream()
                .map(b -> {
                    List<BookingPassenger> ps = bookingPassengerRepository.findByBookingId(b.getId());
                    return BookingResponse.fromBooking(b, ps);
                })
                .toList();
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(BookingCancelRequest request, Integer currentUserId) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getCustomer() == null ||
                booking.getCustomer().getUser() == null ||
                !booking.getCustomer().getUser().getId().equals(currentUserId)) {
            log.warn("User {} cố tình hủy đơn hàng {} không thuộc sở hữu!", currentUserId, booking.getId());
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }

        updateInventory(booking.getSchedule(), bookingPassengerRepository.findByBookingId(booking.getId()).size());

        if (booking.getVoucher() != null) {
            Voucher v = booking.getVoucher();
            v.setUsageCount(Math.max(0, v.getUsageCount() - 1));
            voucherRepository.save(v);
            log.info("Đã hoàn lại lượt sử dụng cho Voucher: {}", v.getCode());
        }

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setVoucher(null);
        bookingRepository.save(booking);

        saveStatusHistory(booking, oldStatus, BookingStatus.CANCELLED, "Người dùng yêu cầu hủy", "User ID: " + currentUserId);

        return getBookingById(booking.getId());
    }


    private void saveStatusHistory(Booking booking, BookingStatus from, BookingStatus to, String reason, String changedBy) {
        statusHistoryRepository.save(BookingStatusHistory.builder()
                .booking(booking)
                .fromStatus(from)
                .toStatus(to)
                .reason(reason)
                .changedBy(changedBy)
                .build());
    }

    private String generateCode(String prefix) {
        return prefix + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")) + "-" + (System.currentTimeMillis() % 1000);
    }

    private void updateInventory(TourSchedule schedule, int delta) {
        int newSlots = schedule.getAvailableSlots() + delta;
        if (newSlots < 0) throw new AppException(ErrorCode.TOUR_FULL);
        schedule.setAvailableSlots(newSlots);
        schedule.setStatus(newSlots == 0 ? ScheduleStatus.FULL : ScheduleStatus.OPENING);
        tourScheduleRepository.save(schedule);
    }

    @Override
    public BookingResponse lookupBooking(String bookingCode, String email) {
        Booking booking = bookingRepository.findByBookingCodeAndContactEmail(bookingCode, email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã đơn hàng hoặc Email không khớp!"));

        List<BookingPassenger> passengers = bookingPassengerRepository.findByBookingId(booking.getId());

        return BookingResponse.fromBooking(booking, passengers);
    }
    @Override
    public List<BookingResponse> getAllBookingsForAdmin() {
        log.info("Admin đang lấy toàn bộ danh sách Đơn hàng");

        return bookingRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(
                        Booking::getCreatedAt,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())
                ))
                .map(booking -> {
                    // Cần load đủ passengers và payments để Frontend tính toán paymentStatus
                    List<BookingPassenger> passengers = bookingPassengerRepository.findByBookingId(booking.getId());
                    List<Payment> payments = paymentRepository.findByBookingId(booking.getId());

                    BookingResponse response = BookingResponse.fromBooking(booking, passengers);

                    // Tính toán tiền đã trả để nhét vào response
                    BigDecimal totalPaid = payments.stream()
                            .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                            .map(Payment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal remaining = booking.getTotalFinalPrice().subtract(totalPaid).max(BigDecimal.ZERO);

                    response.setPayments(payments.stream()
                            .map(p -> PaymentResponse.fromPayment(p, remaining))
                            .toList());

                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponse updateBookingStatus(Integer bookingId, String status, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        BookingStatus oldStatus = booking.getStatus();
        BookingStatus newStatus = BookingStatus.valueOf(status.toUpperCase());

        if (oldStatus == newStatus) {
            return getBookingById(bookingId);
        }

        int passengerCount = bookingPassengerRepository.findByBookingId(bookingId).size();

        if (newStatus == BookingStatus.CANCELLED) {

            updateInventory(booking.getSchedule(), passengerCount);
            if (booking.getVoucher() != null) {
                Voucher v = booking.getVoucher();
                v.setUsageCount(Math.max(0, v.getUsageCount() - 1));
                voucherRepository.save(v);
                log.info("Đã hoàn lại lượt sử dụng cho Voucher: {}", v.getCode());
            }

        } else if (oldStatus == BookingStatus.CANCELLED && (newStatus == BookingStatus.PENDING || newStatus == BookingStatus.CONFIRMED)) {
            if (booking.getSchedule().getAvailableSlots() < passengerCount) {
                throw new AppException(ErrorCode.TOUR_FULL);
            }
            updateInventory(booking.getSchedule(), -passengerCount);
        }

        booking.setStatus(newStatus);
        bookingRepository.save(booking);
        saveStatusHistory(booking, oldStatus, newStatus, reason, "ADMIN");

        return getBookingById(bookingId);
    }

    @Override
    public List<PassengerResponse> getPassengersByScheduleId(Integer scheduleId) {
        List<BookingPassenger> passengers = bookingPassengerRepository.findPassengersByScheduleId(scheduleId);

        if (passengers == null || passengers.isEmpty()) {
            return Collections.emptyList();
        }
        return passengers.stream()
                .map(PassengerResponse::fromPassenger)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void cancelAllBookingsBySchedule(Integer scheduleId, String reason) {
        // 1. Tìm tất cả đơn hàng KHÔNG PHẢI trạng thái CANCELLED thuộc lịch trình này
        List<Booking> bookings = bookingRepository.findByScheduleId(scheduleId).stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .collect(Collectors.toList());

        log.info("Bắt đầu hủy tập thể {} đơn hàng cho lịch trình ID: {}", bookings.size(), scheduleId);

        for (Booking booking : bookings) {
            int passengerCount = bookingPassengerRepository.findByBookingId(booking.getId()).size();

            updateInventory(booking.getSchedule(), passengerCount);

            if (booking.getVoucher() != null) {
                Voucher v = booking.getVoucher();
                v.setUsageCount(Math.max(0, v.getUsageCount() - 1));
                voucherRepository.save(v);
            }

            BookingStatus oldStatus = booking.getStatus();
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setVoucher(null);
            bookingRepository.save(booking);
            saveStatusHistory(booking, oldStatus, BookingStatus.CANCELLED, reason, "ADMIN_SYSTEM");

            log.info("Đã hủy đơn hàng: {} thuộc lịch trình {}", booking.getBookingCode(), scheduleId);
        }
    }

}
