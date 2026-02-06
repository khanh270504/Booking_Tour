package com.example.bookingtour.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "tour_costs")
@Data
@NoArgsConstructor
public class TourCost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private TourSchedule schedule;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @Column(name = "expense_name", length = 100)
    private String expenseName; // VD: Tiền phòng khách sạn Mường Thanh

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "status", length = 20)
    private String status; // UNPAID, PAID

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}