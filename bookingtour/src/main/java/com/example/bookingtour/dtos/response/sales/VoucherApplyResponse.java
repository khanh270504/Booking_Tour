package com.example.bookingtour.dtos.response.sales;

import com.example.bookingtour.entities.Voucher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherApplyResponse {

    private Integer voucherId;
    private String voucherCode;

    private String title;
    private String description;

    private BigDecimal originalTotal;
    private BigDecimal discountAmount;
    private BigDecimal finalTotal;

    private boolean success;
    private String message;

    public static VoucherApplyResponse fromVoucher(Voucher voucher, BigDecimal orderTotal) {
        if (voucher == null) return null;

        BigDecimal discount = BigDecimal.ZERO;

        if ("FIXED".equalsIgnoreCase(voucher.getDiscountType())) {
            discount = voucher.getDiscountValue();
        } else if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
            BigDecimal ratio = voucher.getDiscountValue().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            discount = orderTotal.multiply(ratio);

            if (voucher.getMaxDiscountAmount() != null && discount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                discount = voucher.getMaxDiscountAmount();
            }
        }

        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }

        return VoucherApplyResponse.builder()
                .voucherId(voucher.getId())
                .voucherCode(voucher.getCode())
                .title(voucher.getTitle())
                .description(voucher.getDescription())
                .originalTotal(orderTotal)
                .discountAmount(discount)
                .finalTotal(orderTotal.subtract(discount))
                .success(true)
                .message("Áp dụng mã giảm giá thành công!")
                .build();
    }
}