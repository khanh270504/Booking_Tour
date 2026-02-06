package com.example.bookingtour.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey; // Key chống thanh toán lặp

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // MOMO, VNPAY

    @Column(name = "status", length = 20)
    private String status; // SUCCESS, FAILED

    @Column(name = "transaction_code", length = 100)
    private String transactionCode; // Mã giao dịch từ phía Ngân hàng/Ví

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}