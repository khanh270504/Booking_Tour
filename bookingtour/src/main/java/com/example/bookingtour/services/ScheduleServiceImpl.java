package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IBookingService;
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

import java.time.LocalDate;
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
    private final IBookingService bookingService;
    private ScheduleResponse enrichScheduleResponse(TourSchedule schedule) {
        ScheduleResponse sDto = ScheduleResponse.fromSchedule(schedule);

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
    @Transactional(readOnly = true)
    public ScheduleResponse getScheduleById(Integer id) {
        TourSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
        return enrichScheduleResponse(schedule);
    }

    @Override
    @Transactional
    public ScheduleResponse createSchedule(ScheduleCreateRequest request) {
        if (request.getDepartureDate() == null || request.getReturnDate() == null) {
            throw new AppException(ErrorCode.INVALID_DATE_FORMAT);
        }

        if (request.getDepartureDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.INVALID_DEPARTURE_DATE);
        }

        if (request.getReturnDate().isBefore(request.getDepartureDate())) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        if (request.getPricings() == null || request.getPricings().isEmpty()) {
            throw new AppException(ErrorCode.PRICING_REQUIRED_FOR_SCHEDULE);
        }

        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        String generatedCode = generateScheduleCode(tour, request.getDepartureDate());

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

        List<TourPricingConfig> pricingConfigs = request.getPricings().stream()
                .map(pricingDto -> TourPricingConfig.builder()
                        .schedule(savedSchedule)
                        .passengerType(pricingDto.getPassengerType())
                        .price(pricingDto.getPrice())
                        .currency(pricingDto.getCurrency())
                        .build())
                .toList();
        pricingRepository.saveAll(pricingConfigs);

        if (request.getSurcharges() != null && !request.getSurcharges().isEmpty()) {
            List<TourSurcharge> surcharges = request.getSurcharges().stream()
                    .map(surchargeDto -> TourSurcharge.builder()
                            .schedule(savedSchedule)
                            .surchargeName(surchargeDto.getSurchargeName())
                            .amount(surchargeDto.getAmount())
                            .isMandatory(surchargeDto.getIsMandatory())
                            .build())
                    .toList();
            surchargeRepository.saveAll(surcharges);
        }

        return enrichScheduleResponse(savedSchedule);
    }

    @Override
    @Transactional
    public ScheduleResponse updateScheduleStatus(Integer id, String statusStr) {
        TourSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        ScheduleStatus newStatus;
        try {
            newStatus = ScheduleStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }

        ScheduleStatus currentStatus = schedule.getStatus();

        if (currentStatus == ScheduleStatus.CANCELLED || currentStatus == ScheduleStatus.COMPLETED) {
            throw new AppException(ErrorCode.CANNOT_UPDATE_FINAL_STATUS);
        }

        if (newStatus == ScheduleStatus.CANCELLED) {
            int totalBooked = schedule.getMaxSlots() - schedule.getAvailableSlots();

            if (totalBooked > 0) {
                log.info("Admin hủy tour ID {} có {} khách. Đang kích hoạt hoàn tiền và hoàn voucher...", id, totalBooked);


                bookingService.cancelAllBookingsBySchedule(id, "Hủy do lý do bất khả kháng/Thời tiết");
            }
        }

        schedule.setStatus(newStatus);
        scheduleRepository.save(schedule);

        return enrichScheduleResponse(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedulesByTourId(Integer tourId) {
        return scheduleRepository.findValidSchedules(
                        tourId,
                        LocalDate.now(),
                        List.of(ScheduleStatus.OPENING, ScheduleStatus.FULL)
                ).stream()
                .map(this::enrichScheduleResponse)
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedulesForAdmin(Integer tourId) {
        return scheduleRepository.findByTourId(tourId)
                .stream().map(this::enrichScheduleResponse).toList();
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
    @Transactional(readOnly = true)
    public List<PricingConfigResponse> getPricingBySchedule(Integer scheduleId) {
        return pricingRepository.findByScheduleId(scheduleId).stream()
                .map(PricingConfigResponse::fromPricingConfig)
                .toList();
    }

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
    @Transactional(readOnly = true)
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


    private String generateScheduleCode(Tour tour, LocalDate departureDate) {
        if (departureDate == null) {
            throw new AppException(ErrorCode.INVALID_DEPARTURE_DATE);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
        String dateStr = departureDate.format(formatter);

        String tourIdentifier = (tour.getTourcode() != null && !tour.getTourcode().trim().isEmpty())
                ? tour.getTourcode().trim().toUpperCase()
                : "TOUR" + tour.getId();

        String baseCode = tourIdentifier + "-" + dateStr;
        String randomSuffix = UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        return baseCode + "-" + randomSuffix;
    }
    @Override
    @Transactional
    public ScheduleResponse updateSchedule(Integer id, ScheduleCreateRequest request) {
        TourSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (schedule.getStatus() == ScheduleStatus.CANCELLED || schedule.getStatus() == ScheduleStatus.COMPLETED) {
            throw new AppException(ErrorCode.CANNOT_UPDATE_FINAL_STATUS);
        }

        int totalBooked = schedule.getMaxSlots() - schedule.getAvailableSlots();
        if (request.getMaxSlots() < totalBooked) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }

        schedule.setMaxSlots(request.getMaxSlots());
        schedule.setAvailableSlots(request.getMaxSlots() - totalBooked);

        if (request.getDepartureLocation() != null && !request.getDepartureLocation().trim().isEmpty()) {
            schedule.setDepartureLocation(request.getDepartureLocation());
        }

        TourSchedule updatedSchedule = scheduleRepository.save(schedule);

        if (request.getPricings() != null && !request.getPricings().isEmpty()) {

            List<TourPricingConfig> oldPricings = pricingRepository.findByScheduleId(id);
            if (oldPricings != null && !oldPricings.isEmpty()) {
                pricingRepository.deleteAll(oldPricings);
            }

            List<TourPricingConfig> newPricingConfigs = request.getPricings().stream()
                    .map(pricingDto -> TourPricingConfig.builder()
                            .schedule(updatedSchedule)
                            .passengerType(pricingDto.getPassengerType())
                            .price(pricingDto.getPrice())
                            .currency(pricingDto.getCurrency() != null ? pricingDto.getCurrency() : "VND")
                            .build())
                    .toList();

            pricingRepository.saveAll(newPricingConfigs);
            log.info(" Admin đã cập nhật lại bảng giá vé mới cho Schedule ID: {}", id);
        }

        return enrichScheduleResponse(updatedSchedule);
    }
}