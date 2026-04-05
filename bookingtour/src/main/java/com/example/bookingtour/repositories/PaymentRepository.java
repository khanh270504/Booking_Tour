package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.Payment;
import com.example.bookingtour.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByBookingId(Integer bookingId);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    boolean existsByBookingIdAndStatus(Integer bookingId, PaymentStatus status);
}