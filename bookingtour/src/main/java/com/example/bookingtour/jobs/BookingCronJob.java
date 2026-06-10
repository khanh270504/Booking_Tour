package com.example.bookingtour.jobs;

import com.example.bookingtour.entities.Booking;
import com.example.bookingtour.entities.BookingStatusHistory;
import com.example.bookingtour.entities.Payment;
import com.example.bookingtour.entities.TourSchedule;
import com.example.bookingtour.enums.BookingStatus;
import com.example.bookingtour.enums.PaymentStatus;
import com.example.bookingtour.enums.ScheduleStatus;
import com.example.bookingtour.repositories.BookingRepository;
import com.example.bookingtour.repositories.BookingStatusHistoryRepository;
import com.example.bookingtour.repositories.PaymentRepository;
import com.example.bookingtour.repositories.TourScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingCronJob {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TourScheduleRepository scheduleRepository;
    private final BookingStatusHistoryRepository historyRepository;

    @Scheduled(cron = "2 * * * * ?")
    @Transactional
    public void autoUpdateBookingStatus() {
        log.info("[CRON JOB] Bắt đầu quét đơn hàng...");

        List<TourSchedule> completedSchedules = scheduleRepository.findByStatus(ScheduleStatus.COMPLETED);

        for (TourSchedule schedule : completedSchedules) {
            List<Booking> bookings = bookingRepository.findByScheduleAndStatus(schedule, BookingStatus.CONFIRMED);

            for (Booking booking : bookings) {
                List<Payment> payments = paymentRepository.findByBookingId(booking.getId());
                boolean isPaid = payments.stream()
                        .anyMatch(p -> p.getStatus().equals(PaymentStatus.SUCCESS));

                if (isPaid) {
                    BookingStatus oldStatus = booking.getStatus();
                    booking.setStatus(BookingStatus.COMPLETED);
                    BookingStatusHistory history = new BookingStatusHistory();
                    history.setBooking(booking);
                    history.setFromStatus(oldStatus); // <--- THÊM DÒNG NÀY ĐỂ HẾT NULL ÔNG NHÉ!
                    history.setToStatus(BookingStatus.COMPLETED);
                    history.setReason("Hệ thống tự động hoàn tất sau chuyến đi");
                    history.setChangedBy("SYSTEM_CRON_JOB");
                    history.setCreatedAt(Instant.now());
                    historyRepository.save(history);
                    log.info("Booking {} hoàn thành & đã lưu lịch sử từ {} sang COMPLETED.", booking.getId(), oldStatus);
                }
            }
            bookingRepository.saveAll(bookings);
        }
    }
}