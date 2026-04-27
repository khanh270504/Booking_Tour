package com.example.bookingtour.entities;

import com.example.bookingtour.enums.ScheduleStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "tour_schedules")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TourSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @Column(name = "departure_date", nullable = false)
    private LocalDateTime departureDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(name = "max_slots", nullable = false)
    private Integer maxSlots;

    @Column(name = "available_slots", nullable = false)
    private Integer availableSlots;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ScheduleStatus status;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "departure_location")
    private String departureLocation;

    @Column(name = "schedule_code", unique = true)
    private String scheduleCode;
}