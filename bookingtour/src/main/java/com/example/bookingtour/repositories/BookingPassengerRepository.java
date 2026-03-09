package com.example.bookingtour.repositories;
import com.example.bookingtour.entities.BookingPassenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface BookingPassengerRepository extends JpaRepository<BookingPassenger, Integer> {
    List<BookingPassenger> findByBookingId(Integer bookingId);
}