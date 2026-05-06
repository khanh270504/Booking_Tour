package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.tour.PricingConfigRequest;
import com.example.bookingtour.dtos.request.tour.ScheduleCreateRequest;
import com.example.bookingtour.dtos.request.tour.SurchargeRequest;
import com.example.bookingtour.dtos.response.tour.PricingConfigResponse;
import com.example.bookingtour.dtos.response.tour.ScheduleResponse;
import com.example.bookingtour.dtos.response.tour.SurchargeResponse;

import java.util.List;

public interface IScheduleService {
    ScheduleResponse createSchedule(ScheduleCreateRequest request);

    List<ScheduleResponse> getSchedulesByTourId(Integer tourId);

    ScheduleResponse updateScheduleStatus(Integer scheduleId, String status);

    ScheduleResponse getScheduleById(Integer id);


    PricingConfigResponse createPricing(PricingConfigRequest request);

    List<PricingConfigResponse> getPricingBySchedule(Integer scheduleId);

    SurchargeResponse createSurcharge(SurchargeRequest request);

    List<SurchargeResponse> getSurchargesBySchedule(Integer scheduleId);

    void deleteSurcharge(Integer id);
}