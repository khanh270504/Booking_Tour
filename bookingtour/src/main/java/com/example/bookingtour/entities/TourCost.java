package com.example.bookingtour.entities;

import com.example.bookingtour.enums.TourCostStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "tour_costs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourCost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private TourSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @Column(name = "expense_name", length = 150)
    private String expenseName;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TourCostStatus status;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "paid_at")
    private Instant paidAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}