package com.example.bookingtour.services;

import com.example.bookingtour.dtos.response.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
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
    private final TourImageRepository tourImageRepository;

    private TourResponse enrichTourResponse(Tour tour) {
        TourResponse dto = TourResponse.fromTour(tour);

        List<ScheduleResponse> schedules = scheduleRepository.findByTourId(tour.getId()).stream()
                .map(schedule -> {
                    ScheduleResponse sDto = ScheduleResponse.fromSchedule(schedule);
                    List<PricingConfigResponse> pricings = pricingRepository.findByScheduleId(schedule.getId())
                            .stream()
                            .map(PricingConfigResponse::fromPricingConfig)
                            .collect(Collectors.toList());
                    sDto.setPricings(pricings);

                    return sDto;
                })
                .collect(Collectors.toList());

        dto.setSchedules(schedules);
        return dto;
    }
    @Override
    @Transactional
    public TourResponse createTour(TourCreateRequest request) {
        log.info("Bắt đầu tạo Tour: {}", request.getName());

        Destination destination = destinationRepository.findById(request.getDestinationId())
                .orElseThrow(() -> new AppException(ErrorCode.DESTINATION_NOT_FOUND));

        Tour tour = Tour.builder()
                .tourcode(request.getTourCode())
                .name(request.getName())
                .description(request.getDescription())
                .destination(destination)
                .itinerary(request.getItinerary())
                .thumbnail(request.getThumbnail())
                .minParticipants(request.getMinParticipants())
                .status(TourStatus.ACTIVE)
                .build();

        return enrichTourResponse(tourRepository.save(tour));
    }

    @Override
    @Transactional
    public TourResponse updateTour(Integer id, TourCreateRequest request) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        tour.setName(request.getName());
        tour.setDescription(request.getDescription());
        tour.setItinerary(request.getItinerary());

        tour.setThumbnail(request.getThumbnail());
        tour.setMinParticipants(request.getMinParticipants());

        if (!tour.getDestination().getId().equals(request.getDestinationId())) {
            Destination newDest = destinationRepository.findById(request.getDestinationId())
                    .orElseThrow(() -> new AppException(ErrorCode.DESTINATION_NOT_FOUND));
            tour.setDestination(newDest);
        }

        return enrichTourResponse(tour);
    }

    @Override
    public List<TourResponse> getAllToursForClient() {
        return tourRepository.findByStatus(TourStatus.ACTIVE).stream()
                .map(this::enrichTourResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TourResponse getTourByIdForClient(Integer id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        if (tour.getStatus() != TourStatus.ACTIVE) {
            throw new AppException(ErrorCode.TOUR_NOT_FOUND);
        }
        return enrichTourResponse(tour);
    }
    @Override
    public List<TourResponse> getAllTours() {
        log.info("Admin đang truy cập danh sách toàn bộ Tour");
        return tourRepository.findAll().stream()
                .map(this::enrichTourResponse)
                .collect(Collectors.toList());
    }

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
                .maxSlots(request.getMaxSlots())
                .availableSlots(request.getMaxSlots())
                .status(ScheduleStatus.OPENING)
                .build();

        return ScheduleResponse.fromSchedule(scheduleRepository.save(schedule));
    }
    @Override
    @Transactional
    public ScheduleResponse updateScheduleStatus(Integer scheduleId, String status) {
        TourSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        try {

            ScheduleStatus newStatus = ScheduleStatus.valueOf(status.toUpperCase());
            schedule.setStatus(newStatus);

            log.info("Đã cập nhật trạng thái lịch trình ID {} sang {}", scheduleId, newStatus);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }
        return ScheduleResponse.fromSchedule(scheduleRepository.save(schedule));
    }

    @Override
    public List<DestinationResponse> getAllDestinations() {
        return destinationRepository.findAll().stream()
                .map(dest -> {
                    DestinationResponse res = new DestinationResponse();
                    res.setId(dest.getId());
                    res.setName(dest.getName());
                    res.setDescription(dest.getDescription());
                    return res;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TourImageResponse> getImagesByTour(Integer tourId) {
        return tourImageRepository.findByTourId(tourId).stream()
                .map(img -> {
                    TourImageResponse res = new TourImageResponse();
                    res.setId(img.getId());
                    res.setTourId(img.getTour().getId());
                    res.setImageUrl(img.getImageUrl());
                    return res;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> getSchedulesByTour(Integer tourId) {
        return scheduleRepository.findByTourId(tourId).stream()
                .map(ScheduleResponse::fromSchedule).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTour(Integer id) {
        Tour tour = tourRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        tour.setStatus(TourStatus.INACTIVE);
        tourRepository.save(tour);
    }
    @Override
    public List<PricingConfigResponse> getPricingBySchedule(Integer scheduleId) {
        return pricingRepository.findByScheduleId(scheduleId).stream()
                .map(PricingConfigResponse::fromPricingConfig)
                .collect(Collectors.toList());
    }
    @Override
    public List<SurchargeResponse> getSurchargesBySchedule(Integer scheduleId) {
        return surchargeRepository.findByScheduleId(scheduleId).stream()
                .map(SurchargeResponse::fromSurcharge)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public PricingConfigResponse createPricing(PricingConfigRequest request) {
        // Tìm Schedule thay vì tìm Tour
        TourSchedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        TourPricingConfig pricing = TourPricingConfig.builder()
                .schedule(schedule) // Map vào Schedule
                .passengerType(request.getPassengerType())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .build();

        return PricingConfigResponse.fromPricingConfig(pricingRepository.save(pricing));
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
    @Transactional
    public void deleteSurcharge(Integer id) {
        if (!surchargeRepository.existsById(id)) {
            throw new AppException(ErrorCode.SURCHARGE_NOT_FOUND);
        }
        surchargeRepository.deleteById(id);
        log.warn("Đã xóa phụ phí ID: {}", id);
    }

    @Override
    public PageResponse<TourResponse> searchTours(TourSearchRequest searchRequest, Pageable pageable) {
        log.info("Thực hiện tìm kiếm tour với tiêu chí: {}", searchRequest);


        Specification<Tour> spec = TourSpecification.filterTours(searchRequest);


        Page<Tour> tourPage = tourRepository.findAll(spec, pageable);

        List<TourResponse> tourResponses = tourPage.getContent().stream()
                .map(this::enrichTourResponse)
                .collect(Collectors.toList());

        return PageResponse.<TourResponse>builder()
                .currentPage(tourPage.getNumber() + 1)
                .totalPages(tourPage.getTotalPages())
                .pageSize(tourPage.getSize())
                .totalElements(tourPage.getTotalElements())
                .data(tourResponses)
                .build();
    }
}