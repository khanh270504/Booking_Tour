package com.example.bookingtour.entities;

import com.example.bookingtour.enums.PaymentMethod;
import com.example.bookingtour.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50)
    private PaymentMethod paymentMethod; // MOMO, VNPAY

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private PaymentStatus status; // SUCCESS, FAILED

    @Column(name = "transaction_code", length = 100)
    private String transactionCode; // Mã giao dịch từ phía Ngân hàng/Ví

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}