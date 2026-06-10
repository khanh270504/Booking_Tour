package com.example.bookingtour.dtos.response.dashboard;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardOverviewResponse {
    private TopStatsDto topStats;
    private List<RevenueChartDto> revenueData;
    private List<DestinationDto> destinationData;
    private List<StatusDto> statusData;
    private List<FunnelDto> funnelData;
    private List<SourceDto> sourceData;
    private List<SalesLeaderboardDto> salesLeaderboard;
    private List<TaskAlertDto> urgentTasks;


    @Data @Builder
    public static class TopStatsDto {
        private String totalRevenue;
        private String revenueChange;
        private String winRate;
        private String winRateChange;
        private Integer newLeads;
        private String leadsChange;
        private Integer pendingTasks;
        private Integer overdueTasks;
    }

    @Data @Builder
    public static class RevenueChartDto {
        private String month;
        private Double revenue;
        private Double cost;
    }

    @Data @Builder
    public static class DestinationDto {
        private String name;
        private Integer bookings;
        private String color;
    }

    @Data @Builder
    public static class StatusDto {
        private String name;
        private Integer value;
        private String color;
    }

    @Data @Builder
    public static class FunnelDto {
        private String stage;
        private Integer count;
        private String conversion;
        private String color;
    }

    @Data @Builder
    public static class SourceDto {
        private String name;
        private Integer value;
        private String color;
    }

    @Data @Builder
    public static class SalesLeaderboardDto {
        private String name;
        private String revenue;
        private Integer deals;
        private Integer target;
        private String avatar;
    }

    @Data @Builder
    public static class TaskAlertDto {
        private String text;
        private String staff;
        private String delay;
        private Boolean isUrgent;
    }
}