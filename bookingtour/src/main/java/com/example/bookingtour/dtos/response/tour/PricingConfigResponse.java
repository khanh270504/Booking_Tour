package com.example.bookingtour.dtos.response.tour;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PricingConfigResponse {
    private Integer id;
    private String passengerType; // ADULT, CHILD
    private BigDecimal price;
    private String currency;
    private LocalDate effectiveDate;
}