package com.example.bookingtour.entities;

import com.example.bookingtour.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

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
    @JoinColumn(name = "customer_id")
    private CustomerProfile  customer;

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
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "tour_name_snapshot", length = 255)
    private String tourNameSnapshot;
    @Column(name = "departure_date_snapshot")
    private LocalDate departureDateSnapshot;

    @Column(name = "departure_location_snapshot", length = 255)
    private String departureLocationSnapshot;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @Column(name = "contact_phone", nullable = false)
    private String contactPhone;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

}