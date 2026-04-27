package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.tour.*;
import com.example.bookingtour.dtos.response.PageResponse;
import com.example.bookingtour.dtos.response.tour.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ITourService {

    // --- Quản lý Tour chính ---
    TourResponse createTour(TourCreateRequest request);

    List<TourResponse> getAllTours();

    List<TourResponse> getAllToursForClient();

    TourResponse getTourByIdForClient(Integer id);

    TourResponse updateTour(Integer id, TourCreateRequest request);

    void deleteTour(Integer id);

    // --- Quản lý Lịch trình (Schedules) ---
    ScheduleResponse createSchedule(ScheduleCreateRequest request);

    List<ScheduleResponse> getSchedulesByTour(Integer tourId);

    ScheduleResponse updateScheduleStatus(Integer scheduleId, String status);

    // --- Quản lý Giá
    PricingConfigResponse createPricing(PricingConfigRequest request);

    List<PricingConfigResponse> getPricingBySchedule(Integer scheduleId);

    // --- Quản lý Phụ phí
    SurchargeResponse createSurcharge(SurchargeRequest request);

    List<SurchargeResponse> getSurchargesBySchedule(Integer scheduleId);

    void deleteSurcharge(Integer id);

    // --- Tiện ích & Hình ảnh ---
    List<DestinationResponse> getAllDestinations();

    List<TourImageResponse> getImagesByTour(Integer tourId);

    // --- TÌM KIẾM (Search) ---
    PageResponse<TourResponse> searchTours(TourSearchRequest searchRequest, Pageable pageable);

}