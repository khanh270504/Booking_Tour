package com.example.bookingtour.Repositories;

import com.example.bookingtour.Entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByBookingId(Integer bookingId);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}