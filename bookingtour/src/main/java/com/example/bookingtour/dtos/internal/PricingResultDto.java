package com.example.bookingtour.dtos.internal;

import com.example.bookingtour.enums.PassengerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PricingResultDto {


    private BigDecimal totalOriginalPrice;
    private BigDecimal totalSurcharge;
    private BigDecimal totalDiscount;
    private BigDecimal totalFinalPrice;


    private Map<PassengerType, BigDecimal> unitPriceMap;
}