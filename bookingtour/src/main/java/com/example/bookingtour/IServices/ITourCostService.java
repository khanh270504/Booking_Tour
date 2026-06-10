package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.operation.TourCostRequest;
import com.example.bookingtour.dtos.response.PageResponse;
import com.example.bookingtour.dtos.response.operation.TourCostResponse;
import java.util.List;
import java.util.Map;

public interface ITourCostService {
    TourCostResponse createTourCost(TourCostRequest request);
    TourCostResponse updateTourCost(Integer id, TourCostRequest request);
    List<TourCostResponse> getTourCostsByScheduleId(Integer scheduleId);
    TourCostResponse getTourCostById(Integer id);
    TourCostResponse updateCostStatus(Integer id, String status, String note);
    void deleteTourCost(Integer id);
    List<TourCostResponse> getTourCostsByProviderId(Integer providerId);
    List<TourCostResponse> getAllTourCosts();
    PageResponse<TourCostResponse> getTourCosts(int page, int size, String keyword);
    Map<String, Double> getCostStatistics();
}