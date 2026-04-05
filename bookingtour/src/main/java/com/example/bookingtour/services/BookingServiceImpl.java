package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IBookingService;
import com.example.bookingtour.dtos.internal.PricingResultDto;
import com.example.bookingtour.dtos.request.booking.BookingCancelRequest;
import com.example.bookingtour.dtos.request.booking.BookingCreateRequest;
import com.example.bookingtour.dtos.request.booking.PassengerRequest;
import com.example.bookingtour.dtos.request.payment.ManualPaymentRequest;
import com.example.bookingtour.dtos.response.booking.BookingResponse;
import com.example.bookingtour.dtos.response.payment.PaymentResponse;
import com.example.bookingtour.entities.*;
import com.example.bookingtour.enums.BookingStatus;
import com.example.bookingtour.enums.PassengerType;
import com.example.bookingtour.enums.ScheduleStatus;
import com.example.bookingtour.enums.UserStatus;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements IBookingService {

    private final UserRepository userRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final BookingPassengerRepository bookingPassengerRepository;
    private final BookingRepository bookingRepository;
    private final CustomerProfileRepository profileRepository;
    private final PricingServiceImpl pricingService;
    private final BookingStatusHistoryRepository statusHistoryRepository;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request, String userInternalId) {
        log.info("--- Bắt đầu xử lý đơn hàng cho: {} ---", request.getEmail());

        // 1. Xác định User (Giao cho hàm handleShadowUser xử lý logic check trùng)
        User user;
        if (userInternalId != null) {
            user = userRepository.findById(Integer.parseInt(userInternalId))
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        } else {
            user = handleShadowUser(request);
        }

        // 2. Check chỗ trống của Tour
        TourSchedule schedule = tourScheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (schedule.getAvailableSlots() < request.getPassengers().size()) {
            throw new AppException(ErrorCode.TOUR_FULL);
        }

        // 3. Tính tiền tự động
        PricingResultDto pricing = pricingService.calculatePrice(schedule.getTour().getId(), request.getPassengers());

        // 4. Lưu Booking (Liên kết với User cũ hoặc mới tạo)
        Booking booking = Booking.builder()
                .bookingCode(generateCode("BK"))
                .user(user)
                .schedule(schedule)
                .status(BookingStatus.PENDING)
                .totalOriginalPrice(pricing.getTotalOriginalPrice())
                .totalSurcharge(pricing.getTotalSurcharge())
                .totalDiscount(pricing.getTotalDiscount())
                .totalFinalPrice(pricing.getTotalFinalPrice())
                .build();

        bookingRepository.save(booking);

        // Lưu lịch sử trạng thái ban đầu
        saveStatusHistory(booking, null, BookingStatus.PENDING, "Khởi tạo đơn hàng");

        // 5. Lưu danh sách hành khách & Cập nhật số lượng chỗ trống
        List<BookingPassenger> savedPassengers = persistPassengers(request.getPassengers(), booking, pricing.getUnitPriceMap());
        updateInventory(schedule, -request.getPassengers().size());

        log.info("=> [SUCCESS] Đơn hàng {} đã tạo xong cho User ID: {}", booking.getBookingCode(), user.getId());

        return BookingResponse.fromBooking(booking, request.getCustomerProfile().getFullName(), savedPassengers);
    }

    /**
     * Logic "linh hồn" để chống Duplicate Email:
     * Tìm thấy thì dùng lại, không thấy mới tạo.
     */
    private User handleShadowUser(BookingCreateRequest request) {
        String emailClean = request.getEmail().trim().toLowerCase();

        // Check xem ông này đã từng đặt tour hay có tài khoản chưa
        Optional<User> existingUser = userRepository.findByEmail(emailClean);

        if (existingUser.isPresent()) {
            log.info("=> Email {} đã tồn tại. Dùng lại User ID: {}", emailClean, existingUser.get().getId());
            return existingUser.get();
        }

        // Nếu chưa có thì mới tạo User mới nặc danh (Shadow User)
        log.info("=> Email {} mới toanh. Đang tạo Shadow User...", emailClean);
        User user = User.builder()
                .userCode(generateCode("G"))
                .email(emailClean)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        // Tạo luôn Profile đi kèm cho User mới
        CustomerProfile profile = CustomerProfile.builder()
                .user(user)
                .fullName(request.getCustomerProfile().getFullName())
                .phone(request.getCustomerProfile().getPhone())
                .build();
        profileRepository.save(profile);

        return user;
    }

    // --- CÁC HÀM KHÁC GIỮ NGUYÊN ---

    @Override
    public BookingResponse getBookingById(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        List<BookingPassenger> passengers = bookingPassengerRepository.findByBookingId(bookingId);
        String customerName = profileRepository.findByUser_Id(booking.getUser().getId())
                .map(CustomerProfile::getFullName).orElse("Guest");
        return BookingResponse.fromBooking(booking, customerName, passengers);
    }

    @Override
    public List<BookingResponse> getBookingsByUser(String userInternalId) {
        Integer id = Integer.parseInt(userInternalId);
        String customerName = profileRepository.findByUser_Id(id).map(CustomerProfile::getFullName).orElse("Guest");
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(id).stream()
                .map(b -> BookingResponse.fromBooking(b, customerName, bookingPassengerRepository.findByBookingId(b.getId())))
                .toList();
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(BookingCancelRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        if (booking.getStatus() == BookingStatus.CANCELLED) throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);

        updateInventory(booking.getSchedule(), bookingPassengerRepository.findByBookingId(booking.getId()).size());
        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        saveStatusHistory(booking, oldStatus, BookingStatus.CANCELLED, "Người dùng yêu cầu hủy");

        return getBookingById(booking.getId());
    }

    private void saveStatusHistory(Booking booking, BookingStatus from, BookingStatus to, String reason) {
        statusHistoryRepository.save(BookingStatusHistory.builder()
                .booking(booking).fromStatus(from).toStatus(to).reason(reason).build());
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

    private List<BookingPassenger> persistPassengers(List<PassengerRequest> reqs, Booking b, Map<PassengerType, BigDecimal> prices) {
        List<BookingPassenger> list = reqs.stream().map(r -> BookingPassenger.builder()
                .booking(b).fullName(r.getFullName()).gender(r.getGender())
                .birthDate(r.getBirthDate()).passengerType(PassengerType.valueOf(r.getPassengerType().toUpperCase()))
                .unitPrice(prices.get(PassengerType.valueOf(r.getPassengerType().toUpperCase()))).build()).toList();
        return bookingPassengerRepository.saveAll(list);
    }
}