package com.example.bookingtour.dtos.request.tour;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class TourSearchRequest {
    private String keyword;

    private List<Integer> destinationIds;

    private List<String> departureLocations;

    private LocalDate fromDate;
    private LocalDate toDate;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

}