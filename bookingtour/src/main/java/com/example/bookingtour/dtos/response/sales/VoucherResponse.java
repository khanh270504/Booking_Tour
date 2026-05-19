package com.example.bookingtour.dtos.response.sales;

import com.example.bookingtour.entities.Voucher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherResponse {

    private Integer id;

    private String title;

    private String description;

    private String code;

    private String discountType;

    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;

    private BigDecimal minOrderValue;

    private Integer maxUsage;

    private Integer usageCount;

    private Integer maxUsagePerUser;

    private Instant startDate;

    private Instant expiryDate;

    private Boolean isActive;

    private Integer tourId;

    private Integer userId;

    private Instant createdAt;

    public static VoucherResponse fromVoucher(Voucher entity) {

        if (entity == null) {
            return null;
        }

        return VoucherResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .code(entity.getCode())
                .discountType(entity.getDiscountType())
                .discountValue(entity.getDiscountValue())
                .maxDiscountAmount(entity.getMaxDiscountAmount())
                .minOrderValue(entity.getMinOrderValue())
                .maxUsage(entity.getMaxUsage())
                .usageCount(entity.getUsageCount())
                .maxUsagePerUser(entity.getMaxUsagePerUser())
                .startDate(entity.getStartDate())
                .expiryDate(entity.getExpiryDate())
                .isActive(entity.getIsActive())
                .tourId(
                        entity.getTour() != null
                                ? entity.getTour().getId()
                                : null
                )
                .userId(
                        entity.getUser() != null
                                ? entity.getUser().getId()
                                : null
                )
                .createdAt(entity.getCreatedAt())
                .build();
    }
}