package com.example.bookingtour.dtos.response.tour;

import com.example.bookingtour.entities.TourPricingConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PricingConfigResponse {
    private Integer tourId;
    private Integer id;
    private String passengerType;
    private BigDecimal price;
    private String currency;
    private LocalDate effectiveDate;

    public static PricingConfigResponse fromPricingConfig(TourPricingConfig config) {
        if (config == null) return null;
        return PricingConfigResponse.builder()
                .id(config.getId())

                .tourId(config.getTour() != null ? config.getTour().getId() : null)

                .passengerType(config.getPassengerType() != null ? config.getPassengerType().name() : null)

                .price(config.getPrice())
                .currency(config.getCurrency())
                .effectiveDate(config.getEffectiveDate())
                .build();
    }

}