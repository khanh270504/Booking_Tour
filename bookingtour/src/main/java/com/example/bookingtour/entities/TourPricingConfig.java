package com.example.bookingtour.entities;

import com.example.bookingtour.enums.PassengerType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tour_pricing_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourPricingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private TourSchedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_type", length = 20, nullable = false)
    private PassengerType passengerType;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "currency", length = 3)
    private String currency;

}