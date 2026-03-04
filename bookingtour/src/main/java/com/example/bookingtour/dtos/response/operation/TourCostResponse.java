package com.example.bookingtour.dtos.response.operation;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TourCostResponse {
    private Integer id;
    private Integer scheduleId;
    private String providerName;
    private String expenseName;
    private BigDecimal amount;
    private String status;
    private Instant createdAt;
}