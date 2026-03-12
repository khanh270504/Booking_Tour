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
        log.info("--- Khởi tạo Đơn hàng cho email: {} ---", request.getEmail());

        // 1. Xác định User (Dùng ID Integer để tìm cho nhanh)
        User user = (userInternalId != null)
                ? userRepository.findById(Integer.parseInt(userInternalId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED))
                : handleShadowUser(request);

        // 2. Check chỗ trống
        TourSchedule schedule = tourScheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (schedule.getAvailableSlots() < request.getPassengers().size()) {
            throw new AppException(ErrorCode.TOUR_FULL);
        }

        // 3. Tính tiền
        PricingResultDto pricing = pricingService.calculatePrice(schedule.getTour().getId(), request.getPassengers());

        // 4. Lưu Booking (Liên kết qua User Object - JPA tự lấy ID số để map)
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

        // Ghi lịch sử trạng thái (Lần đầu: from null to PENDING)
        saveStatusHistory(booking, null, BookingStatus.PENDING, "Khởi tạo đơn hàng mới");

        // 5. Lưu danh sách khách và cập nhật kho
        List<BookingPassenger> savedPassengers = persistPassengers(request.getPassengers(), booking, pricing.getUnitPriceMap());
        updateInventory(schedule, -request.getPassengers().size());

        log.info("=> Tạo đơn thành công! Mã Code: {}, ID User: {}", booking.getBookingCode(), user.getId());

        return BookingResponse.fromBooking(booking, request.getCustomerProfile().getFullName(), savedPassengers);
    }

    private User handleShadowUser(BookingCreateRequest request) {
        // Tạo User ảo (ID tự tăng, code G-... để hiển thị)
        User user = User.builder()
                .userCode(generateCode("G"))
                .email(request.getEmail())
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user); // Trả về User đã có ID số từ Database

        // Tạo Profile (Liên kết qua ID số của User ngầm định)
        CustomerProfile profile = CustomerProfile.builder()
                .user(user)
                .fullName(request.getCustomerProfile().getFullName())
                .phone(request.getCustomerProfile().getPhone())
                .build();
        profileRepository.save(profile);

        return user;
    }

    @Override
    public BookingResponse getBookingById(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        List<BookingPassenger> passengers = bookingPassengerRepository.findByBookingId(bookingId);

        // Dùng ID số để tìm Profile cho nhanh
        String customerName = profileRepository.findByUser_Id(booking.getUser().getId())
                .map(CustomerProfile::getFullName)
                .orElse("Unknown");

        return BookingResponse.fromBooking(booking, customerName, passengers);
    }

    @Override
    public List<BookingResponse> getBookingsByUser(String userInternalId) {
        Integer id = Integer.parseInt(userInternalId);

        String customerName = profileRepository.findByUser_Id(id)
                .map(CustomerProfile::getFullName)
                .orElse("Unknown");

        // Tìm danh sách đơn hàng theo ID số
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(id).stream()
                .map(booking -> {
                    List<BookingPassenger> passengers = bookingPassengerRepository.findByBookingId(booking.getId());
                    return BookingResponse.fromBooking(booking, customerName, passengers);
                })
                .toList();
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(BookingCancelRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }

        List<BookingPassenger> passengers = bookingPassengerRepository.findByBookingId(booking.getId());
        updateInventory(booking.getSchedule(), passengers.size());

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Lưu lịch sử (from oldStatus to CANCELLED)
        saveStatusHistory(booking, oldStatus, BookingStatus.CANCELLED, "Khách hàng hủy đơn");

        String customerName = profileRepository.findByUser_Id(booking.getUser().getId())
                .map(CustomerProfile::getFullName)
                .orElse("Unknown");

        return BookingResponse.fromBooking(booking, customerName, passengers);
    }

    @Override
    @Transactional
    public PaymentResponse processManualPayment(ManualPaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.PAID);
        bookingRepository.save(booking);

        // Lưu lịch sử thanh toán
        saveStatusHistory(booking, oldStatus, BookingStatus.PAID,
                "Xác nhận thanh toán thủ công (Mã GD: " + request.getTransactionCode() + ")");

        return PaymentResponse.builder()
                .bookingId(booking.getId())
                .status("SUCCESS")
                .transactionCode(request.getTransactionCode())
                .build();
    }

    // --- HÀM PHỤ TRỢ (HELPER METHODS) ---

    private void saveStatusHistory(Booking booking, BookingStatus fromStatus, BookingStatus toStatus, String reason) {
        BookingStatusHistory history = BookingStatusHistory.builder()
                .booking(booking)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .reason(reason)
                .build();
        statusHistoryRepository.save(history);
    }

    private String generateCode(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        return prefix + '-' + timestamp + '-' + System.currentTimeMillis() % 10000;
    }

    private void updateInventory(TourSchedule schedule, int delta) {
        int newSlots = schedule.getAvailableSlots() + delta;
        if (newSlots < 0) throw new AppException(ErrorCode.TOUR_FULL);
        schedule.setAvailableSlots(newSlots);
        if (newSlots == 0) schedule.setStatus(ScheduleStatus.FULL);
        else if (newSlots > 0 && schedule.getStatus() == ScheduleStatus.FULL) schedule.setStatus(ScheduleStatus.OPENING);
        tourScheduleRepository.save(schedule);
    }

    private List<BookingPassenger> persistPassengers(List<PassengerRequest> requests, Booking booking, Map<PassengerType, BigDecimal> priceMap) {
        List<BookingPassenger> passengers = requests.stream()
                .map(req -> {
                    PassengerType type = PassengerType.valueOf(req.getPassengerType().toUpperCase());
                    return BookingPassenger.builder()
                            .booking(booking)
                            .fullName(req.getFullName())
                            .gender(req.getGender())
                            .birthDate(req.getBirthDate())
                            .passengerType(type)
                            .unitPrice(priceMap.get(type))
                            .build();
                })
                .toList();
        return bookingPassengerRepository.saveAll(passengers);
    }
}