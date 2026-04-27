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
    private String code;
    private String discountType;
    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderValue;

    private Integer maxUsage;
    private Integer usageCount;
    private Instant expiryDate;

    public static VoucherResponse fromVoucher(Voucher entity) {
        if (entity == null) {
            return null;
        }
        return VoucherResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .discountType(entity.getDiscountType())
                .discountValue(entity.getDiscountValue())
                .maxDiscountAmount(entity.getMaxDiscountAmount())
                .minOrderValue(entity.getMinOrderValue())
                .maxUsage(entity.getMaxUsage())
                .usageCount(entity.getUsageCount())
                .expiryDate(entity.getExpiryDate())
                .build();
    }
}