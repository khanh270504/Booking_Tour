package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IBookingService;
import com.example.bookingtour.dtos.internal.PricingResultDto;
import com.example.bookingtour.dtos.request.booking.BookingCancelRequest;
import com.example.bookingtour.dtos.request.booking.BookingCreateRequest;
import com.example.bookingtour.dtos.response.booking.BookingResponse;
import com.example.bookingtour.entities.*;
import com.example.bookingtour.enums.BookingStatus;
import com.example.bookingtour.enums.PassengerType;
import com.example.bookingtour.enums.ScheduleStatus;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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

        PricingResultDto pricing = pricingService.calculatePrice(schedule.getId(), request.getPassengers());

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
        String changedByActor = (actor != null) ? actor.getEmail() : "Guest (" + contactEmail + ")";
        saveStatusHistory(
                booking,
                null,
                BookingStatus.PENDING,
                "Hệ thống ghi nhận đơn hàng mới từ khách hàng.",
                changedByActor
        );
        List<BookingPassenger> savedPassengers = request.getPassengers().stream().map(pReq -> {
            PassengerType type = PassengerType.valueOf(pReq.getPassengerType().toUpperCase());

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

        return BookingResponse.fromBooking(booking, savedPassengers);
    }
    @Override
    public BookingResponse getBookingById(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        List<BookingPassenger> passengers = bookingPassengerRepository.findByBookingId(bookingId);

        return BookingResponse.fromBooking(booking, passengers);
    }

    @Override
    public List<BookingResponse> getBookingsByUser(Integer userInternalId) {

        return bookingRepository.findByCustomer_User_IdOrderByCreatedAtDesc(userInternalId).stream()
                .map(b -> BookingResponse.fromBooking(b, bookingPassengerRepository.findByBookingId(b.getId())))
                .toList();
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(BookingCancelRequest request, Integer currentUserId) { // 🎯 1. Thêm tham số currentUserId
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getCustomer() == null ||
                booking.getCustomer().getUser() == null ||
                !booking.getCustomer().getUser().getId().equals(currentUserId)) {
            log.warn("CẢNH BÁO BẢO MẬT: User {} cố tình hủy đơn hàng {} không thuộc sở hữu!", currentUserId, booking.getId());
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }

        updateInventory(booking.getSchedule(), bookingPassengerRepository.findByBookingId(booking.getId()).size());

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
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
}