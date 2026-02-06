package com.example.bookingtour.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "tour_surcharges")
@Data
@NoArgsConstructor
public class TourSurcharge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @Column(name = "surcharge_name", length = 100)
    private String surchargeName; // VD: Phòng đơn

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "is_mandatory")
    private Boolean isMandatory; // Bắt buộc hay không
}