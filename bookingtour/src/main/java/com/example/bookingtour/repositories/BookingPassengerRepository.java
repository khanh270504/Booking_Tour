package com.example.bookingtour.repositories;
import com.example.bookingtour.entities.BookingPassenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface BookingPassengerRepository extends JpaRepository<BookingPassenger, Integer> {
    List<BookingPassenger> findByBookingId(Integer bookingId);

    @Query("SELECT bp FROM BookingPassenger bp " +
            "WHERE bp.booking.schedule.id = :scheduleId")
    List<BookingPassenger> findPassengersByScheduleId(@Param("scheduleId") Integer scheduleId);
    List<BookingPassenger> findByBookingIdIn(List<Integer> bookingIds);
}