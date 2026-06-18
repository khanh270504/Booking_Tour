package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IPricingService;
import com.example.bookingtour.IServices.IVoucherService; // 🎯 Thêm ông này vào
import com.example.bookingtour.dtos.internal.PricingResultDto;
import com.example.bookingtour.dtos.request.booking.PassengerRequest;
import com.example.bookingtour.dtos.request.sales.VoucherApplyRequest; // 🎯 Thêm DTO này
import com.example.bookingtour.dtos.response.sales.VoucherApplyResponse;
import com.example.bookingtour.entities.TourPricingConfig;
import com.example.bookingtour.entities.TourSurcharge;
import com.example.bookingtour.enums.PassengerType;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.TourPricingConfigRepository;
import com.example.bookingtour.repositories.TourSurchargeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PricingServiceImpl implements IPricingService {

    TourPricingConfigRepository pricingRepository;
    TourSurchargeRepository surchargeRepository;

    IVoucherService voucherService;

    @Override
    public PricingResultDto calculatePrice(Integer scheduleId, List<PassengerRequest> passengers, String voucherCode, Integer tourId) {
        log.info("--- Bắt đầu tính giá cho Lịch trình ID: {} ---", scheduleId);

        //  Lấy cấu hình giá gốc
        List<TourPricingConfig> pricingConfigs = pricingRepository.findByScheduleId(scheduleId);
        if (pricingConfigs.isEmpty()) {
            throw new AppException(ErrorCode.PRICING_NOT_FOUND);
        }

        Map<PassengerType, BigDecimal> priceMap = pricingConfigs.stream()
                .collect(Collectors.toMap(
                        TourPricingConfig::getPassengerType,
                        TourPricingConfig::getPrice
                ));

        //  Tính tổng giá gốc
        BigDecimal totalOriginal = passengers.stream()
                .map(req -> {
                    try {
                        PassengerType type = req.getPassengerType();
                        if (req.getPassengerType() == null) {
                            throw new AppException(ErrorCode.PRICING_NOT_FOUND);
                        }

                        BigDecimal exactPrice = priceMap.get(type);
                        if (exactPrice == null) throw new AppException(ErrorCode.PRICING_NOT_FOUND);
                        return exactPrice;
                    } catch (IllegalArgumentException e) {
                        throw new AppException(ErrorCode.PRICING_NOT_FOUND);
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //  Tính phụ thu bắt buộc
        List<TourSurcharge> mandatorySurcharges = surchargeRepository.findByScheduleId(scheduleId).stream()
                .filter(TourSurcharge::getIsMandatory)
                .collect(Collectors.toList());

        BigDecimal surchargePerPerson = mandatorySurcharges.stream()
                .map(TourSurcharge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSurcharge = surchargePerPerson.multiply(BigDecimal.valueOf(passengers.size()));

        //  TÍNH TỔNG TIỀN TRƯỚC KHI GIẢM GIÁ (Gốc + Phụ thu)
        BigDecimal totalBeforeDiscount = totalOriginal.add(totalSurcharge);
        BigDecimal totalDiscount = BigDecimal.ZERO;

        //  LOGIC XỬ LÝ VOUCHER
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            // Tạo Request để ném sang VoucherService
            VoucherApplyRequest applyRequest = new VoucherApplyRequest();
            applyRequest.setCode(voucherCode);
            applyRequest.setOrderTotal(totalBeforeDiscount);
            applyRequest.setTourId(tourId);

            // Gọi hàm tính toán siêu việt anh em mình vừa viết
            VoucherApplyResponse voucherResponse = voucherService.applyVoucher(applyRequest);

            // Lấy kết quả tiền được giảm về
            totalDiscount = voucherResponse.getDiscountAmount();
            log.info("=> Đã áp dụng mã {}: Giảm {} VNĐ", voucherCode, totalDiscount);
        }

        //  Tính toán cuối cùng
        BigDecimal totalFinal = totalBeforeDiscount.subtract(totalDiscount);

        if (totalFinal.compareTo(BigDecimal.ZERO) < 0) {
            totalFinal = BigDecimal.ZERO;
        }

        log.info("=> Kết quả: Gốc={} | Phụ thu={} | Giảm giá={} | Tổng cuối={}",
                totalOriginal, totalSurcharge, totalDiscount, totalFinal);

        return PricingResultDto.builder()
                .totalOriginalPrice(totalOriginal)
                .totalSurcharge(totalSurcharge)
                .totalDiscount(totalDiscount)
                .totalFinalPrice(totalFinal)
                .unitPriceMap(priceMap)
                .build();
    }
}