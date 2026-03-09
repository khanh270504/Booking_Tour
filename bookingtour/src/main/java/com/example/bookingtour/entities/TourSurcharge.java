package com.example.bookingtour.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tour_surcharges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
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