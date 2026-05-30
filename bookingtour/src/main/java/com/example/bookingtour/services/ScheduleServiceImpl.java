package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IScheduleService;
import com.example.bookingtour.dtos.request.tour.PricingConfigRequest;
import com.example.bookingtour.dtos.request.tour.ScheduleCreateRequest;
import com.example.bookingtour.dtos.request.tour.SurchargeRequest;
import com.example.bookingtour.dtos.response.tour.PricingConfigResponse;
import com.example.bookingtour.dtos.response.tour.ScheduleResponse;
import com.example.bookingtour.dtos.response.tour.SurchargeResponse;
import com.example.bookingtour.entities.Tour;
import com.example.bookingtour.entities.TourPricingConfig;
import com.example.bookingtour.entities.TourSchedule;
import com.example.bookingtour.entities.TourSurcharge;
import com.example.bookingtour.enums.ScheduleStatus;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.TourPricingConfigRepository;
import com.example.bookingtour.repositories.TourRepository;
import com.example.bookingtour.repositories.TourScheduleRepository;
import com.example.bookingtour.repositories.TourSurchargeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements IScheduleService {
    private final TourScheduleRepository scheduleRepository;
    private final TourPricingConfigRepository pricingRepository;
    private final TourSurchargeRepository surchargeRepository;
    private final TourRepository tourRepository;


    private ScheduleResponse enrichScheduleResponse(TourSchedule schedule) {
        ScheduleResponse sDto = ScheduleResponse.fromSchedule(schedule);

        // Lấy và nạp Pricing
        List<TourPricingConfig> pricings = pricingRepository.findByScheduleId(schedule.getId());
        if (pricings != null) {
            sDto.setPricings(pricings.stream()
                    .map(PricingConfigResponse::fromPricingConfig).toList());

        }

        List<TourSurcharge> surcharges = surchargeRepository.findByScheduleId(schedule.getId());
        if (surcharges != null) {
            sDto.setSurcharges(surcharges.stream()
                    .map(SurchargeResponse::fromSurcharge).toList());
        }

        return sDto;
    }
    @Override
    public ScheduleResponse getScheduleById(Integer id) {
        TourSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
        return enrichScheduleResponse(schedule);
    }
    @Override
    @Transactional
    public ScheduleResponse createSchedule(ScheduleCreateRequest request) {
        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        String generatedCode = generateScheduleCode(tour, request.getDepartureDate());

        // 1. Lưu Schedule trước để sinh ra ID trong Database
        TourSchedule schedule = TourSchedule.builder()
                .tour(tour)
                .departureDate(request.getDepartureDate())
                .scheduleCode(generatedCode)
                .departureLocation(request.getDepartureLocation())
                .returnDate(request.getReturnDate())
                .maxSlots(request.getMaxSlots())
                .availableSlots(request.getMaxSlots())
                .status(ScheduleStatus.OPENING)
                .build();

        TourSchedule savedSchedule = scheduleRepository.save(schedule);

        // 2. Lưu danh sách Cấu hình Giá (Pricings) đi kèm
        if (request.getPricings() != null && !request.getPricings().isEmpty()) {
            List<TourPricingConfig> pricingConfigs = request.getPricings().stream()
                    .map(pricingDto -> TourPricingConfig.builder()
                            .schedule(savedSchedule)
                            .passengerType(pricingDto.getPassengerType())
                            .price(pricingDto.getPrice())
                            .currency(pricingDto.getCurrency())
                            .build())
                    .toList();
            pricingRepository.saveAll(pricingConfigs); // Lưu cả cụm vào DB
        }

        // 3. Lưu danh sách Phụ phí (Surcharges) đi kèm
        if (request.getSurcharges() != null && !request.getSurcharges().isEmpty()) {
            List<TourSurcharge> surcharges = request.getSurcharges().stream()
                    .map(surchargeDto -> TourSurcharge.builder()
                            .schedule(savedSchedule)
                            .surchargeName(surchargeDto.getSurchargeName())
                            .amount(surchargeDto.getAmount())
                            .isMandatory(surchargeDto.getIsMandatory())
                            .build())
                    .toList();
            surchargeRepository.saveAll(surcharges); // Lưu cả cụm vào DB
        }

        return enrichScheduleResponse(savedSchedule);
    }

    @Override
    @Transactional
    public ScheduleResponse updateScheduleStatus(Integer id, String status) {
        TourSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
        try {
            schedule.setStatus(ScheduleStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }

        return enrichScheduleResponse(schedule);
    }

    @Override
    public List<ScheduleResponse> getSchedulesByTourId(Integer tourId) {
        return scheduleRepository.findByTourId(tourId).stream()
                .map(this::enrichScheduleResponse)
                .toList();
    }
    @Override
    @Transactional
    public PricingConfigResponse createPricing(PricingConfigRequest request) {
        TourSchedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        TourPricingConfig pricing = TourPricingConfig.builder()
                .schedule(schedule)
                .passengerType(request.getPassengerType())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .build();

        return PricingConfigResponse.fromPricingConfig(pricingRepository.save(pricing));
    }

    @Override
    public List<PricingConfigResponse> getPricingBySchedule(Integer scheduleId) {
        return pricingRepository.findByScheduleId(scheduleId).stream()
                .map(PricingConfigResponse::fromPricingConfig)
                .toList();
    }

    // --- Quản lý Phụ phí (Surcharge) ---
    @Override
    @Transactional
    public SurchargeResponse createSurcharge(SurchargeRequest request) {
        TourSchedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        TourSurcharge surcharge = TourSurcharge.builder()
                .schedule(schedule)
                .surchargeName(request.getSurchargeName())
                .amount(request.getAmount())
                .isMandatory(request.getIsMandatory())
                .build();

        return SurchargeResponse.fromSurcharge(surchargeRepository.save(surcharge));
    }

    @Override
    public List<SurchargeResponse> getSurchargesBySchedule(Integer scheduleId) {
        return surchargeRepository.findByScheduleId(scheduleId).stream()
                .map(SurchargeResponse::fromSurcharge)
                .toList();
    }

    @Override
    @Transactional
    public void deleteSurcharge(Integer id) {
        if (!surchargeRepository.existsById(id)) {
            throw new AppException(ErrorCode.SURCHARGE_NOT_FOUND);
        }
        surchargeRepository.deleteById(id);
        log.info("Đã xóa phụ phí ID: {}", id);
    }
    private String generateScheduleCode(Tour tour, Object departureDate) {
        String dateStr = "";

        if (departureDate instanceof java.time.LocalDate) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
            dateStr = ((java.time.LocalDate) departureDate).format(formatter);
        } else {
            String[] parts = departureDate.toString().split("-");
            if (parts.length == 3) {
                dateStr = parts[2] + parts[1] + parts[0].substring(2);
            } else {
                dateStr = String.valueOf(System.currentTimeMillis()).substring(8); // Fallback nếu lỗi chuỗi
            }
        }

        String tourIdentifier = (tour.getTourcode() != null && !tour.getTourcode().isEmpty())
                ? tour.getTourcode().toUpperCase()
                : "TOUR" + tour.getId();

        String baseCode = tourIdentifier + "-" + dateStr;

        String randomSuffix = UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        return baseCode + "-" + randomSuffix;


    }
}
