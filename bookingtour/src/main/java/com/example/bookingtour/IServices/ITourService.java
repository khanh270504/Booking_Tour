package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.tour.*;
import com.example.bookingtour.dtos.response.tour.*;

import java.util.List;

public interface ITourService {

    // Quản lý thông tin Tour chính
    TourResponse createTour(TourCreateRequest request);
    List<TourResponse> getAllTours();
    List<TourResponse> getAllToursForClient();
    TourResponse getTourById(Integer id);
    List<TourResponse> getToursByDestinationForClient(Integer destId);
    TourResponse getTourByIdForClient(Integer id);
    TourResponse updateTour(Integer id, TourCreateRequest request);
    void deleteTour(Integer id);

    // Quản lý Lịch trình
    ScheduleResponse createSchedule(ScheduleCreateRequest request);
    List<ScheduleResponse> getSchedulesByTour(Integer tourId);
    ScheduleResponse updateScheduleStatus(Integer scheduleId, String status);

    // Quản lý Cấu hình giá
    PricingConfigResponse createPricing(PricingConfigRequest request);
    List<PricingConfigResponse> getPricingByTour(Integer tourId);

    // Quản lý Phụ phí
    SurchargeResponse createSurcharge(SurchargeRequest request);
    List<SurchargeResponse> getSurchargesByTour(Integer tourId);
    void deleteSurcharge(Integer id);
}