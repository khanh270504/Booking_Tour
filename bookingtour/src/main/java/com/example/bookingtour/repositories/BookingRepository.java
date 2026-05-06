package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    @Query("SELECT b FROM Booking b WHERE b.customer.user.id = :userId ORDER BY b.createdAt DESC")
    List<Booking> findByCustomer_User_IdOrderByCreatedAtDesc(@Param("userId") Integer userId);

    Optional<Booking> findByBookingCodeAndContactEmail(String bookingCode, String contactEmail);
}