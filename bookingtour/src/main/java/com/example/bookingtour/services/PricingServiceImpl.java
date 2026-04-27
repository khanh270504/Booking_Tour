package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IPricingService;
import com.example.bookingtour.dtos.internal.PricingResultDto;
import com.example.bookingtour.dtos.request.booking.PassengerRequest;
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

    @Override
    public PricingResultDto calculatePrice(Integer scheduleId, List<PassengerRequest> passengers) {
        log.info("--- Bắt đầu tính giá cho Lịch trình (Schedule) ID: {} ---", scheduleId);

        // 1. Lấy bảng giá của Lịch trình này
        List<TourPricingConfig> pricingConfigs = pricingRepository.findByScheduleId(scheduleId);
        if (pricingConfigs.isEmpty()) {
            throw new AppException(ErrorCode.PRICING_NOT_FOUND);
        }

        Map<PassengerType, BigDecimal> priceMap = pricingConfigs.stream()
                .collect(Collectors.toMap(
                        TourPricingConfig::getPassengerType,
                        TourPricingConfig::getPrice
                ));

        // 2. Tính tổng giá gốc dựa trên số lượng và loại hành khách
        BigDecimal totalOriginal = passengers.stream()
                .map(req -> {
                    try {
                        PassengerType type = PassengerType.valueOf(req.getPassengerType().toUpperCase());
                        BigDecimal exactPrice = priceMap.get(type);
                        if (exactPrice == null) {
                            log.error("Không tìm thấy giá cho loại khách: {}", type);
                            throw new AppException(ErrorCode.PRICING_NOT_FOUND);
                        }
                        return exactPrice;
                    } catch (IllegalArgumentException e) {
                        throw new AppException(ErrorCode.PRICING_NOT_FOUND);
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. TỰ ĐỘNG TÍNH PHỤ THU BẮT BUỘC (Mandatory Surcharges)
        // Ví dụ: Phí môi trường, Phí phục vụ... cứ là Mandatory thì phải cộng vào tổng
        List<TourSurcharge> mandatorySurcharges = surchargeRepository.findByScheduleId(scheduleId).stream()
                .filter(TourSurcharge::getIsMandatory)
                .collect(Collectors.toList());

        // Tổng phụ thu = (Tổng tiền phụ thu cố định) * (Số lượng người)
        // Lưu ý: Nếu phụ thu tính theo đầu người thì nhân với passengers.size()
        BigDecimal surchargePerPerson = mandatorySurcharges.stream()
                .map(TourSurcharge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSurcharge = surchargePerPerson.multiply(BigDecimal.valueOf(passengers.size()));

        // 4. Tính toán cuối cùng
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalFinal = totalOriginal.add(totalSurcharge).subtract(totalDiscount);

        if (totalFinal.compareTo(BigDecimal.ZERO) < 0) {
            totalFinal = BigDecimal.ZERO;
        }

        log.info("=> Kết quả: Gốc={} | Phụ thu bắt buộc={} | Tổng cuối={}",
                totalOriginal, totalSurcharge, totalFinal);

        return PricingResultDto.builder()
                .totalOriginalPrice(totalOriginal)
                .totalSurcharge(totalSurcharge)
                .totalDiscount(totalDiscount)
                .totalFinalPrice(totalFinal)
                .unitPriceMap(priceMap)
                .build();
    }
}