package com.example.bookingtour.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private TourSchedule schedule;

    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    // --- CÁC CỘT TIỀN (QUAN TRỌNG) ---
    @Column(name = "total_original_price")
    private BigDecimal totalOriginalPrice;

    @Column(name = "total_discount")
    private BigDecimal totalDiscount;

    @Column(name = "total_surcharge")
    private BigDecimal totalSurcharge;

    @Column(name = "total_final_price")
    private BigDecimal totalFinalPrice;
    // ---------------------------------

    @Column(name = "status", length = 30)
    private String status; // PENDING, DEPOSITED, PAID, CANCELLED

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}