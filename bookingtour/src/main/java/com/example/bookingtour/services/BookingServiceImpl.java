package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IBookingService;
import com.example.bookingtour.IServices.IPaymentService;
import com.example.bookingtour.configurations.RabbitMQConfig;
import com.example.bookingtour.dtos.internal.PricingResultDto;
import com.example.bookingtour.dtos.request.booking.BookingCancelRequest;
import com.example.bookingtour.dtos.request.booking.BookingCreateRequest;
import com.example.bookingtour.dtos.request.email.BookingEmailEvent;
import com.example.bookingtour.dtos.response.booking.BookingResponse;
import com.example.bookingtour.dtos.response.booking.BookingStatusHistoryResponse;
import com.example.bookingtour.dtos.response.booking.PassengerResponse;
import com.example.bookingtour.dtos.response.payment.PaymentResponse;
import com.example.bookingtour.entities.*;
import com.example.bookingtour.enums.*;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.*;
import com.example.bookingtour.services.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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
    private final NotificationRepository notificationRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final IPaymentService paymentService;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request, Integer userInternalId) {
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
                    .fullName(request.getContactInfo().getFullName())
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

        Booking booking = Booking.builder()
                .bookingCode(generateCode("BK"))
                .createdBy(actor)
                .customer(customerProfile)
                .schedule(schedule)
                .status(BookingStatus.PENDING)
                .tourNameSnapshot(schedule.getTour() != null ? schedule.getTour().getName() : "N/A")
                .departureDateSnapshot(schedule.getDepartureDate())
                .departureLocationSnapshot(schedule.getTour().getDestination().getName())
                .voucher(appliedVoucher)
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
        if (request.getNote() != null && request.getNote().contains("CRM")) {
            Payment pendingPayment = new Payment();
            pendingPayment.setBooking(booking);
            pendingPayment.setAmount(booking.getTotalFinalPrice());
            pendingPayment.setStatus(PaymentStatus.PENDING);
            pendingPayment.setTransactionCode("PENDING-CRM-" + booking.getBookingCode());
            pendingPayment.setIdempotencyKey(UUID.randomUUID().toString() + "-CRM-INIT-B" + booking.getId());
            pendingPayment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
            paymentRepository.save(pendingPayment);

            log.info("CRM FLOW: Đã tự động tạo hóa đơn PENDING trị giá {} VNĐ cho đơn hàng CRM {}",
                    booking.getTotalFinalPrice(), booking.getBookingCode());
        } else {
            log.info("WEB FLOW: Khách tự đặt tour ngoài giao diện Web. Không sinh Payment PENDING trước.");
        }

        log.info("CRM FLOW: Đã tự động tạo hóa đơn PENDING trị giá {} VNĐ cho đơn hàng thật {}",
                booking.getTotalFinalPrice(), booking.getBookingCode());
        String changedByActor = (actor != null) ? actor.getEmail() : "Guest (" + contactEmail + ")";
        saveStatusHistory(booking, null, BookingStatus.PENDING, "Hệ thống ghi nhận đơn hàng mới từ khách hàng.", changedByActor);

        if (appliedVoucher != null) {
            appliedVoucher.setUsageCount(appliedVoucher.getUsageCount() + 1);
            voucherRepository.save(appliedVoucher);
        }

        List<BookingPassenger> savedPassengers = request.getPassengers().stream().map(pReq -> {
            return bookingPassengerRepository.save(BookingPassenger.builder()
                    .booking(booking)
                    .fullName(pReq.getFullName())
                    .birthDate(pReq.getBirthDate())
                    .gender(pReq.getGender().toUpperCase())
                    .passengerType(pReq.getPassengerType())
                    .build());
        }).collect(Collectors.toList());

        try {
            BookingEmailEvent emailEvent = BookingEmailEvent.builder()
                    .type("BOOKING_SUCCESS")
                    .toEmail(request.getContactInfo().getEmail())
                    .bookingCode(booking.getBookingCode())
                    .tourName(schedule.getTour().getName())
                    .customerName(request.getContactInfo().getFullName())
                    .phone(request.getContactInfo().getPhone())
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, emailEvent);
            log.info("RABBITMQ: Đã gửi sự kiện BOOKING_SUCCESS vào Queue");
        } catch (Exception e) {
            log.error("Lỗi khi đẩy tin nhắn gửi mail vào RabbitMQ: {}", e.getMessage());
        }

        try {
            if (customerProfile != null && customerProfile.getUser() != null) {
                notificationRepository.save(Notification.builder()
                        .title("Đặt tour thành công!")
                        .message("Hệ thống đã ghi nhận đơn hàng " + booking.getBookingCode() + ". Vui lòng hoàn tất thanh toán.")
                        .type(NotificationType.BOOKING)
                        .user(customerProfile.getUser())
                        .createdBy("Hệ thống")
                        .build());
            }

            if (actor != null) {
                List<User> receivers = userRepository.findAdminAndKetoan();
                if (receivers != null && !receivers.isEmpty()) {
                    String staffName = actor.getEmail();
                    var staffProfileOpt = staffProfileRepository.findById(actor.getId());
                    if (staffProfileOpt.isPresent()) {
                        staffName = staffProfileOpt.get().getFullName();
                    }

                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
                    String formattedPrice = df.format(booking.getTotalFinalPrice());

                    String notiMessage = String.format("Nhân viên %s vừa tạo đơn %s cho khách %s.\nGiá trị đơn hàng: %s VNĐ.",
                            staffName, booking.getBookingCode(), booking.getContactName(), formattedPrice);

                    for (User receiver : receivers) {
                        notificationRepository.save(Notification.builder()
                                .title("Đơn hàng mới tạo từ CRM")
                                .message(notiMessage)
                                .type(NotificationType.BOOKING)
                                .user(receiver)
                                .createdBy("CRM System")
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi xử lý thông báo tạo đơn: {}", e.getMessage());
        }
        if (actor != null) {
            log.info("CRM FLOW: Đã tạo đơn hộ thành công (Mã: {}). Đơn hàng ở trạng thái PENDING, chờ bộ phận kế toán duyệt dòng tiền.",
                    booking.getBookingCode());
        }

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

        if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.PARTIALLY_PAID) {
            log.info("Khách hủy đơn đã xuống tiền -> Chuyển luồng ủy thác tự động hoàn tiền sang PaymentService...");

            updateInventory(booking.getSchedule(), bookingPassengerRepository.findByBookingId(booking.getId()).size());
            if (booking.getVoucher() != null) {
                Voucher v = booking.getVoucher();
                v.setUsageCount(Math.max(0, v.getUsageCount() - 1));
                voucherRepository.save(v);
            }

            paymentService.cancelAndRefundBooking(booking.getId(), "Khách hàng chủ động yêu cầu hủy tour trực tuyến trên Website");
            return getBookingById(booking.getId());
        }

        updateInventory(booking.getSchedule(), bookingPassengerRepository.findByBookingId(booking.getId()).size());

        if (booking.getVoucher() != null) {
            Voucher v = booking.getVoucher();
            v.setUsageCount(Math.max(0, v.getUsageCount() - 1));
            voucherRepository.save(v);
        }

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setVoucher(null);
        bookingRepository.save(booking);

        saveStatusHistory(booking, oldStatus, BookingStatus.CANCELLED, "Người dùng yêu cầu hủy (Đơn chưa thanh toán)", "User ID: " + currentUserId);

        sendAdminNotificationOnCancel(booking);
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
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookingsForAdmin() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        // 🎯 CHỐT PHÂN QUYỀN: Cả ADMIN và KẾ TOÁN (ACCOUNTANT) đều được xem full phế hệ thống
        boolean canViewAll = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ACCOUNTANT"));

        List<Booking> bookings;

        if (canViewAll) {
            log.info("CRM SECURITY: Vai trò CAO CẤP (ADMIN/ACCOUNTANT) - Khai thác toàn bộ đơn hàng hệ thống.");
            // Dùng đúng hàm gốc có sẵn trong repo của ông giáo
            bookings = bookingRepository.findAll();
        } else {
            // Luồng trích xuất giới hạn dành riêng cho SALE
            Integer currentUserId = null;
            if (auth != null && auth.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
                // Lấy userId bằng cách ép chuỗi an toàn, dell sợ lỗi ClassCastException Long/Integer ngầm
                Object userIdObj = jwt.getClaim("userId");
                if (userIdObj != null) {
                    currentUserId = Integer.parseInt(userIdObj.toString());
                }
            }

            log.info("CRM SECURITY: Vai trò SALE - Trích xuất đơn hàng cá nhân của UserId {}.", currentUserId);
            // Dùng đúng hàm gốc có sẵn: findByCreatedBy_Id
            bookings = (currentUserId != null) ? bookingRepository.findByCreatedBy_Id(currentUserId) : Collections.emptyList();
        }

        if (bookings.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> bookingIds = bookings.stream().map(Booking::getId).toList();
        List<BookingPassenger> allPassengers = bookingPassengerRepository.findByBookingIdIn(bookingIds);
        List<Payment> allPayments = paymentRepository.findByBookingIdIn(bookingIds);

        return bookings.stream()
                .sorted(java.util.Comparator.comparing(Booking::getCreatedAt, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())))
                .map(booking -> {
                    List<BookingPassenger> passengers = allPassengers.stream()
                            .filter(p -> p.getBooking().getId().equals(booking.getId())).toList();
                    List<Payment> payments = allPayments.stream()
                            .filter(p -> p.getBooking().getId().equals(booking.getId())).toList();

                    BookingResponse response = BookingResponse.fromBooking(booking, passengers);

                    BigDecimal totalPaid = payments.stream()
                            .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                            .map(Payment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal remaining = booking.getTotalFinalPrice().subtract(totalPaid).max(BigDecimal.ZERO);

                    response.setPayments(payments.stream().map(p -> PaymentResponse.fromPayment(p, remaining)).toList());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponse updateBookingStatus(Integer bookingId, String status, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        BookingStatus oldStatus = booking.getStatus();
        BookingStatus newStatus = BookingStatus.valueOf(status.toUpperCase());

        if (!isAdmin) {
            Integer currentUserId = null;
            if (auth.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt) {
                Long uId = ((org.springframework.security.oauth2.jwt.Jwt) auth.getPrincipal()).getClaim("userId");
                if (uId != null) currentUserId = uId.intValue();
            }

            if (booking.getCreatedBy() == null || !booking.getCreatedBy().getId().equals(currentUserId)) {
                throw new RuntimeException("Bạn không có quyền can thiệp vào đơn hàng này!");
            }

            if (newStatus == BookingStatus.CONFIRMED || newStatus == BookingStatus.COMPLETED) {
                throw new RuntimeException("Thao tác duyệt thu tiền/Xác nhận đơn chỉ thuộc thẩm quyền của Admin hoặc Kế toán!");
            }
        }

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
            }

            if (oldStatus == BookingStatus.CONFIRMED || oldStatus == BookingStatus.PARTIALLY_PAID) {
                log.info("Admin hủy đơn đã thu tiền -> Kích hoạt lệnh hoàn tiền tự động đồng bộ...");
                paymentService.cancelAndRefundBooking(booking.getId(), "Quản trị viên hệ thống chủ động hủy đơn: " + reason);
                return getBookingById(bookingId);
            }
        } else if (oldStatus == BookingStatus.CANCELLED && (newStatus == BookingStatus.PENDING || newStatus == BookingStatus.CONFIRMED)) {
            if (booking.getSchedule().getAvailableSlots() < passengerCount) {
                throw new AppException(ErrorCode.TOUR_FULL);
            }
            updateInventory(booking.getSchedule(), -passengerCount);
        }

        booking.setStatus(newStatus);
        bookingRepository.save(booking);

        saveStatusHistory(booking, oldStatus, newStatus, reason, isAdmin ? "ADMIN" : "SALE_STAFF");
        sendCustomerNotificationOnStatusChange(booking, newStatus, reason);

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

    // 🌟 KHỚP NỐI VỊ TRÍ 3: ADMIN HỦY TẬP THỂ TOÀN BỘ ĐƠN HÀNG DO HOÃN LỊCH TRÌNH KHỞI HÀNH
    @Override
    @Transactional
    public void cancelAllBookingsBySchedule(Integer scheduleId, String reason) {
        List<Booking> bookings = bookingRepository.findByScheduleId(scheduleId).stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .collect(Collectors.toList());

        log.info("Bắt đầu hủy tập thể và kích hoạt hoàn tiền tự động cho {} đơn hàng thuộc lịch trình ID: {}", bookings.size(), scheduleId);

        for (Booking booking : bookings) {
            int passengerCount = bookingPassengerRepository.findByBookingId(booking.getId()).size();

            // Hoàn lại kho chỗ trống lịch trình & voucher
            updateInventory(booking.getSchedule(), passengerCount);
            if (booking.getVoucher() != null) {
                Voucher v = booking.getVoucher();
                v.setUsageCount(Math.max(0, v.getUsageCount() - 1));
                voucherRepository.save(v);
            }

            BookingStatus oldStatus = booking.getStatus();

            // 🌟 LOGIC ĐỒNG BỘ: Kiểm tra nếu đơn hàng này đã nộp tiền, ép nốt đống Payment ăn theo trạng thái REFUND
            if (oldStatus == BookingStatus.CONFIRMED || oldStatus == BookingStatus.PARTIALLY_PAID) {
                log.info("Đơn hàng {} đã đóng tiền thành công -> Kích hoạt tự động REFUND hóa đơn...", booking.getBookingCode());
                paymentService.cancelAndRefundBooking(booking.getId(), "Hủy tour diện rộng do thay đổi lịch trình hệ thống: " + reason);
            } else {
                // Đơn chưa trả tiền (PENDING) thì chỉ cần cập nhật trạng thái đơn trơn như bình thường
                booking.setStatus(BookingStatus.CANCELLED);
                booking.setVoucher(null);
                bookingRepository.save(booking);
                saveStatusHistory(booking, oldStatus, BookingStatus.CANCELLED, reason, "ADMIN_SYSTEM");
            }

            log.info("Đã xử lý hủy thành công đơn hàng: {} thuộc lịch trình {}", booking.getBookingCode(), scheduleId);
            sendMassCancelNotification(booking, reason);
        }
    }

    private void sendAdminNotificationOnCancel(Booking booking) {
        try {
            List<User> receivers = userRepository.findAdminAndKetoan();
            if (receivers != null && !receivers.isEmpty()) {
                java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
                String formattedPrice = df.format(booking.getTotalFinalPrice());

                String notiMessage = String.format("Khách hàng %s đã chủ động hủy đơn %s trên hệ thống Website.\nGiá trị đơn hoàn trả: %s VNĐ.",
                        booking.getContactName(), booking.getBookingCode(), formattedPrice);

                for (User receiver : receivers) {
                    notificationRepository.save(Notification.builder()
                            .title("Khách hàng hủy đơn hàng")
                            .message(notiMessage)
                            .type(NotificationType.ERROR)
                            .user(receiver)
                            .createdBy("Website")
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("Lỗi xử lý thông báo khách hủy đơn: {}", e.getMessage());
        }
    }

    private void sendCustomerNotificationOnStatusChange(Booking booking, BookingStatus newStatus, String reason) {
        try {
            if (booking.getCustomer() != null && booking.getCustomer().getUser() != null) {
                String notiTitle = "Cập nhật trạng thái đơn hàng";
                String notiMessage = "Đơn hàng " + booking.getBookingCode() + " của bạn đã được chuyển sang trạng thái: " + newStatus;
                NotificationType notiType = NotificationType.INFO;

                if (newStatus == BookingStatus.CONFIRMED) {
                    notiTitle = "Đơn đặt tour đã được xác nhận!";
                    notiMessage = "Hệ thống xác nhận đơn " + booking.getBookingCode() + " đã thanh toán thành công. Chúc bạn có một chuyến đi vui vẻ!";
                    notiType = NotificationType.SUCCESS;
                } else if (newStatus == BookingStatus.CANCELLED) {
                    notiTitle = "Đơn hàng đã bị hủy bỏ";
                    notiMessage = "Đơn hàng " + booking.getBookingCode() + " đã bị hủy bởi quản trị viên hệ thống. Lý do: " + reason;
                    notiType = NotificationType.ERROR;
                }

                notificationRepository.save(Notification.builder()
                        .title(notiTitle)
                        .message(notiMessage)
                        .type(notiType)
                        .user(booking.getCustomer().getUser())
                        .createdBy("Hệ thống")
                        .build());
            }
        } catch (Exception e) {
            log.error("Lỗi xử lý thông báo đổi trạng thái đơn: {}", e.getMessage());
        }
    }

    private void sendMassCancelNotification(Booking booking, String reason) {
        try {
            if (booking.getCustomer() != null && booking.getCustomer().getUser() != null) {
                Notification massCancelNoti = Notification.builder()
                        .title("Thông báo: Hủy lịch trình khởi hành")
                        .message(String.format(
                                "Rất tiếc, đơn hàng %s của bạn đã bị hủy do lịch trình chuyến đi \"%s\" (Khởi hành ngày %s) bị thay đổi.\nLý do từ hệ thống: %s.\nVui lòng liên hệ hotline để nhận phương án hoàn tiền.",
                                booking.getBookingCode(),
                                booking.getTourNameSnapshot(),
                                booking.getDepartureDateSnapshot() != null ? booking.getDepartureDateSnapshot().toString() : "N/A",
                                reason
                        ))
                        .type(NotificationType.ERROR)
                        .user(booking.getCustomer().getUser())
                        .createdBy("ADMIN_SYSTEM")
                        .build();

                notificationRepository.save(massCancelNoti);
            }
        } catch (Exception e) {
            log.error("Lỗi gửi thông báo hủy lịch trình cho đơn {}: {}", booking.getBookingCode(), e.getMessage());
        }
    }


}