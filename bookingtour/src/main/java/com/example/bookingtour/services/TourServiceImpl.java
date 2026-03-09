package com.example.bookingtour.services;

import com.example.bookingtour.entities.*;
import com.example.bookingtour.enums.*;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.IServices.ITourService;
import com.example.bookingtour.repositories.*;
import com.example.bookingtour.dtos.request.tour.*;
import com.example.bookingtour.dtos.response.tour.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourServiceImpl implements ITourService {
    private final TourRepository tourRepository;
    private final DestinationRepository destinationRepository;
    private final TourScheduleRepository scheduleRepository;
    private final TourPricingConfigRepository pricingRepository;
    private final TourSurchargeRepository surchargeRepository;

    //Tour
    @Override
    @Transactional
    public TourResponse createTour(TourCreateRequest request) {
        log.info("Bắt đầu tạo Tour: {}", request.getName());

        Destination destination = destinationRepository.findById(request.getDestinationId())
                .orElseThrow(() -> new AppException(ErrorCode.DESTINATION_NOT_FOUND));

        Tour tour = Tour.builder()
                .name(request.getName())
                .tourType(request.getTourType())
                .description(request.getDescription())
                .destination(destination)
                .itinerary(request.getItinerary())
                .status(TourStatus.DRAFT)
                .build();

        return TourResponse.fromTour(tourRepository.save(tour));
    }
    @Override
    @Transactional
    public TourResponse updateTour(Integer id, TourCreateRequest request) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        if (!tour.getDestination().getId().equals(request.getDestinationId())) {
            Destination newDest = destinationRepository.findById(request.getDestinationId())
                    .orElseThrow(() -> new AppException(ErrorCode.DESTINATION_NOT_FOUND));
            tour.setDestination(newDest);
        }

        tour.setName(request.getName());
        tour.setDescription(request.getDescription());
        tour.setTourType(request.getTourType());
        tour.setItinerary(request.getItinerary());

        return TourResponse.fromTour(tourRepository.save(tour));
    }
    @Override
    @Transactional
    public void deleteTour(Integer id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        boolean hasActiveSchedules = scheduleRepository.existsByTourIdAndStatus(id, ScheduleStatus.OPENING);
        if (hasActiveSchedules) {
            throw new AppException(ErrorCode.TOUR_HAS_ACTIVE_SCHEDULES);
        }
        tour.setStatus(TourStatus.INACTIVE);
        tourRepository.save(tour);

        log.warn("Đã chuyển trạng thái Tour ID: {} sang DELETED (Soft Delete)", id);
    }

    @Override
    public List<TourResponse> getAllToursForClient() {
        return tourRepository.findByStatus(TourStatus.ACTIVE).stream()
                .map(TourResponse::fromTour)
                .collect(Collectors.toList());
    }
    @Override
    public List<TourResponse> getAllTours() {
        log.info("Admin đang truy cập danh sách toàn bộ Tour");
        return tourRepository.findAll().stream()
                .map(TourResponse::fromTour)
                .collect(Collectors.toList());
    }
    @Override
    public List<TourResponse> getToursByDestinationForClient(Integer destId) {
        return tourRepository.findByDestinationIdAndStatus(destId, TourStatus.ACTIVE).stream()
                .map(TourResponse::fromTour)
                .collect(Collectors.toList());
    }
    @Override
    public TourResponse getTourById(Integer id) {
        return tourRepository.findById(id)
                .map(TourResponse::fromTour)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
    }

    @Override
    public TourResponse getTourByIdForClient(Integer id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        if (tour.getStatus() != TourStatus.ACTIVE) {
            throw new AppException(ErrorCode.TOUR_NOT_FOUND);
        }

        return TourResponse.fromTour(tour);
    }

    //Lich trinh
    @Override
    @Transactional
    public ScheduleResponse createSchedule(ScheduleCreateRequest request) {

        if (request.getReturnDate().isBefore(request.getDepartureDate())) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        TourSchedule schedule = TourSchedule.builder()
                .tour(tour)
                .departureDate(request.getDepartureDate())
                .returnDate(request.getReturnDate())
                .availableSlots(request.getAvailableSlots())
                .status(ScheduleStatus.OPENING)
                .build();

        return ScheduleResponse.fromSchedule(scheduleRepository.save(schedule));
    }

    @Override
    public List<ScheduleResponse> getSchedulesByTour(Integer tourId) {
        return scheduleRepository.findByTourId(tourId).stream()
                .map(ScheduleResponse::fromSchedule)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ScheduleResponse updateScheduleStatus(Integer scheduleId, String status) {
        TourSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        try {
            schedule.setStatus(ScheduleStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }

        return ScheduleResponse.fromSchedule(scheduleRepository.save(schedule));
    }

    //Gia tien cua tung loai khach
    @Override
    @Transactional
    public PricingConfigResponse createPricing(PricingConfigRequest request) {
        if (request.getPrice().signum() < 0) {
            throw new AppException(ErrorCode.INVALID_AMOUNT);
        }

        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        TourPricingConfig pricing = TourPricingConfig.builder()
                .tour(tour)
                .passengerType(request.getPassengerType())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .effectiveDate(request.getEffectiveDate())
                .build();

        log.info("Đã tạo cấu hình giá mới cho Tour ID: {} - Loại khách: {}", request.getTourId(), request.getPassengerType());
        return PricingConfigResponse.fromPricingConfig(pricingRepository.save(pricing));
    }

    @Override
    public List<PricingConfigResponse> getPricingByTour(Integer tourId) {
        return pricingRepository.findByTourId(tourId).stream()
                .map(PricingConfigResponse::fromPricingConfig)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SurchargeResponse createSurcharge(SurchargeRequest request) {
        if (request.getAmount().signum() < 0) {
            throw new AppException(ErrorCode.INVALID_AMOUNT);
        }

        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        TourSurcharge surcharge = TourSurcharge.builder()
                .tour(tour)
                .surchargeName(request.getSurchargeName())
                .amount(request.getAmount())
                .isMandatory(request.getIsMandatory())
                .build();

        log.info("Đã thêm phụ phí '{}' cho Tour ID: {}", request.getSurchargeName(), request.getTourId());
        return SurchargeResponse.fromSurcharge(surchargeRepository.save(surcharge));
    }
    @Override
    public List<SurchargeResponse> getSurchargesByTour(Integer tourId) {
        return surchargeRepository.findByTourId(tourId).stream()
                .map(SurchargeResponse::fromSurcharge)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSurcharge(Integer id) {
        if (!surchargeRepository.existsById(id)) {
            throw new AppException(ErrorCode.SURCHARGE_NOT_FOUND);
        }
        surchargeRepository.deleteById(id);
        log.warn("Đã xóa phụ phí ID: {}", id);
    }
}
