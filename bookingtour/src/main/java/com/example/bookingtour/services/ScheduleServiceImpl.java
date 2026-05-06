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

import java.util.List;

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

        TourSchedule schedule = TourSchedule.builder()
                .tour(tour)
                .departureDate(request.getDepartureDate())
                .returnDate(request.getReturnDate())
                .maxSlots(request.getMaxSlots())
                .availableSlots(request.getMaxSlots())
                .status(ScheduleStatus.OPENING)
                .build();

        return enrichScheduleResponse(scheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public ScheduleResponse updateScheduleStatus(Integer id, String status) {
        TourSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
        schedule.setStatus(ScheduleStatus.valueOf(status.toUpperCase()));
        return enrichScheduleResponse(scheduleRepository.save(schedule));
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
}
