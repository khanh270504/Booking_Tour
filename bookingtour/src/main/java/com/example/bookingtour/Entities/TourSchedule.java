package com.example.bookingtour.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "tour_schedules")
@Data
@NoArgsConstructor
public class TourSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    // Số chỗ còn lại (Atomic Update sẽ trừ trực tiếp vào cột này)
    @Column(name = "available_slots", nullable = false)
    private Integer availableSlots;

    @Column(name = "status", length = 20)
    private String status; // OPEN, CLOSED, FULL

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}