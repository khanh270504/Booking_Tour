package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.tour.PricingConfigRequest;
import com.example.bookingtour.dtos.request.tour.ScheduleCreateRequest;
import com.example.bookingtour.dtos.request.tour.TourCreateRequest;
import com.example.bookingtour.dtos.request.tour.TourSearchRequest;
import com.example.bookingtour.dtos.response.tour.PricingConfigResponse;
import com.example.bookingtour.dtos.response.tour.ScheduleResponse;
import com.example.bookingtour.dtos.response.tour.TourResponse;

import java.util.List;

public interface ITourService {
    // 1. Quản lý Tour
    TourResponse createTour(TourCreateRequest request);
    TourResponse getTourById(Integer tourId);
    List<TourResponse> searchTours(TourSearchRequest request);

    // 2. Mở bán lịch khởi hành
    ScheduleResponse createSchedule(ScheduleCreateRequest request);
    List<ScheduleResponse> getSchedulesByTour(Integer tourId);

    // 3. Cấu hình giá
    PricingConfigResponse addPricingConfig(PricingConfigRequest request);
}