package com.example.bookingtour.entities;

import com.example.bookingtour.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(name = "booking_code", unique = true, length = 50)
    private String bookingCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private TourSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;


    @Column(name = "total_original_price")
    private BigDecimal totalOriginalPrice;

    @Column(name = "total_discount")
    private BigDecimal totalDiscount;

    @Column(name = "total_surcharge")
    private BigDecimal totalSurcharge;

    @Column(name = "total_final_price")
    private BigDecimal totalFinalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private BookingStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false) // Không cho phép sửa ngày tạo đơn
    private Instant createdAt;
}