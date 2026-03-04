package com.example.bookingtour.dtos.response.system;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

@Data
public class DailyRevenueResponse {
    private LocalDate reportDate;
    private Integer totalBookings;
    private BigDecimal totalRevenue;
    private BigDecimal totalProfit;
    private Instant updatedAt;
}