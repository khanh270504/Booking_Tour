package com.example.bookingtour.dtos.response.tour;

import com.example.bookingtour.entities.TourSurcharge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SurchargeResponse {
    private Integer id;
    private Integer scheduleId;
    private String surchargeName;
    private BigDecimal amount;
    private Boolean isMandatory;

    public static SurchargeResponse fromSurcharge(TourSurcharge surcharge) {
        if (surcharge == null) return null;

        return SurchargeResponse.builder()
                .id(surcharge.getId())
                .scheduleId(surcharge.getSchedule() != null ? surcharge.getSchedule().getId() : null)
                .surchargeName(surcharge.getSurchargeName())
                .amount(surcharge.getAmount())
                .isMandatory(surcharge.getIsMandatory())
                .build();
    }
}