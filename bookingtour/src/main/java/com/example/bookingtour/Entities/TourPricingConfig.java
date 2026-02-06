package com.example.bookingtour.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tour_pricing_configs")
@Data
@NoArgsConstructor
public class TourPricingConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;

    // ADULT, CHILD, INFANT (Nên tạo Enum cho cái này)
    @Column(name = "passenger_type", length = 20)
    private String passengerType;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "currency", length = 3)
    private String currency; // VND

    @Column(name = "effective_date")
    private LocalDate effectiveDate; // Giá áp dụng từ ngày...
}