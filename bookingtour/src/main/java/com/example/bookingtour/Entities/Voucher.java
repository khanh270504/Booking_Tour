package com.example.bookingtour.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "vouchers")
@Data
@NoArgsConstructor
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code", unique = true, length = 50)
    private String code;

    @Column(name = "discount_type", length = 20)
    private String discountType; // PERCENT, FIXED_AMOUNT

    @Column(name = "discount_value")
    private BigDecimal discountValue;

    @Column(name = "max_usage")
    private Integer maxUsage;

    @Column(name = "expiry_date")
    private Instant expiryDate;
}