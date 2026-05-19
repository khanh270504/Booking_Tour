package com.example.bookingtour.services;

import com.example.bookingtour.dtos.request.sales.VoucherApplyRequest;
import com.example.bookingtour.dtos.request.sales.VoucherCreateRequest;
import com.example.bookingtour.dtos.response.sales.VoucherApplyResponse;
import com.example.bookingtour.dtos.response.sales.VoucherResponse;
import com.example.bookingtour.entities.Voucher;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.TourRepository;
import com.example.bookingtour.repositories.UserRepository;
import com.example.bookingtour.repositories.VoucherRepository;
import com.example.bookingtour.IServices.IVoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherServiceImpl implements IVoucherService {

    private final VoucherRepository voucherRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public VoucherResponse createVoucher(VoucherCreateRequest request) {
        if (voucherRepository.findByCode(request.getCode().toUpperCase()).isPresent()) {
            throw new AppException(ErrorCode.VOUCHER_ALREADY_EXISTS);
        }

        if (request.getStartDate() != null && request.getExpiryDate().isBefore(request.getStartDate())) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        Voucher voucher = Voucher.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .code(request.getCode().toUpperCase())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderValue(request.getMinOrderValue())
                .maxUsage(request.getMaxUsage())
                .maxUsagePerUser(request.getMaxUsagePerUser())
                .startDate(request.getStartDate())
                .expiryDate(request.getExpiryDate())
                .isActive(request.getIsActive())
                .tour(request.getTourId() != null
                        ? tourRepository.findById(request.getTourId())
                        .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND))
                        : null)
                .user(request.getUserId() != null
                        ? userRepository.findById(request.getUserId())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND))
                        : null)
                .usageCount(0)
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
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
        return VoucherResponse.fromVoucher(voucher);
    }

    @Override
    @Transactional
    public VoucherResponse updateVoucher(Integer id, VoucherCreateRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        if (request.getStartDate() != null && request.getExpiryDate().isBefore(request.getStartDate())) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        voucher.setTitle(request.getTitle());
        voucher.setDescription(request.getDescription());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setMaxUsage(request.getMaxUsage());
        voucher.setMaxUsagePerUser(request.getMaxUsagePerUser());
        voucher.setStartDate(request.getStartDate());
        voucher.setExpiryDate(request.getExpiryDate());
        voucher.setIsActive(request.getIsActive());

        voucher.setTour(request.getTourId() != null
                ? tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND))
                : null);

        voucher.setUser(request.getUserId() != null
                ? userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND))
                : null);

        return VoucherResponse.fromVoucher(voucherRepository.save(voucher));
    }

    @Override
    public VoucherApplyResponse applyVoucher(VoucherApplyRequest request) {
        Voucher voucher = voucherRepository.findByCode(request.getCode().toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        // 1. Kiểm tra trạng thái hoạt động
        if (!voucher.getIsActive()) {
            throw new AppException(ErrorCode.VOUCHER_INACTIVE);
        }

        // 2. Kiểm tra thời gian (Ngày bắt đầu & Ngày hết hạn)
        Instant now = Instant.now();
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            throw new AppException(ErrorCode.VOUCHER_NOT_YET_STARTED);
        }
        if (now.isAfter(voucher.getExpiryDate())) {
            throw new AppException(ErrorCode.VOUCHER_EXPIRED);
        }

        // 3. Kiểm tra tổng lượt dùng hệ thống
        if (voucher.getUsageCount() >= voucher.getMaxUsage()) {
            throw new AppException(ErrorCode.VOUCHER_OUT_OF_STOCK);
        }

        // 4. Kiểm tra điều kiện Tour (Nếu voucher dành riêng cho 1 tour)
        if (voucher.getTour() != null) {
            if (request.getTourId() == null) {
                throw new AppException(ErrorCode.VOUCHER_NOT_FOR_THIS_TOUR);
            }

            if (!voucher.getTour().getId().equals(request.getTourId())) {
                throw new AppException(ErrorCode.VOUCHER_NOT_FOR_THIS_TOUR);
            }
        }

        // 5. Kiểm tra JWT: Voucher dành riêng cho 1 User (VIP Promo)
        if (voucher.getUser() != null) {
            Integer currentUserId = getCurrentUserIdSafely();

            if (currentUserId == null) {
                throw new AppException(ErrorCode.UNAUTHORIZED_VOUCHER_ACCESS);
            }

            if (!voucher.getUser().getId().equals(currentUserId)) {
                throw new AppException(ErrorCode.VOUCHER_NOT_FOR_YOU);
            }
        }
        // 6. Kiểm tra giá trị đơn hàng tối thiểu
        if (request.getOrderTotal().compareTo(voucher.getMinOrderValue()) < 0) {
            throw new AppException(ErrorCode.ORDER_TOTAL_NOT_ENOUGH);
        }

        return VoucherApplyResponse.fromVoucher(voucher, request.getOrderTotal());
    }

    @Override
    @Transactional
    public void redeemVoucher(String code) {
        Voucher voucher = voucherRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        if (!voucher.getIsActive() || Instant.now().isAfter(voucher.getExpiryDate())) {
            throw new AppException(ErrorCode.VOUCHER_EXPIRED);
        }

        if (voucher.getUsageCount() >= voucher.getMaxUsage()) {
            throw new AppException(ErrorCode.VOUCHER_OUT_OF_STOCK);
        }

        voucher.setUsageCount(voucher.getUsageCount() + 1);
        voucherRepository.save(voucher);

        log.info(" Đã chốt Voucher: {} | Lượt dùng mới: {}", code, voucher.getUsageCount());
    }

    @Override
    public VoucherResponse getVoucherByCode(String code) {
        return voucherRepository.findByCode(code.toUpperCase())
                .map(VoucherResponse::fromVoucher)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
    }

    @Override
    public void deleteVoucher(Integer id) {
        throw new UnsupportedOperationException("Tính năng xóa bị chặn. Vui lòng chuyển trạng thái isActive = false");
    }
    private Integer getCurrentUserIdSafely() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) auth.getPrincipal();
            Long userId = jwt.getClaim("userId");
            return userId != null ? userId.intValue() : null;
        }
        return null;
    }
    @Override
    public List<VoucherResponse> getActiveVouchersForPublic() {
        Instant now = Instant.now();

        return voucherRepository.findAll().stream()
                .filter(Voucher::getIsActive)
                .filter(v -> v.getUsageCount() < v.getMaxUsage())
                .filter(v -> v.getExpiryDate().isAfter(now))
                .filter(v -> v.getStartDate() == null || !v.getStartDate().isAfter(now))
                .map(VoucherResponse::fromVoucher)
                .collect(Collectors.toList());
    }

}