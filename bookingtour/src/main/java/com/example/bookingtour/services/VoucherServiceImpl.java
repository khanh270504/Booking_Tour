package com.example.bookingtour.services;

import com.example.bookingtour.dtos.request.sales.VoucherApplyRequest;
import com.example.bookingtour.dtos.request.sales.VoucherCreateRequest;
import com.example.bookingtour.dtos.response.sales.VoucherApplyResponse;
import com.example.bookingtour.dtos.response.sales.VoucherResponse;
import com.example.bookingtour.entities.Voucher;
import com.example.bookingtour.repositories.VoucherRepository;
import com.example.bookingtour.IServices.IVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements IVoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    @Transactional
    public VoucherResponse createVoucher(VoucherCreateRequest request) {
        if (voucherRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("Mã giảm giá đã tồn tại!");
        }

        Voucher voucher = Voucher.builder()
                .code(request.getCode().toUpperCase())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderValue(request.getMinOrderValue())
                .maxUsage(request.getMaxUsage())
                .usageCount(0)
                .expiryDate(request.getExpiryDate())
                .build();

        return VoucherResponse.fromVoucher(voucherRepository.save(voucher));
    }

    @Override
    public List<VoucherResponse> getAllVouchers() {
        return voucherRepository.findAll().stream()
                .map(VoucherResponse::fromVoucher)
                .collect(Collectors.toList());
    }

    @Override
    public VoucherResponse getVoucherById(Integer id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Voucher"));
        return VoucherResponse.fromVoucher(voucher);
    }

    @Override
    @Transactional
    public VoucherResponse updateVoucher(Integer id, VoucherCreateRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Voucher"));

        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setMaxUsage(request.getMaxUsage());
        voucher.setExpiryDate(request.getExpiryDate());

        return VoucherResponse.fromVoucher(voucherRepository.save(voucher));
    }

    @Override
    public void deleteVoucher(Integer id) {
        // Tạm thời để trống hoặc ném ngoại lệ chờ nâng cấp Soft Delete như ông giáo muốn
        throw new UnsupportedOperationException("Tính năng xóa cứng bị chặn để bảo vệ dữ liệu kế toán!");
    }

    @Override
    public VoucherApplyResponse applyVoucher(VoucherApplyRequest request) {
        // 1. Tìm voucher
        Voucher voucher = voucherRepository.findByCode(request.getCode().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ"));

        // 2. Kiểm tra các điều kiện cơ bản
        if (voucher.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Mã giảm giá đã hết hạn");
        }
        if (voucher.getUsageCount() >= voucher.getMaxUsage()) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
        }
        if (request.getOrderTotal().compareTo(voucher.getMinOrderValue()) < 0) {
            throw new RuntimeException("Đơn hàng chưa đủ giá trị tối thiểu: " + voucher.getMinOrderValue());
        }

        // 3. Tính toán số tiền giảm
        BigDecimal discountAmount = BigDecimal.ZERO;
        if ("FIXED_AMOUNT".equals(voucher.getDiscountType())) {
            discountAmount = voucher.getDiscountValue();
        } else if ("PERCENT".equals(voucher.getDiscountType())) {
            BigDecimal ratio = voucher.getDiscountValue().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            discountAmount = request.getOrderTotal().multiply(ratio);

            // Chặn trần giảm tối đa
            if (voucher.getMaxDiscountAmount() != null && discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                discountAmount = voucher.getMaxDiscountAmount();
            }
        }

        // Không cho giảm quá tổng tiền đơn hàng
        if (discountAmount.compareTo(request.getOrderTotal()) > 0) {
            discountAmount = request.getOrderTotal();
        }

        return VoucherApplyResponse.builder()
                .voucherId(voucher.getId())
                .voucherCode(voucher.getCode())
                .originalTotal(request.getOrderTotal())
                .discountAmount(discountAmount)
                .finalTotal(request.getOrderTotal().subtract(discountAmount))
                .message("Áp dụng mã thành công!")
                .build();
    }

    @Override
    @Transactional
    public void redeemVoucher(String code) {
        Voucher voucher = voucherRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Mã không tồn tại để chốt"));

        if (voucher.getUsageCount() >= voucher.getMaxUsage()) {
            throw new RuntimeException("Mã vừa hết lượt ngay khi bạn thanh toán!");
        }

        voucher.setUsageCount(voucher.getUsageCount() + 1);
        voucherRepository.save(voucher);
    }

    @Override
    public VoucherResponse getVoucherByCode(String code) {
        return voucherRepository.findByCode(code.toUpperCase())
                .map(VoucherResponse::fromVoucher)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại"));
    }
}