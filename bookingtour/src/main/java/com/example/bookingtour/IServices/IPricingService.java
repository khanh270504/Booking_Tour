package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.internal.PricingResultDto;
import com.example.bookingtour.dtos.request.booking.PassengerRequest;

import java.util.List;

public interface IPricingService {
    PricingResultDto calculatePrice(Integer scheduleId, List<PassengerRequest> passengers, String voucherCode, Integer tourId);
}