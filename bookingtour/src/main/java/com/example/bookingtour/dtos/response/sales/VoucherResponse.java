package com.example.bookingtour.dtos.response.sales;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class VoucherResponse {
    private String code;
    private String discountType;
    private BigDecimal discountValue;
    private Integer maxUsage;
    private Instant expiryDate;
}