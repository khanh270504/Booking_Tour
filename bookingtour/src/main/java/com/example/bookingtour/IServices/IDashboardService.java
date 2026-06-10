package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.response.dashboard.DashboardOverviewResponse;

public interface IDashboardService {

    DashboardOverviewResponse getOverviewData(String period);
}
