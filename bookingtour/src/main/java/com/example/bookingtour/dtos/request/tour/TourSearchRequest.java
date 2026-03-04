package com.example.bookingtour.dtos.request.tour;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TourSearchRequest {
    private String keyword;
    private Integer destinationId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String tourType;
}