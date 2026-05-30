package com.example.bookingtour.dtos.response.operation;

import com.example.bookingtour.entities.Provider;
import com.example.bookingtour.enums.ProviderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderResponse {
    private Integer id;
    private String providerCode;
    private String name;
    private String serviceType;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private ProviderStatus status;

    private Long linkedToursCount;
    private BigDecimal totalCostVolume;

    public static ProviderResponse fromEntity(Provider entity, Long linkedToursCount, BigDecimal totalCostVolume) {
        if (entity == null) return null;
        return ProviderResponse.builder()
                .id(entity.getId())
                .providerCode(entity.getProviderCode())
                .name(entity.getName())
                .serviceType(entity.getServiceType() != null ? entity.getServiceType().name() : null)
                .contactPerson(entity.getContactPerson())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .address(entity.getAddress())
                .status(entity.getStatus())
                .linkedToursCount(linkedToursCount != null ? linkedToursCount : 0L)
                .totalCostVolume(totalCostVolume != null ? totalCostVolume : BigDecimal.ZERO)
                .build();
    }
}