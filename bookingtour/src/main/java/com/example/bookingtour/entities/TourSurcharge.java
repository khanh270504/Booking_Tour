package com.example.bookingtour.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tour_surcharges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourSurcharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private TourSchedule schedule;

    @Column(name = "surcharge_name", length = 100, nullable = false)
    private String surchargeName;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "is_mandatory")
    private Boolean isMandatory;
}
