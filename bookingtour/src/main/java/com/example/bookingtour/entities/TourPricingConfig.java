package com.example.bookingtour.entities;

import com.example.bookingtour.enums.PassengerType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tour_pricing_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TourPricingConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_type", length = 20)
    private PassengerType passengerType;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;
}