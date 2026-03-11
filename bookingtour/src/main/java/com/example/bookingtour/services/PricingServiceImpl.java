package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IPricingService;
import com.example.bookingtour.dtos.internal.PricingResultDto;
import com.example.bookingtour.dtos.request.booking.PassengerRequest;
import com.example.bookingtour.entities.TourPricingConfig;
import com.example.bookingtour.enums.PassengerType;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.TourPricingConfigRepository;
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

    @Override
    public PricingResultDto calculatePrice(Integer tourId, List<PassengerRequest> passengers) {
        log.info("--- Bắt đầu báo giá cho Tour ID: {} ---", tourId);

        List<TourPricingConfig> pricingConfigs = pricingRepository.findByTourId(tourId);
        if (pricingConfigs.isEmpty()) {
            throw new AppException(ErrorCode.PRICING_NOT_FOUND);
        }

        Map<PassengerType, BigDecimal> priceMap = pricingConfigs.stream()
                .collect(Collectors.toMap(
                        TourPricingConfig::getPassengerType,
                        TourPricingConfig::getPrice
                ));


        BigDecimal totalOriginal = passengers.stream()
                .map(req -> {
                    PassengerType type = PassengerType.valueOf(req.getPassengerType().toUpperCase());
                    BigDecimal exactPrice = priceMap.get(type);
                    if (exactPrice == null) throw new AppException(ErrorCode.PRICING_NOT_FOUND);
                    return exactPrice;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        BigDecimal totalSurcharge = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        BigDecimal totalFinal = totalOriginal.add(totalSurcharge).subtract(totalDiscount);

        if (totalFinal.compareTo(BigDecimal.ZERO) < 0) {
            totalFinal = BigDecimal.ZERO;
        }

        log.info("=> Báo giá xong: Gốc = {}, Phụ thu = {}, Giảm = {}, Cuối = {}",
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