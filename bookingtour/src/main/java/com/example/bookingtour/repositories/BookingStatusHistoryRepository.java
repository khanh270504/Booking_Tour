package com.example.bookingtour.repositories;
import com.example.bookingtour.entities.BookingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface BookingStatusHistoryRepository extends JpaRepository<BookingStatusHistory, Integer> {
    List<BookingStatusHistory> findByBookingIdOrderByCreatedAtDesc(Integer bookingId);
}