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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

    private List<TourResponse> enrichTourResponsesBulk(List<Tour> tours) {
        if (tours.isEmpty()) return List.of();

        List<Integer> tourIds = tours.stream().map(Tour::getId).collect(Collectors.toList());

        List<TourSchedule> allSchedules = scheduleRepository.findByTourIdIn(tourIds);

        List<Integer> scheduleIds = allSchedules.stream().map(TourSchedule::getId).collect(Collectors.toList());

        List<TourPricingConfig> allPricings = pricingRepository.findByScheduleIdIn(scheduleIds);

        Map<Integer, List<PricingConfigResponse>> pricingMap = allPricings.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getSchedule().getId(),
                        Collectors.mapping(PricingConfigResponse::fromPricingConfig, Collectors.toList())
                ));

        Map<Integer, List<ScheduleResponse>> scheduleMap = allSchedules.stream()
                .map(schedule -> {
                    ScheduleResponse sDto = ScheduleResponse.fromSchedule(schedule);
                    sDto.setPricings(pricingMap.getOrDefault(schedule.getId(), List.of()));
                    return sDto;
                })
                .collect(Collectors.groupingBy(ScheduleResponse::getTourId, Collectors.toList()));

        return tours.stream()
                .map(tour -> {
                    TourResponse dto = TourResponse.fromTour(tour);
                    dto.setSchedules(scheduleMap.getOrDefault(tour.getId(), List.of()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "tours", key = "'client_all'")
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
    @Caching(evict = {
            @CacheEvict(value = "tour", key = "#id"),
            @CacheEvict(value = "tours", key = "'client_all'")
    })
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
    @Cacheable(value = "tours", key = "'client_all'")
    public List<TourResponse> getAllToursForClient() {
        List<Tour> tours = tourRepository.findByStatus(TourStatus.ACTIVE);
        return enrichTourResponsesBulk(tours);
    }

    @Override
    @Cacheable(value = "tour", key = "#id")
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
        List<Tour> tours = tourRepository.findAll();
        return enrichTourResponsesBulk(tours);
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
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tour", key = "#id"),
            @CacheEvict(value = "tours", key = "'client_all'")
    })
    public void deleteTour(Integer id) {
        Tour tour = tourRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        tour.setStatus(TourStatus.INACTIVE);
        tourRepository.save(tour);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tour", key = "#id"),
            @CacheEvict(value = "tours", key = "'client_all'")
    })
    public void restoreTour(Integer id) {
        log.info("Admin đang khôi phục tour có ID: {}", id);
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tour để khôi phục"));

        tour.setStatus(TourStatus.ACTIVE);
        tourRepository.save(tour);
    }

    @Override
    public PageResponse<TourResponse> searchTours(TourSearchRequest searchRequest, Pageable pageable) {
        log.info("Thực hiện tìm kiếm tour với tiêu chí: {}", searchRequest);

        Specification<Tour> spec = TourSpecification.filterTours(searchRequest);
        Page<Tour> tourPage = tourRepository.findAll(spec, pageable);

        List<TourResponse> tourResponses = enrichTourResponsesBulk(tourPage.getContent());

        return PageResponse.<TourResponse>builder()
                .currentPage(tourPage.getNumber() + 1)
                .totalPages(tourPage.getTotalPages())
                .pageSize(tourPage.getSize())
                .totalElements(tourPage.getTotalElements())
                .data(tourResponses)
                .build();
    }

    private String generateCode(Tour tour) {
        return tour.getName().toUpperCase().replaceAll("\\s+", "")
                + "_" + LocalDate.now().getMonthValue();
    }
}