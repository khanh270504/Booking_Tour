package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IBookingService;
import com.example.bookingtour.dtos.internal.PricingResultDto; // Đã đổi tên chuẩn
import com.example.bookingtour.dtos.request.booking.BookingCancelRequest;
import com.example.bookingtour.dtos.request.booking.BookingCreateRequest;
import com.example.bookingtour.dtos.request.booking.PassengerRequest;
import com.example.bookingtour.dtos.request.payment.ManualPaymentRequest;
import com.example.bookingtour.dtos.response.booking.BookingResponse;
import com.example.bookingtour.dtos.response.payment.PaymentResponse;
import com.example.bookingtour.entities.Booking;
import com.example.bookingtour.entities.BookingPassenger;
import com.example.bookingtour.entities.CustomerProfile;
import com.example.bookingtour.entities.TourSchedule;
import com.example.bookingtour.entities.User;
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

    @Override
    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request, String userId) {
        log.info("--- Khởi tạo Đơn hàng cho email: {} ---", request.getEmail());

        // 1. Định danh User (Khách vãng lai hoặc đã login)
        User user = (userId != null)
                ? userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED))
                : handleShadowUser(request);

        // 2. Check chỗ trống
        TourSchedule schedule = tourScheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (schedule.getAvailableSlots() < request.getPassengers().size()) {
            throw new AppException(ErrorCode.TOUR_FULL); // Đã thêm chữ THROW thần thánh vào đây!
        }

        // 3. Gọi Thủ quỹ tính tiền
        PricingResultDto pricing = pricingService.calculatePrice(schedule.getTour().getId(), request.getPassengers());

        // 4. Lưu Booking
        Booking booking = bookingRepository.save(Booking.builder()
                .bookingCode(generateCode("BK"))
                .user(user)
                .schedule(schedule)
                .status(BookingStatus.PENDING)
                .totalOriginalPrice(pricing.getTotalOriginalPrice()) // Tiền gốc
                .totalSurcharge(pricing.getTotalSurcharge())         // Phụ thu
                .totalDiscount(pricing.getTotalDiscount())           // Giảm giá
                .totalFinalPrice(pricing.getTotalFinalPrice())       // Chốt hạ
                .build());

        List<BookingPassenger> savedPassengers = persistPassengers(request.getPassengers(), booking, pricing.getUnitPriceMap());

        updateInventory(schedule, -request.getPassengers().size());

        log.info("=> Tạo đơn thành công! Mã Code: {}, Tổng tiền: {}", booking.getBookingCode(), booking.getTotalFinalPrice());

        return BookingResponse.fromBooking(booking, request.getCustomerProfile().getFullName(), savedPassengers);
    }

    private User handleShadowUser(BookingCreateRequest request) {
        String guestId = generateCode("G");


        User user = userRepository.save(User.builder()
                .userId(guestId)
                .email(request.getEmail())
              //  .roleName("USER")
                .status(UserStatus.ACTIVE)
                .build());

        profileRepository.save(CustomerProfile.builder()
                .userId(guestId)
                .fullName(request.getCustomerProfile().getFullName())
                .phone(request.getCustomerProfile().getPhone())
                .build());

        return user;
    }

    private String generateCode(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        // Đổi thành System.currentTimeMillis() cho an toàn hơn nanoTime ở một số HĐH
        return prefix + '-' + timestamp + '-' + System.currentTimeMillis() % 10000;
    }

    private void updateInventory(TourSchedule schedule, int delta) {
        int newSlots = schedule.getAvailableSlots() + delta;

        if (newSlots < 0) {
            throw new AppException(ErrorCode.TOUR_FULL);
        }

        schedule.setAvailableSlots(newSlots);

        if (newSlots == 0) {
            schedule.setStatus(ScheduleStatus.FULL);
        } else if (newSlots > 0 && schedule.getStatus() == ScheduleStatus.FULL) {
            schedule.setStatus(ScheduleStatus.OPENING);
        }

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
                            .unitPrice(priceMap.get(type)) // Móc giá từ HashMap an toàn
                            .build();
                })
                .toList();

        return bookingPassengerRepository.saveAll(passengers);
    }
    @Override
    public BookingResponse getBookingById(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        List<BookingPassenger> passengers = bookingPassengerRepository.findByBookingId(bookingId);

        // Tự tay móc tên khách hàng từ bảng Profile
        String customerName = profileRepository.findByUserId(booking.getUser().getUserId())
                .map(CustomerProfile::getFullName)
                .orElse("Unknown");

        return BookingResponse.fromBooking(booking, customerName, passengers);
    }

    @Override
    public List<BookingResponse> getBookingsByUser(String userId) {
        String customerName = profileRepository.findByUserId(userId)
                .map(CustomerProfile::getFullName)
                .orElse("Unknown");

        // GỌI ĐÚNG CÁI HÀM BẠN VỪA VIẾT Ở ĐÂY NHÉ:
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
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

        // Tự tay lấy list khách ra đếm xem có bao nhiêu người để còn hoàn trả chỗ
        List<BookingPassenger> passengers = bookingPassengerRepository.findByBookingId(booking.getId());

        // Trả lại chỗ trống cho Tour (cộng thêm vào)
        updateInventory(booking.getSchedule(), passengers.size());

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        String customerName = profileRepository.findByUserId(booking.getUser().getUserId())
                .map(CustomerProfile::getFullName)
                .orElse("Unknown");

        return BookingResponse.fromBooking(booking, customerName, passengers);
    }

    @Override
    @Transactional
    public PaymentResponse processManualPayment(ManualPaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // Đổi trạng thái sang Đã thanh toán
        booking.setStatus(BookingStatus.PAID);
        bookingRepository.save(booking);

        return PaymentResponse.builder()
                .bookingId(booking.getId())
                .status("SUCCESS")
                .transactionCode(request.getTransactionCode())
                .build();
    }
}