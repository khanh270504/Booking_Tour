package com.example.bookingtour.dtos.response.tour;

import com.example.bookingtour.entities.TourPricingConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PricingConfigResponse {
    private Integer id;
    private Integer scheduleId;
    private String passengerType;
    private BigDecimal price;
    private String currency;

    public static PricingConfigResponse fromPricingConfig(TourPricingConfig config) {
        if (config == null) return null;

        return PricingConfigResponse.builder()
                .id(config.getId())
                .scheduleId(config.getSchedule() != null ? config.getSchedule().getId() : null)
                .passengerType(config.getPassengerType() != null ? config.getPassengerType().name() : null)
                .price(config.getPrice())
                .currency(config.getCurrency())
                .build();
    }
}